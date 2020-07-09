package me.blog.korn123.easydiary.fragments

import android.accounts.Account
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_dev.*
import kotlinx.android.synthetic.main.layout_settings_backup_gms.*
import kotlinx.android.synthetic.main.layout_settings_backup_gms.accountInfo
import kotlinx.android.synthetic.main.layout_settings_backup_gms.profilePhoto
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.RealmFileItemAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper.Companion.callAccountCallback
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper.Companion.initGoogleSignAccount
import me.blog.korn123.easydiary.services.BackupPhotoService
import me.blog.korn123.easydiary.services.RecoverPhotoService
import java.util.*

class SettingsGMSBackupFragment() : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var progressContainer: ConstraintLayout
    private lateinit var mRootView: ViewGroup
    private var mTaskFlag = 0
    private val mActivity: Activity
        get() = activity!!


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.layout_settings_backup_gms, container, false) as ViewGroup
        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        progressContainer = mActivity.findViewById(R.id.progressContainer)

        // Clear google OAuth token generated prior to version 1.4.80
        if (!mActivity.config.clearLegacyToken) GoogleOAuthHelper.signOutGoogleOAuth(mActivity, false)

        bindEvent()
        updateFragmentUI(mRootView)
        initPreference()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mRootView)
        initPreference()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        mActivity.pauseLock()

        when (resultCode == Activity.RESULT_OK && intent != null) {
            true -> {
                // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
                when (requestCode) {
                    REQUEST_CODE_GOOGLE_SIGN_IN -> {
                        // The Task returned from this call is always completed, no need to attach
                        // a listener.
                        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
                        val googleSignAccount = task.getResult(ApiException::class.java)
                        googleSignAccount?.account?.let {
                            callAccountCallback(it)
                        }
                    }
                }
            }
            false -> {
                when (requestCode) {
                    REQUEST_CODE_GOOGLE_SIGN_IN, REQUEST_CODE_GOOGLE_DRIVE_PERMISSIONS -> {
                        mActivity.makeSnackBar("Google account verification failed.")
                        progressContainer.visibility = View. GONE
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mActivity.pauseLock()
        if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            when (requestCode) {
                REQUEST_CODE_EXTERNAL_STORAGE -> if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    when (mTaskFlag) {
                        SETTING_FLAG_EXPORT_GOOGLE_DRIVE -> backupDiaryRealm()
                        SETTING_FLAG_IMPORT_GOOGLE_DRIVE -> recoverDiaryRealm()
                        SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE -> backupDiaryPhoto()
                        SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE -> recoverDiaryPhoto()
                    }
                }
            }
        } else {
            mActivity.makeSnackBar(mActivity.findViewById(android.R.id.content), getString(R.string.guide_message_3))
        }
    }


    /***************************************************************************************************
     *   backup and recovery
     *
     ***************************************************************************************************/


    private fun initDriveWorkingDirectory(account: Account, workingFolderName: String, callback: (workingFolderId: String) -> Unit) {
        val driveServiceHelper = DriveServiceHelper(mActivity, account)
        // 01. AAF 폴더 검색
        driveServiceHelper.queryFiles("'root' in parents and name = '${DriveServiceHelper.AAF_ROOT_FOLDER_NAME}' and trashed = false").run {
            addOnSuccessListener { result ->
                when (result.files.size) {
                    // 02. AAF 폴더 없으면 생성
                    0 -> driveServiceHelper.createFolder(DriveServiceHelper.AAF_ROOT_FOLDER_NAME).addOnSuccessListener { aafFolderId ->
                        // 02-01. workingFolder 생성
                        driveServiceHelper.createFolder(workingFolderName, aafFolderId).addOnSuccessListener { workingFolderId ->
                            callback(workingFolderId)
                        }
                    }
                    // 03. workingFolder 검색
                    1 -> {
                        val parentId = result.files[0].id
                        driveServiceHelper.queryFiles("'$parentId' in parents and name = '$workingFolderName' and trashed = false").addOnSuccessListener {
                            when (it.files.size) {
                                // 03-01. workingFolder 생성
                                0 -> driveServiceHelper.createFolder(workingFolderName, parentId).addOnSuccessListener { workingFolderId ->
                                    callback(workingFolderId)
                                }
                                1 -> {
                                    callback(it.files[0].id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun backupDiaryRealm() {
        mActivity.setScreenOrientationSensor(false)
        progressContainer.visibility = View.VISIBLE
        // delete unused compressed photo file
//        File(Environment.getExternalStorageDirectory().absolutePath + DIARY_PHOTO_DIRECTORY).listFiles()?.map {
//            Log.i("PHOTO-URI", "${it.absolutePath} | ${EasyDiaryDbHelper.countPhotoUriBy(FILE_URI_PREFIX + it.absolutePath)}")
//            if (EasyDiaryDbHelper.countPhotoUriBy(FILE_URI_PREFIX + it.absolutePath) == 0) it.delete()
//        }
        val realmPath = EasyDiaryDbHelper.getRealmPath()
        initGoogleSignAccount(this) { account ->
            initDriveWorkingDirectory(account, DriveServiceHelper.AAF_EASY_DIARY_REALM_FOLDER_NAME) {
                val driveServiceHelper = DriveServiceHelper(mActivity, account)
                driveServiceHelper.createFile(
                        it, realmPath,
                        DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
                        EasyDiaryUtils.easyDiaryMimeType
                ).addOnSuccessListener {
                    progressContainer.visibility = View. GONE
                    mActivity.makeSnackBar(getString(R.string.backup_completed_message))
                    mActivity.config.diaryBackupGoogle = System.currentTimeMillis()
                    mActivity.setScreenOrientationSensor(true)
                }.addOnFailureListener { e ->
                    mActivity.makeSnackBar(e.message ?: "Please try again later.")
                    progressContainer.visibility = View.GONE
                    mActivity.setScreenOrientationSensor(true)
                }
            }
        }
    }

    private fun recoverDiaryRealm() {
        mActivity.setScreenOrientationSensor(false)
        progressContainer.visibility = View.VISIBLE
        openRealmFilePickerDialog()
    }

    private fun openRealmFilePickerDialog() {
        initGoogleSignAccount(this) { account ->
            val driveServiceHelper = DriveServiceHelper(mActivity, account)

//            driveServiceHelper.queryFiles("mimeType contains 'text/aaf_v' and name contains '$DIARY_DB_NAME'", 1000)

            driveServiceHelper.queryFiles("(mimeType = '${EasyDiaryUtils.easyDiaryMimeTypeAll.joinToString("' or mimeType = '")}') and trashed = false", 1000)
                    .addOnSuccessListener {
                        var alertDialog: AlertDialog? = null
                        val realmFiles: ArrayList<HashMap<String, String>> = arrayListOf()
                        it.files.map { file ->
                            val itemInfo = hashMapOf<String, String>("name" to file.name, "id" to file.id, "createdTime" to file.createdTime.toString())
                            realmFiles.add(itemInfo)
                        }
                        val builder = AlertDialog.Builder(mActivity)
                        builder.setNegativeButton(getString(android.R.string.cancel)) { _, _ -> mActivity.setScreenOrientationSensor(true) }
//                        builder.setMessage(getString(R.string.open_realm_file_message))
                        val inflater = mActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val fontView = inflater.inflate(R.layout.dialog_realm_files, null)
                        val listView = fontView.findViewById<ListView>(R.id.files)
                        val adapter = RealmFileItemAdapter(mActivity, R.layout.item_realm_file, realmFiles)
                        listView.adapter = adapter
                        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            val itemInfo = parent.adapter.getItem(position) as HashMap<String, String>
                            itemInfo["id"]?.let { realmFileId ->
                                progressContainer.visibility = View.VISIBLE
                                val realmPath = EasyDiaryDbHelper.getRealmPath()
                                EasyDiaryDbHelper.closeInstance()
                                driveServiceHelper.downloadFile(realmFileId, realmPath).run {
                                    addOnSuccessListener {
                                        mActivity.refreshApp()
                                    }
                                    addOnFailureListener {  }
                                }

                            }
                            alertDialog?.cancel()
                        }

                        alertDialog = builder.create().apply { mActivity.updateAlertDialog(this, null, fontView, "${getString(R.string.open_realm_file_title)} (Total: ${it.files.size})") }
                        progressContainer.visibility = View.GONE
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        mActivity.makeSnackBar(e.message ?: "Please try again later.")
                        progressContainer.visibility = View.GONE
                    }
        }
    }

    private fun recoverDiaryPhoto() {
        mActivity.setScreenOrientationSensor(false)
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount(this) { _ ->
            mActivity.runOnUiThread {
                progressContainer.visibility = View.GONE
                mActivity.run {
                    showAlertDialog(getString(R.string.recover_confirm_attached_photo), DialogInterface.OnClickListener { _, _ ->
                        val recoverPhotoService = Intent(this, RecoverPhotoService::class.java)
                        startService(recoverPhotoService)
                        finish()
                    }, DialogInterface.OnClickListener { _, _ -> setScreenOrientationSensor(true) })
                }
            }
        }
    }

    private fun backupDiaryPhoto() {
        mActivity.setScreenOrientationSensor(false)
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount(this) { account ->
            initDriveWorkingDirectory(account, DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                progressContainer.visibility = View.GONE
                mActivity.run {
                    showAlertDialog(getString(R.string.backup_confirm_message), DialogInterface.OnClickListener { _, _ ->
                        val backupPhotoService = Intent(this, BackupPhotoService::class.java)
                        backupPhotoService.putExtra(DriveServiceHelper.WORKING_FOLDER_ID, photoFolderId)
                        startService(backupPhotoService)
                        finish()
                    }, DialogInterface.OnClickListener { _, _ -> setScreenOrientationSensor(true) })
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
                if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    recoverDiaryRealm()
                } else { // Permission has already been granted
                    confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.backupSetting -> {
                mTaskFlag = SETTING_FLAG_EXPORT_GOOGLE_DRIVE
                if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    backupDiaryRealm()
                } else { // Permission has already been granted
                    confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.backupAttachPhoto -> {
                mTaskFlag = SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE
                when (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> backupDiaryPhoto()
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.recoverAttachPhoto -> {
                mTaskFlag = SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE
                when (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> recoverDiaryPhoto()
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.signOutGoogleOAuth -> {
                GoogleOAuthHelper.signOutGoogleOAuth(mActivity)
                determineAccountInfo()
            }
        }
    }

    private fun bindEvent() {
        restoreSetting.setOnClickListener(mOnClickListener)
        backupSetting.setOnClickListener(mOnClickListener)
        backupAttachPhoto.setOnClickListener(mOnClickListener)
        recoverAttachPhoto.setOnClickListener(mOnClickListener)
        signOutGoogleOAuth.setOnClickListener(mOnClickListener)
    }

    private fun initPreference() {
        determineAccountInfo()
    }

    private fun determineAccountInfo() {
        when (GoogleOAuthHelper.isValidGoogleSignAccount(mActivity)) {
            true -> {
                profilePhoto.visibility = View.VISIBLE
                GoogleOAuthHelper.getGoogleSignAccount(mActivity)?.run {
                    val sb = StringBuilder()
                    sb.append(this.displayName +  System.getProperty("line.separator"))
                    sb.append(this.email)
                    accountInfo.text = sb.toString()
                    Glide.with(mActivity)
                            .load(this.photoUrl)
                            .apply(RequestOptions().circleCrop())
                            .into(profilePhoto)
                }
            }
            false -> {
                profilePhoto.visibility = View.GONE
                accountInfo.text = "No linked Google account information"
            }
        }
    }
}
