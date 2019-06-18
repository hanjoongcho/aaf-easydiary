package me.blog.korn123.easydiary.activities

import android.accounts.Account
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
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
import com.xw.repo.BubbleSeekBar
import io.github.aafactory.commons.helpers.BaseConfig
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_settings.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.FontItemAdapter
import me.blog.korn123.easydiary.adapters.RealmFileItemAdapter
import me.blog.korn123.easydiary.adapters.ThumbnailSizeItemAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.services.BackupPhotoService
import me.blog.korn123.easydiary.services.RecoverPhotoService
import org.apache.commons.io.FilenameUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors


/**
 * Created by CHO HANJOONG on 2017-11-04.
 */

class SettingsActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mAccountCallback: (Account) -> Unit
    private var mAlertDialog: AlertDialog? = null
    private var mTaskFlag = 0
    private var mDevModeClickCount = 0
    

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setTitle(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }

        bindEvent()
        EasyDiaryUtils.changeDrawableIconColor(this, config.primaryColor, R.drawable.minus_6)
        EasyDiaryUtils.changeDrawableIconColor(this, config.primaryColor, R.drawable.plus_6)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pauseLock()

        when (resultCode == Activity.RESULT_OK && data != null) {
            true -> {
                // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
                when (requestCode) {
                    REQUEST_CODE_GOOGLE_SIGN_IN -> {
                        // The Task returned from this call is always completed, no need to attach
                        // a listener.
                        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                        val googleSignAccount = task.getResult(ApiException::class.java)
                        googleSignAccount.account?.let {
                            requestDrivePermissions(it) { mAccountCallback.invoke(it) }
                        }
                    }
                    REQUEST_CODE_GOOGLE_DRIVE_PERMISSIONS -> {
                        mPermissionCallback.invoke()                        
                    }       
                }
            }
            false -> {
                makeSnackBar("Google account verification failed.")
                progressContainer.visibility = View. GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initPreference()
        setFontsStyle()
        setupInvite()
        
        if (BaseConfig(this).isThemeChanged) {
            BaseConfig(this).isThemeChanged = false
            val readDiaryIntent = Intent(this, DiaryMainActivity::class.java)
            readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(readDiaryIntent)
            this.overridePendingTransition(0, 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        pauseLock()
        if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            when (requestCode) {
                REQUEST_CODE_EXTERNAL_STORAGE -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    when (mTaskFlag) {
                        SETTING_FLAG_EXPORT_GOOGLE_DRIVE -> backupDiaryRealm()
                        SETTING_FLAG_IMPORT_GOOGLE_DRIVE -> recoverDiaryRealm()
                        SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE -> backupDiaryPhoto()
                        SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE -> recoverDiaryPhoto()
                    }
                }
                REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    openFontSettingDialog()
                }
                REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_EXCEL -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    exportExcel()
                }
            }
        } else {
            makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
        }
    }

    
    /***************************************************************************************************
     *   backup and recovery
     *
     ***************************************************************************************************/
    private fun initGoogleSignAccount(callback: (account: Account) -> Unit) {
        mAccountCallback = callback

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

        if (googleSignInAccount == null) {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.oauth_requerst_id_token))
                    .requestEmail()
                    .build()
            val client = GoogleSignIn.getClient(this, gso)
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
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))
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
        val driveServiceHelper = DriveServiceHelper(this, account)
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
                val driveServiceHelper = DriveServiceHelper(this, account)
                driveServiceHelper.createFile(
                        it, EasyDiaryDbHelper.getInstance().path,
                        DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
                        EasyDiaryUtils.easyDiaryMimeType
                ).addOnSuccessListener {
                    progressContainer.visibility = View. GONE
                    makeSnackBar(getString(R.string.backup_completed_message))
                }.addOnFailureListener { e ->
                    makeSnackBar(e.message ?: "Please try again later.")
                    progressContainer.visibility = View.GONE
                }
            }
        }
    }

    private fun backupDiaryPhoto() {
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount { account ->
            initDriveWorkingDirectory(account, DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                progressContainer.visibility = View.GONE
                showAlertDialog(getString(R.string.backup_confirm_message), DialogInterface.OnClickListener {_, _ ->
                    val backupPhotoService = Intent(this, BackupPhotoService::class.java)
                    backupPhotoService.putExtra(DriveServiceHelper.WORKING_FOLDER_ID, photoFolderId)
                    startService(backupPhotoService)
                    finish()
                }, null)
            }
        }
    }

    private fun recoverDiaryRealm() {
        progressContainer.visibility = View.VISIBLE
        openRealmFilePickerDialog()
    }
    
    private fun recoverDiaryPhoto() {
        initGoogleSignAccount { _ ->
            runOnUiThread {
                showAlertDialog(getString(R.string.recover_confirm_attached_photo), DialogInterface.OnClickListener {_, _ ->
                    val recoverPhotoService = Intent(this, RecoverPhotoService::class.java)
                    startService(recoverPhotoService)
                    finish()
                }, null)
            }
        }
    }

    private fun openRealmFilePickerDialog() {
        initGoogleSignAccount { account ->
            val driveServiceHelper = DriveServiceHelper(this, account)

//            driveServiceHelper.queryFiles("mimeType contains 'text/aaf_v' and name contains '$DIARY_DB_NAME'", 1000)
            driveServiceHelper.queryFiles("mimeType contains 'text/aaf_v' and trashed = false", 1000)
                    .addOnSuccessListener {
                        val realmFiles: ArrayList<HashMap<String, String>> = arrayListOf()
                        it.files.map { file -> 
                            val itemInfo = hashMapOf<String, String>("name" to file.name, "id" to file.id, "createdTime" to file.createdTime.toString())
                            realmFiles.add(itemInfo)
                        }
                        val builder = AlertDialog.Builder(this@SettingsActivity)
                        builder.setNegativeButton(getString(android.R.string.cancel), null)
                        builder.setTitle(getString(R.string.open_realm_file_title))
                        builder.setMessage(getString(R.string.open_realm_file_message))
                        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val fontView = inflater.inflate(R.layout.dialog_realm_files, null)
                        val listView = fontView.findViewById<ListView>(R.id.files)
                        val adapter = RealmFileItemAdapter(this@SettingsActivity, R.layout.item_realm_file, realmFiles)
                        listView.adapter = adapter
                        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            val itemInfo = parent.adapter.getItem(position) as HashMap<String, String>
                            itemInfo["id"]?.let { realmFileId ->
                                progressContainer.visibility = View.VISIBLE
                                driveServiceHelper.downloadFile(realmFileId, EasyDiaryDbHelper.getInstance().path).run {
                                    addOnSuccessListener {
                                        val readDiaryIntent = Intent(this@SettingsActivity, DiaryMainActivity::class.java)
                                        readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        val mPendingIntentId = 123456
                                        val mPendingIntent = PendingIntent.getActivity(this@SettingsActivity, mPendingIntentId, readDiaryIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                                        val mgr = this@SettingsActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
                                        ActivityCompat.finishAffinity(this@SettingsActivity)
                                        //System.runFinalizersOnExit(true)
                                        System.exit(0)
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
                        makeSnackBar(e.message ?: "Please try again later.")
                        progressContainer.visibility = View.GONE
                    }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun bindEvent() {
        primaryColor.setOnClickListener(mOnClickListener)
        fontSetting.setOnClickListener(mOnClickListener)
        thumbnailSetting.setOnClickListener(mOnClickListener)
        sensitiveOption.setOnClickListener(mOnClickListener)
        addTtfFontSetting.setOnClickListener(mOnClickListener)
        appLockSetting.setOnClickListener(mOnClickListener)
        restoreSetting.setOnClickListener(mOnClickListener)
        backupSetting.setOnClickListener(mOnClickListener)
        rateAppSetting.setOnClickListener(mOnClickListener)
        licenseView.setOnClickListener(mOnClickListener)
        restorePhotoSetting.setOnClickListener(mOnClickListener)
        releaseNotes.setOnClickListener(mOnClickListener)
        boldStyleOption.setOnClickListener(mOnClickListener)
        multiPickerOption.setOnClickListener(mOnClickListener)
        backupAttachPhoto.setOnClickListener(mOnClickListener)
        recoverAttachPhoto.setOnClickListener(mOnClickListener)
        fingerprint.setOnClickListener(mOnClickListener)
        enableCardViewPolicy.setOnClickListener(mOnClickListener)
        decreaseFont.setOnClickListener(mOnClickListener)
        increaseFont.setOnClickListener(mOnClickListener)
        contentsSummary.setOnClickListener(mOnClickListener)
        exportExcel.setOnClickListener(mOnClickListener)
        faq.setOnClickListener(mOnClickListener)
        privacyPolicy.setOnClickListener(mOnClickListener)
        signOutGoogleOAuth.setOnClickListener(mOnClickListener)
        devMode.setOnClickListener {
            mDevModeClickCount++
            if (mDevModeClickCount > 5) {
                signOutGoogleOAuth.visibility = View.VISIBLE
                makeSnackBar(BuildConfig.VERSION_CODE.toString())
            }
        }

        fontLineSpacing.configBuilder
                .min(0.2F)
                .max(1.8F)
                .progress(config.lineSpacingScaleFactor)
                .floatType()
                .sectionCount(16)
                .sectionTextInterval(2)
                .showSectionText()
                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                .autoAdjustSectionMark()
                .build()


        val bubbleSeekBarListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
                Log.i("progress", "$progress $progressFloat")
                config.lineSpacingScaleFactor = progressFloat
                setFontsStyle()
                Log.i("progress", "${config.lineSpacingScaleFactor}")
            }
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {}
            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {}
        }
        fontLineSpacing.setOnProgressChangedListener(bubbleSeekBarListener)

        progressContainer.setOnTouchListener { _, _ -> true }
    }
    
    private val mOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.primaryColor -> TransitionHelper.startActivityWithTransition(this@SettingsActivity, Intent(this@SettingsActivity, CustomizationActivity::class.java))
            R.id.fontSetting -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                openFontSettingDialog()
            } else {
                confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING)
            }
            R.id.thumbnailSetting -> {
                openThumbnailSettingDialog()
            }
            R.id.sensitiveOption -> {
                sensitiveOptionSwitcher.toggle()
                config.diarySearchQueryCaseSensitive = sensitiveOptionSwitcher.isChecked
            }
            R.id.addTtfFontSetting -> {
                openGuideView(getString(R.string.add_ttf_fonts_title))
            }
            R.id.restoreSetting -> {
                mTaskFlag = SETTING_FLAG_IMPORT_GOOGLE_DRIVE
                if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    recoverDiaryRealm()
                } else { // Permission has already been granted
                    confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.backupSetting -> {
                mTaskFlag = SETTING_FLAG_EXPORT_GOOGLE_DRIVE
                if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    backupDiaryRealm()
                } else { // Permission has already been granted
                    confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.backupAttachPhoto -> {
                mTaskFlag = SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE
                when (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> backupDiaryPhoto()
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.recoverAttachPhoto -> {
                mTaskFlag = SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE
                when (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> recoverDiaryPhoto()
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.exportExcel -> {
                when (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> exportExcel()
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_EXCEL)
                }
            }
            R.id.restorePhotoSetting -> {
                openGuideView(getString(R.string.restore_photo))
            }
            R.id.rateAppSetting -> openGooglePlayBy("me.blog.korn123.easydiary")
            R.id.licenseView -> {
                TransitionHelper.startActivityWithTransition(this, Intent(this, MarkDownViewActivity::class.java).apply {
                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, "https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/LICENSE.md")
                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, getString(R.string.preferences_information_licenses))
                })
            }
            R.id.releaseNotes -> checkWhatsNewDialog(false)
            R.id.boldStyleOption -> {
                boldStyleOptionSwitcher.toggle()
                config.boldStyleEnable = boldStyleOptionSwitcher.isChecked
            }
            R.id.multiPickerOption -> {
                multiPickerOptionSwitcher.toggle()
                config.multiPickerEnable = multiPickerOptionSwitcher.isChecked
            }
            R.id.appLockSetting -> {
                when (config.aafPinLockEnable) {
                    true -> {
                        if (config.fingerprintLockEnable) {
                            showAlertDialog(getString(R.string.pin_release_need_fingerprint_disable), null)
                        } else {
                            appLockSettingSwitcher.isChecked = false
                            config.aafPinLockEnable = false
                            showAlertDialog(getString(R.string.pin_setting_release), null)
                        }
                    }
                    false -> {
                        startActivity(Intent(this, PinLockActivity::class.java).apply {
                            putExtra(FingerprintLockActivity.LAUNCHING_MODE, PinLockActivity.ACTIVITY_SETTING)
                        })
                    }
                }
            }
            R.id.fingerprint -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    when (config.fingerprintLockEnable) {
                        true -> {
                            fingerprintSwitcher.isChecked = false
                            config.fingerprintLockEnable = false
                            showAlertDialog(getString(R.string.fingerprint_setting_release), null)
                        }
                        false -> {
                            when (config.aafPinLockEnable) {
                                true -> {
                                    startActivity(Intent(this, FingerprintLockActivity::class.java).apply {
                                        putExtra(FingerprintLockActivity.LAUNCHING_MODE, FingerprintLockActivity.ACTIVITY_SETTING)
                                    })
                                }
                                false -> {
                                    showAlertDialog(getString(R.string.fingerprint_lock_need_pin_setting), null)
                                }
                            }
                        }
                    }
                } else {
                    showAlertDialog(getString(R.string.fingerprint_not_available), null)
                }
            }
            R.id.enableCardViewPolicy -> {
                enableCardViewPolicySwitcher.toggle()
                config.enableCardViewPolicy = enableCardViewPolicySwitcher.isChecked
                updateCardViewPolicy(main_holder)
            }
            R.id.decreaseFont -> {
                config.settingFontSize = config.settingFontSize - 5
                initTextSize(main_holder, this)
            }
            R.id.increaseFont -> {
                config.settingFontSize = config.settingFontSize + 5
                initTextSize(main_holder, this)
            }
            R.id.contentsSummary -> {
                contentsSummarySwitcher.toggle()
                config.enableContentsSummary = contentsSummarySwitcher.isChecked
            }
            R.id.faq -> {
                TransitionHelper.startActivityWithTransition(this, Intent(this, MarkDownViewActivity::class.java).apply {
                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, getString(R.string.faq_url))
                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, getString(R.string.faq_title))
                })
            }
            R.id.privacyPolicy -> {
                TransitionHelper.startActivityWithTransition(this, Intent(this, MarkDownViewActivity::class.java).apply {
                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, getString(R.string.privacy_policy_url))
                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, getString(R.string.privacy_policy_title))
                })
            }
            R.id.signOutGoogleOAuth -> {
                // Configure sign-in to request the user's ID, email address, and basic
                // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.oauth_requerst_id_token))
                        .requestEmail()
                        .build()
                val client = GoogleSignIn.getClient(this, gso)
                client.signOut().addOnCompleteListener { makeSnackBar("Sign out complete:)") }
            }
        }
    }
    
    private fun openThumbnailSettingDialog() {
        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.setTitle(getString(R.string.thumbnail_setting_title))
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_thumbnail, null)
        val listView = containerView.findViewById<ListView>(R.id.listView)

        var selectedIndex = 0
        val listThumbnailSize = ArrayList<Map<String, String>>()
        for (i in 40..200 step 10) {
            listThumbnailSize.add(mapOf("optionTitle" to "${i}dp x ${i}dp", "size" to "$i"))
        }
        
        listThumbnailSize.mapIndexed { index, map ->
            val size = map["size"] ?: "0"
            if (config.settingThumbnailSize == size.toFloat()) selectedIndex = index
        }
        
        val arrayAdapter = ThumbnailSizeItemAdapter(this@SettingsActivity, R.layout.item_font, listThumbnailSize)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            fontInfo["size"]?.let {
                config.settingThumbnailSize = it.toFloat()
            }
            mAlertDialog?.cancel()
        }

        builder.setView(containerView)
        mAlertDialog = builder.create()
        mAlertDialog?.show()
        listView.setSelection(selectedIndex)
    }

    private fun openFontSettingDialog() {
        EasyDiaryUtils.initWorkingDirectory(this@SettingsActivity)
        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.setTitle(getString(R.string.font_setting))
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

        val fontDir = File(Environment.getExternalStorageDirectory().absolutePath + USER_CUSTOM_FONTS_DIRECTORY)
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
            if (config.settingFontName == map["fontName"]) selectedIndex = index
        } 
        
        val arrayAdapter = FontItemAdapter(this@SettingsActivity, R.layout.item_font, listFont)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            fontInfo["fontName"]?.let { 
                config.settingFontName = it
                FontUtils.setCommonTypeface(this@SettingsActivity, assets)
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
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<ViewGroup>(android.R.id.content))
    }

    private fun initPreference() {
        fontSettingSummary.text = FontUtils.fontFileNameToDisplayName(applicationContext, config.settingFontName)
        sensitiveOptionSwitcher.isChecked = config.diarySearchQueryCaseSensitive
        appLockSettingSwitcher.isChecked = config.aafPinLockEnable
        rateAppSettingSummary.text = String.format("Easy Diary v %s", BuildConfig.VERSION_NAME)
        boldStyleOptionSwitcher.isChecked = config.boldStyleEnable
        multiPickerOptionSwitcher.isChecked = config.multiPickerEnable
        fingerprintSwitcher.isChecked = config.fingerprintLockEnable
        enableCardViewPolicySwitcher.isChecked = config.enableCardViewPolicy
        contentsSummarySwitcher.isChecked = config.enableContentsSummary
    }

    private fun setupInvite() {
        inviteSummary.text = String.format(getString(R.string.invite_friends_summary), getString(R.string.app_name))
        invite.setOnClickListener {
            val text = String.format(getString(io.github.aafactory.commons.R.string.share_text), getString(R.string.app_name), getStoreUrl())
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(Intent.createChooser(this, getString(io.github.aafactory.commons.R.string.invite_via)))
            }
        }
    }

    private fun exportExcel() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.export_excel_title))
        builder.setIcon(ContextCompat.getDrawable(this, R.drawable.excel_3))
        builder.setCancelable(false)
        val alert = builder.create()
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_export_progress_excel, null)
        val progressInfo = containerView.findViewById<TextView>(R.id.progressInfo)
        var confirmButton = containerView.findViewById<Button>(R.id.confirm)
        progressInfo.text = "Preparing to export..."
        alert.setView(containerView)
        alert.show()

        Thread(Runnable {
            val diaryList = EasyDiaryDbHelper.readDiary(null)
            val wb: Workbook = HSSFWorkbook()
            val sheet = wb.createSheet("new sheet")

            val headerFont = wb.createFont().apply {
                color = IndexedColors.WHITE.index
            }
            val headerStyle = wb.createCellStyle().apply {
                wrapText = true
                fillForegroundColor = IndexedColors.BLUE.index
                fillPattern = CellStyle.SOLID_FOREGROUND
                alignment = CellStyle.ALIGN_CENTER
                verticalAlignment = CellStyle.VERTICAL_CENTER
                setFont(headerFont)
            }
            val bodyStyle = wb.createCellStyle().apply {
                wrapText = true
                verticalAlignment = CellStyle.VERTICAL_TOP
            }

            val headerRow = sheet.createRow(0)
            headerRow.height = 256 * 3
            headerRow.createCell(SEQ).setCellValue(getString(R.string.export_excel_header_seq))
            headerRow.createCell(WRITE_DATE).setCellValue(getString(R.string.export_excel_header_write_date))
            headerRow.createCell(TITLE).setCellValue(getString(R.string.export_excel_header_title))
            headerRow.createCell(CONTENTS).setCellValue(getString(R.string.export_excel_header_contents))
            headerRow.createCell(ATTACH_PHOTO_NAME).setCellValue(getString(R.string.export_excel_header_attach_photo_path))
            headerRow.createCell(ATTACH_PHOTO_SIZE).setCellValue(getString(R.string.export_excel_header_attach_photo_size))
            headerRow.createCell(WRITE_TIME_MILLIS).setCellValue(getString(R.string.export_excel_header_write_time_millis))
            headerRow.createCell(SYMBOL).setCellValue(getString(R.string.export_excel_header_symbol))
            headerRow.createCell(IS_ALL_DAY).setCellValue(getString(R.string.export_excel_header_is_all_day))

            headerRow.getCell(SEQ).cellStyle = headerStyle
            headerRow.getCell(WRITE_DATE).cellStyle = headerStyle
            headerRow.getCell(TITLE).cellStyle = headerStyle
            headerRow.getCell(CONTENTS).cellStyle = headerStyle
            headerRow.getCell(ATTACH_PHOTO_NAME).cellStyle = headerStyle
            headerRow.getCell(ATTACH_PHOTO_SIZE).cellStyle = headerStyle
            headerRow.getCell(WRITE_TIME_MILLIS).cellStyle = headerStyle
            headerRow.getCell(SYMBOL).cellStyle = headerStyle
            headerRow.getCell(IS_ALL_DAY).cellStyle = headerStyle

            // FIXME:
            // https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/Sheet.html#setColumnWidth-int-int-
            sheet.setColumnWidth(SEQ, 256 * 10)
            sheet.setColumnWidth(WRITE_DATE, 256 * 30)
            sheet.setColumnWidth(TITLE, 256 * 30)
            sheet.setColumnWidth(CONTENTS, 256 * 50)
            sheet.setColumnWidth(ATTACH_PHOTO_NAME, 256 * 80)
            sheet.setColumnWidth(ATTACH_PHOTO_SIZE, 256 * 15)
            sheet.setColumnWidth(WRITE_TIME_MILLIS, 256 * 60)
            sheet.setColumnWidth(SYMBOL, 256 * 10)
            sheet.setColumnWidth(IS_ALL_DAY, 256 * 30)
            val exportFileName = "aaf-easydiray_${DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER)}"
            val diarySymbolMap = EasyDiaryUtils.getDiarySymbolMap(this)
            diaryList.forEachIndexed { index, diaryDto ->
                val row = sheet.createRow(index + 1)
                val photoNames = StringBuffer()
                val photoSizes = StringBuffer()
                diaryDto.photoUris?.map {
                    photoNames.append("$DIARY_PHOTO_DIRECTORY${FilenameUtils.getName(it.getFilePath())}\n")
                    photoSizes.append("${File(it.getFilePath()).length() / 1024}\n")
                }

                val sequence = row.createCell(SEQ).apply {cellStyle = bodyStyle}
                val writeDate = row.createCell(WRITE_DATE).apply {cellStyle = bodyStyle}
                val title = row.createCell(TITLE).apply {cellStyle = bodyStyle}
                val contents = row.createCell(CONTENTS).apply {cellStyle = bodyStyle}
                val attachPhotoNames = row.createCell(ATTACH_PHOTO_NAME).apply {cellStyle = bodyStyle}
                val attachPhotoSizes = row.createCell(ATTACH_PHOTO_SIZE).apply {cellStyle = bodyStyle}
                val writeTimeMillis = row.createCell(WRITE_TIME_MILLIS).apply {cellStyle = bodyStyle}
                val weather = row.createCell(SYMBOL).apply {cellStyle = bodyStyle}
                val isAllDay = row.createCell(IS_ALL_DAY).apply {cellStyle = bodyStyle}

                sequence.setCellValue(diaryDto.sequence.toDouble())
                writeDate.setCellValue(DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis))
                title.setCellValue(diaryDto.title)
                contents.setCellValue(diaryDto.contents)
                attachPhotoNames.setCellValue(photoNames.toString())
                attachPhotoSizes.setCellValue(photoSizes.toString())
                writeTimeMillis.setCellValue(diaryDto.currentTimeMillis.toDouble())
                isAllDay.setCellValue(diaryDto.isAllDay)
                weather.setCellValue(diarySymbolMap[diaryDto.weather])

                runOnUiThread {
                    progressInfo.text = "${index.plus(1)} / ${diaryList.size}\n${getString(R.string.export_excel_xls_location)}: ${WORKING_DIRECTORY + exportFileName}.xls"
                }
            }

            val outputStream = FileOutputStream("${Environment.getExternalStorageDirectory().absolutePath + WORKING_DIRECTORY + exportFileName}.xls")
            wb.write(outputStream)
            outputStream.close()
            runOnUiThread {
                confirmButton.visibility = View.VISIBLE
                confirmButton.setOnClickListener { alert.cancel() }
            }
        }).start()
    }

    private fun openGuideView(title: String) {
        TransitionHelper.startActivityWithTransition(this, Intent(this, MarkDownViewActivity::class.java).apply {
            putExtra(MarkDownViewActivity.OPEN_URL_INFO, getString(R.string.add_ttf_fonts_info_url))
            putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, title)
        })
    }

    private fun getStoreUrl() = "https://play.google.com/store/apps/details?id=$packageName"

    companion object {
        const val SEQ = 0
        const val WRITE_DATE = 1
        const val TITLE = 2
        const val CONTENTS = 3
        const val ATTACH_PHOTO_NAME = 4
        const val ATTACH_PHOTO_SIZE = 5
        const val WEATHER = 6  /* no longer used since version 1.4.79 */
        const val SYMBOL = 6
        const val IS_ALL_DAY = 7
        const val WRITE_TIME_MILLIS = 8
    }
}