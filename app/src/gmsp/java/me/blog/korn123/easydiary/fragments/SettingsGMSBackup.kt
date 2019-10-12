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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
import kotlinx.android.synthetic.main.layout_settings_backup_gms.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.FontItemAdapter
import me.blog.korn123.easydiary.adapters.RealmFileItemAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.services.BackupPhotoService
import me.blog.korn123.easydiary.services.RecoverPhotoService
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class SettingsGMSBackup() : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var progressContainer: ConstraintLayout
    private lateinit var mAccountCallback: (Account) -> Unit
    private lateinit var mRootView: ViewGroup
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private var mAlertDialog: AlertDialog? = null
    private var mDevModeClickCount = 0
    private var mTaskFlag = 0


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

        mContext = context!!
        mActivity = activity!!
        progressContainer = mActivity.findViewById(R.id.progressContainer)

        // Clear google OAuth token generated prior to version 1.4.80
        if (!mContext.config.clearLegacyToken) signOutGoogleOAuth(false)

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
                            requestDrivePermissions(it) { mAccountCallback.invoke(it) }
                        }
                    }
                    REQUEST_CODE_GOOGLE_DRIVE_PERMISSIONS -> {
                        mPermissionCallback.invoke()
                    }
                    REQUEST_CODE_FONT_PICK -> {
                        intent.data?.let { uri ->
                            val fileName = EasyDiaryUtils.queryName(mContext.contentResolver, uri)
                            if (FilenameUtils.getExtension(fileName).equals("ttf", true)) {
                                val inputStream = mContext.contentResolver.openInputStream(uri)
                                val fontDestDir = File(EasyDiaryUtils.getApplicationDataDirectory(mContext) + USER_CUSTOM_FONTS_DIRECTORY)
                                FileUtils.copyInputStreamToFile(inputStream, File(fontDestDir, fileName))
                            } else {
                                mActivity.showAlertDialog(getString(R.string.add_ttf_fonts_title), "$fileName is not ttf file.", null)
                            }
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
    private fun signOutGoogleOAuth(showCompleteMessage: Boolean = true) {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.oauth_requerst_id_token))
                .requestEmail()
                .build()
        val client = GoogleSignIn.getClient(mContext, gso)
        client.signOut().addOnCompleteListener {
            mContext.config.clearLegacyToken = true
            if (showCompleteMessage) mActivity.makeSnackBar("Sign out complete:)")
        }
    }

    private fun initGoogleSignAccount(callback: (account: Account) -> Unit) {
        mAccountCallback = callback

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(mContext)

        if (googleSignInAccount == null) {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.oauth_requerst_id_token))
                    .requestEmail()
                    .build()
            val client = GoogleSignIn.getClient(mActivity, gso)
            startActivityForResult(client.signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
        } else {
            googleSignInAccount.account?.let {
                requestDrivePermissions(it) { mAccountCallback.invoke(it) }
            }
        }
    }

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
                startActivityForResult(e.intent, REQUEST_CODE_GOOGLE_DRIVE_PERMISSIONS)
                null
            }
        })
    }

    private fun initDriveWorkingDirectory(account: Account, workingFolderName: String, callback: (workingFolderId: String) -> Unit) {
        val driveServiceHelper = DriveServiceHelper(mContext, account)
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
        progressContainer.visibility = View.VISIBLE
        // delete unused compressed photo file
//        File(Environment.getExternalStorageDirectory().absolutePath + DIARY_PHOTO_DIRECTORY).listFiles()?.map {
//            Log.i("PHOTO-URI", "${it.absolutePath} | ${EasyDiaryDbHelper.countPhotoUriBy(FILE_URI_PREFIX + it.absolutePath)}")
//            if (EasyDiaryDbHelper.countPhotoUriBy(FILE_URI_PREFIX + it.absolutePath) == 0) it.delete()
//        }

        initGoogleSignAccount { account ->
            initDriveWorkingDirectory(account, DriveServiceHelper.AAF_EASY_DIARY_REALM_FOLDER_NAME) {
                val driveServiceHelper = DriveServiceHelper(mContext, account)
                driveServiceHelper.createFile(
                        it, EasyDiaryDbHelper.getInstance().path,
                        DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
                        EasyDiaryUtils.easyDiaryMimeType
                ).addOnSuccessListener {
                    progressContainer.visibility = View. GONE
                    mActivity.makeSnackBar(getString(R.string.backup_completed_message))
                }.addOnFailureListener { e ->
                    mActivity.makeSnackBar(e.message ?: "Please try again later.")
                    progressContainer.visibility = View.GONE
                }
            }
        }
    }

    private fun recoverDiaryRealm() {
        progressContainer.visibility = View.VISIBLE
        openRealmFilePickerDialog()
    }

    private fun openRealmFilePickerDialog() {
        initGoogleSignAccount { account ->
            val driveServiceHelper = DriveServiceHelper(mContext, account)

//            driveServiceHelper.queryFiles("mimeType contains 'text/aaf_v' and name contains '$DIARY_DB_NAME'", 1000)

            driveServiceHelper.queryFiles("(mimeType = '${EasyDiaryUtils.easyDiaryMimeTypeAll.joinToString("' or mimeType = '")}') and trashed = false", 1000)
                    .addOnSuccessListener {
                        val realmFiles: ArrayList<HashMap<String, String>> = arrayListOf()
                        it.files.map { file ->
                            val itemInfo = hashMapOf<String, String>("name" to file.name, "id" to file.id, "createdTime" to file.createdTime.toString())
                            realmFiles.add(itemInfo)
                        }
                        val builder = AlertDialog.Builder(mContext)
                        builder.setNegativeButton(getString(android.R.string.cancel), null)
                        builder.setTitle("${getString(R.string.open_realm_file_title)} (Total: ${it.files.size})")
                        builder.setMessage(getString(R.string.open_realm_file_message))
                        val inflater = mContext.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val fontView = inflater.inflate(R.layout.dialog_realm_files, null)
                        val listView = fontView.findViewById<ListView>(R.id.files)
                        val adapter = RealmFileItemAdapter(mActivity, R.layout.item_realm_file, realmFiles)
                        listView.adapter = adapter
                        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            val itemInfo = parent.adapter.getItem(position) as HashMap<String, String>
                            itemInfo["id"]?.let { realmFileId ->
                                progressContainer.visibility = View.VISIBLE
                                driveServiceHelper.downloadFile(realmFileId, EasyDiaryDbHelper.getInstance().path).run {
                                    addOnSuccessListener {
                                        mActivity.restartApp()
                                    }
                                    addOnFailureListener {  }
                                }

                            }
                            mAlertDialog?.cancel()
                        }

                        builder.setView(fontView)
                        mAlertDialog = builder.create()
                        mAlertDialog?.show()
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
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount { _ ->
            mActivity.runOnUiThread {
                progressContainer.visibility = View.GONE
                mActivity.showAlertDialog(getString(R.string.recover_confirm_attached_photo), DialogInterface.OnClickListener {_, _ ->
                    val recoverPhotoService = Intent(mContext, RecoverPhotoService::class.java)
                    mActivity.startService(recoverPhotoService)
                    mActivity.finish()
                }, null)
            }
        }
    }

    private fun backupDiaryPhoto() {
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount { account ->
            initDriveWorkingDirectory(account, DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                progressContainer.visibility = View.GONE
                mActivity.showAlertDialog(getString(R.string.backup_confirm_message), DialogInterface.OnClickListener { _, _ ->
                    val backupPhotoService = Intent(mContext, BackupPhotoService::class.java)
                    backupPhotoService.putExtra(DriveServiceHelper.WORKING_FOLDER_ID, photoFolderId)
                    mActivity.startService(backupPhotoService)
                    mActivity.finish()
                }, null)
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
                signOutGoogleOAuth()
            }
        }
    }

    private fun bindEvent() {
        restoreSetting.setOnClickListener(mOnClickListener)
        backupSetting.setOnClickListener(mOnClickListener)
        backupAttachPhoto.setOnClickListener(mOnClickListener)
        recoverAttachPhoto.setOnClickListener(mOnClickListener)
        signOutGoogleOAuth.setOnClickListener(mOnClickListener)
//                devMode.setOnClickListener {
//            mDevModeClickCount++
//            if (mDevModeClickCount > 5) {
//                signOutGoogleOAuth.visibility = View.VISIBLE
//                makeSnackBar(BuildConfig.VERSION_CODE.toString())
//            }
//        }
    }

    private fun initPreference() {

    }

    private fun openFontSettingDialog() {
        EasyDiaryUtils.initWorkingDirectory(mContext)
        val builder = AlertDialog.Builder(mContext)
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.setTitle(getString(R.string.font_setting))
        val inflater = mContext.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val fontView = inflater.inflate(R.layout.dialog_fonts, null)
        val listView = fontView.findViewById<ListView>(R.id.listFont)

        val fontNameArray = resources.getStringArray(R.array.pref_list_fonts_title)
        val fontPathArray = resources.getStringArray(R.array.pref_list_fonts_values)
        val listFont = ArrayList<Map<String, String>>()
        var selectedIndex = 0
        for (i in fontNameArray.indices) {
            val map = HashMap<String, String>()
            map.put("disPlayFontName", fontNameArray[i])
            map.put("fontName", fontPathArray[i])
            listFont.add(map)
        }

        val fontDir = File(EasyDiaryUtils.getApplicationDataDirectory(mContext) + USER_CUSTOM_FONTS_DIRECTORY)
        fontDir.list()?.let {
            for (fontName in it) {
                if (FilenameUtils.getExtension(fontName).equals("ttf", ignoreCase = true)) {
                    val map = HashMap<String, String>()
                    map.put("disPlayFontName", FilenameUtils.getBaseName(fontName))
                    map.put("fontName", fontName)
                    listFont.add(map)
                }
            }
        }

        listFont.mapIndexed { index, map ->
            if (mContext.config.settingFontName == map["fontName"]) selectedIndex = index
        }

        val arrayAdapter = FontItemAdapter(activity!!, R.layout.item_font, listFont)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            fontInfo["fontName"]?.let {
                mContext.config.settingFontName = it
                FontUtils.setCommonTypeface(mContext, mContext.assets)
                initPreference()
                setFontsStyle()
            }
            mAlertDialog?.cancel()
        }

        builder.setView(fontView)
        mAlertDialog = builder.create()
        mAlertDialog?.show()
        listView.setSelection(selectedIndex)
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(mContext, mContext.assets, null, mRootView)
    }
}