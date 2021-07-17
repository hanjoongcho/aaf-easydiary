package me.blog.korn123.easydiary.fragments

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BaseSettingsActivity
import me.blog.korn123.easydiary.adapters.RealmFileItemAdapter
import me.blog.korn123.easydiary.databinding.PartialSettingsBackupGmsBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper.Companion.callAccountCallback
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper.Companion.initGoogleSignAccount
import me.blog.korn123.easydiary.services.BackupPhotoService
import me.blog.korn123.easydiary.services.RecoverPhotoService
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class SettingsGMSBackupFragment : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: PartialSettingsBackupGmsBinding
    private lateinit var progressContainer: ConstraintLayout
    private lateinit var mContext: Context
    private var mTaskFlag = 0
    private val mRequestExternalStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        requireActivity().pauseLock()
        if (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            when (mTaskFlag) {
                SETTING_FLAG_EXPORT_GOOGLE_DRIVE -> backupDiaryRealm()
                SETTING_FLAG_IMPORT_GOOGLE_DRIVE -> recoverDiaryRealm()
                SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE -> backupDiaryPhoto()
                SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE -> recoverDiaryPhoto()
            }
        } else {
            requireActivity().makeSnackBar(requireActivity().findViewById(android.R.id.content), getString(R.string.guide_message_3))
        }
    }
    private val mRequestGoogleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        requireActivity().pauseLock()
        when (it.resultCode == Activity.RESULT_OK && it.data != null) {
            true -> {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                val googleSignAccount = task.getResult(ApiException::class.java)
                googleSignAccount?.account?.let {
                    callAccountCallback(it)
                }
            }
            false -> {
                requireActivity().makeSnackBar("Google account verification failed.")
                progressContainer.visibility = View. GONE
            }
        }
    }
    private val mRequestGoogleDrivePermissions = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        requireActivity().pauseLock()
        when (it.resultCode == Activity.RESULT_OK && it.data != null) {
            true -> {
                mPermissionCallback.invoke()
            }
            false -> {
                requireActivity().makeSnackBar("Google account verification failed.")
                progressContainer.visibility = View. GONE
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = PartialSettingsBackupGmsBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressContainer = (requireActivity() as BaseSettingsActivity).getProgressContainer()

        // Clear google OAuth token generated prior to version 1.4.80
        if (!requireActivity().config.clearLegacyToken) GoogleOAuthHelper.signOutGoogleOAuth(requireActivity(), false)

        bindEvent()
        updateFragmentUI(mBinding.root)
        initPreference()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mBinding.root)
        initPreference()
    }


    /***************************************************************************************************
     *   backup and recovery
     *
     ***************************************************************************************************/
    // FIXME: workaround
    private lateinit var mPermissionCallback: () -> Unit
    private fun requestDrivePermissions(account: Account, permissionCallback: () -> Unit) {
        mPermissionCallback = permissionCallback
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = account
        val googleDriveService: Drive = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
                .setApplicationName(getString(R.string.app_name))
                .build()

        val executor = Executors.newSingleThreadExecutor()
        Tasks.call(executor, Callable<FileList> {
            try {
                var r = googleDriveService.files().list().setQ("'root' in parents and name = '${DriveServiceHelper.AAF_ROOT_FOLDER_NAME}' and trashed = false").setSpaces("drive").execute()
                mPermissionCallback.invoke()
                r
            } catch (e: UserRecoverableAuthIOException) {
                mRequestGoogleDrivePermissions.launch(e.intent)
                null
            }
        })
    }

    private fun backupDiaryRealm() {
        requireActivity().setScreenOrientationSensor(false)
        progressContainer.visibility = View.VISIBLE
        val realmPath = EasyDiaryDbHelper.getRealmPath()
        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
            requestDrivePermissions(account) {
                DriveServiceHelper(mContext, account).run {
                    initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_REALM_FOLDER_NAME) {
                        createFile(
                                it!!, realmPath,
                                DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
                                EasyDiaryUtils.easyDiaryMimeType
                        ).addOnSuccessListener {
                            progressContainer.visibility = View. GONE
                            requireActivity().makeSnackBar(getString(R.string.backup_completed_message))
                            requireActivity().config.diaryBackupGoogle = System.currentTimeMillis()
                            requireActivity().setScreenOrientationSensor(true)
                        }.addOnFailureListener { e ->
                            requireActivity().makeSnackBar(e.message ?: "Please try again later.")
                            progressContainer.visibility = View.GONE
                            requireActivity().setScreenOrientationSensor(true)
                        }
                    }
                }
            }
        }
    }

    private fun recoverDiaryRealm() {
        requireActivity().setScreenOrientationSensor(false)
        progressContainer.visibility = View.VISIBLE
        openRealmFilePickerDialog()
    }

    private fun openRealmFilePickerDialog() {
        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
            requestDrivePermissions(account) {
                val driveServiceHelper = DriveServiceHelper(mContext, account)
//            driveServiceHelper.queryFiles("mimeType contains 'text/aaf_v' and name contains '$DIARY_DB_NAME'", 1000)
                driveServiceHelper.queryFiles("(mimeType = '${EasyDiaryUtils.easyDiaryMimeTypeAll.joinToString("' or mimeType = '")}') and trashed = false", 1000)
                        .addOnSuccessListener {
                            var alertDialog: AlertDialog? = null
                            val realmFiles: ArrayList<HashMap<String, String>> = arrayListOf()
                            it.files.map { file ->
                                val itemInfo = hashMapOf<String, String>("name" to file.name, "id" to file.id, "createdTime" to file.createdTime.toString())
                                realmFiles.add(itemInfo)
                            }
                            val builder = AlertDialog.Builder(requireActivity())
                            builder.setNegativeButton(getString(android.R.string.cancel)) { _, _ -> requireActivity().setScreenOrientationSensor(true) }
//                        builder.setMessage(getString(R.string.open_realm_file_message))
                            val inflater = requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val fontView = inflater.inflate(R.layout.dialog_realm_files, null)
                            val listView = fontView.findViewById<ListView>(R.id.files)
                            val adapter = RealmFileItemAdapter(requireActivity(), R.layout.item_realm_file, realmFiles)
                            listView.adapter = adapter
                            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                                val itemInfo = parent.adapter.getItem(position) as HashMap<String, String>
                                itemInfo["id"]?.let { realmFileId ->
                                    progressContainer.visibility = View.VISIBLE
                                    val realmPath = EasyDiaryDbHelper.getRealmPath()
                                    EasyDiaryDbHelper.closeInstance()
                                    driveServiceHelper.downloadFile(realmFileId, realmPath).run {
                                        addOnSuccessListener {
                                            requireActivity().refreshApp()
                                        }
                                        addOnFailureListener {  }
                                    }

                                }
                                alertDialog?.cancel()
                            }

                            alertDialog = builder.create().apply { requireActivity().updateAlertDialog(this, null, fontView, "${getString(R.string.open_realm_file_title)} (Total: ${it.files.size})") }
                            progressContainer.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            requireActivity().makeSnackBar(e.message ?: "Please try again later.")
                            progressContainer.visibility = View.GONE
                        }
            }
        }
    }

    private fun recoverDiaryPhoto() {
        requireActivity().setScreenOrientationSensor(false)
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
            requestDrivePermissions(account) {
                requireActivity().runOnUiThread {
                    progressContainer.visibility = View.GONE
                    requireActivity().run {
                        showAlertDialog(getString(R.string.recover_confirm_attached_photo), DialogInterface.OnClickListener { _, _ ->
                            val recoverPhotoService = Intent(this, RecoverPhotoService::class.java)
                            startService(recoverPhotoService)
                            finish()
                        }, DialogInterface.OnClickListener { _, _ -> setScreenOrientationSensor(true) })
                    }
                }
            }
        }
    }

    private fun backupDiaryPhoto() {
        requireActivity().setScreenOrientationSensor(false)
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
            requestDrivePermissions(account) {
                DriveServiceHelper(mContext, account).run {
                    initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                        progressContainer.visibility = View.GONE
                        requireActivity().run {
                            showAlertDialog(getString(R.string.backup_confirm_message), DialogInterface.OnClickListener { _, _ ->
                                val backupPhotoService = Intent(this, BackupPhotoService::class.java)
                                backupPhotoService.putExtra(DriveServiceHelper.WORKING_FOLDER_ID, photoFolderId)
                                ContextCompat.startForegroundService(context, backupPhotoService)
                                finish()
                            }, DialogInterface.OnClickListener { _, _ -> setScreenOrientationSensor(true) })
                        }
                    }
                }
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.restoreSetting -> {
                mTaskFlag = SETTING_FLAG_IMPORT_GOOGLE_DRIVE
                if (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    recoverDiaryRealm()
                } else { // Permission has already been granted
                    mRequestExternalStoragePermissionLauncher.launch(EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
            R.id.backupSetting -> {
                mTaskFlag = SETTING_FLAG_EXPORT_GOOGLE_DRIVE
                if (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    backupDiaryRealm()
                } else { // Permission has already been granted
                    mRequestExternalStoragePermissionLauncher.launch(EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
            R.id.backupAttachPhoto -> {
                mTaskFlag = SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE
                when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> backupDiaryPhoto()
                    false -> mRequestExternalStoragePermissionLauncher.launch(EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
            R.id.recoverAttachPhoto -> {
                mTaskFlag = SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE
                when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> recoverDiaryPhoto()
                    false -> mRequestExternalStoragePermissionLauncher.launch(EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
            R.id.signInGoogleOAuth -> {
                when (GoogleOAuthHelper.isValidGoogleSignAccount(requireActivity())) {
                    false -> {
                        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
                            requestDrivePermissions(account) {
                                determineAccountInfo()
                            }
                        }
                    }
                }
            }
            R.id.signOutGoogleOAuth -> {
                GoogleOAuthHelper.signOutGoogleOAuth(requireActivity())
                determineAccountInfo()
            }
        }
    }

    private fun bindEvent() {
        mBinding.run {
            restoreSetting.setOnClickListener(mOnClickListener)
            backupSetting.setOnClickListener(mOnClickListener)
            backupAttachPhoto.setOnClickListener(mOnClickListener)
            recoverAttachPhoto.setOnClickListener(mOnClickListener)
            signInGoogleOAuth.setOnClickListener(mOnClickListener)
            signOutGoogleOAuth.setOnClickListener(mOnClickListener)
        }
    }

    private fun initPreference() {
        determineAccountInfo()
    }

    private fun determineAccountInfo() {
        mBinding.run {
            when (GoogleOAuthHelper.isValidGoogleSignAccount(requireActivity())) {
                true -> {
                    profilePhoto.visibility = View.VISIBLE
                    signInGoogleOAuthTitle.text = getString(R.string.google_drive_account_information_title)
                    GoogleOAuthHelper.getGoogleSignAccount(requireActivity())?.run {
                        val sb = StringBuilder()
                        sb.append(this.displayName +  System.getProperty("line.separator"))
                        sb.append(this.email)
                        requireActivity().runOnUiThread { accountInfo.text = sb.toString() }
                        Glide.with(requireActivity())
                                .load(this.photoUrl)
                                .apply(RequestOptions().circleCrop())
                                .into(profilePhoto)
                    }
                }
                false -> {
                    profilePhoto.visibility = View.GONE
                    signInGoogleOAuthTitle.text = getString(R.string.google_drive_account_sign_in_title)
                    accountInfo.text = getString(R.string.google_drive_account_sign_in_description)
                }
            }
        }
    }
}
