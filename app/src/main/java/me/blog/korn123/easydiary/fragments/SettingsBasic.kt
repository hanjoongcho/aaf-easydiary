package me.blog.korn123.easydiary.fragments

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.xw.repo.BubbleSeekBar
import io.github.aafactory.commons.extensions.updateAppViews
import io.github.aafactory.commons.extensions.updateTextColors
import io.github.aafactory.commons.helpers.BaseConfig
import kotlinx.android.synthetic.main.layout_settings_basic.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.CustomizationActivity
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.adapters.FontItemAdapter
import me.blog.korn123.easydiary.adapters.RealmFileItemAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class SettingsBasic() : androidx.fragment.app.Fragment() {


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
    private var mTaskFlag = 0


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.layout_settings_basic, container, false) as ViewGroup
        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mContext = context!!
        mActivity = activity!!
        progressContainer = mActivity.findViewById(R.id.progressContainer)

        EasyDiaryUtils.changeDrawableIconColor(mContext, mContext.config.primaryColor, R.drawable.minus_6)
        EasyDiaryUtils.changeDrawableIconColor(mContext, mContext.config.primaryColor, R.drawable.plus_6)
        bindEvent()
    }

    override fun onResume() {
        super.onResume()
        initPreference()
        mRootView.let {
            context?.run {
                initTextSize(it, this)
                updateTextColors(it,0,0)
                updateAppViews(it)
                updateCardViewPolicy(it)
            }
        }

        if (BaseConfig(mContext).isThemeChanged) {
            BaseConfig(mContext).isThemeChanged = false
            val readDiaryIntent = Intent(mContext, DiaryMainActivity::class.java)
            readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(readDiaryIntent)
            mActivity.overridePendingTransition(0, 0)
        }
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

    /***************************************************************************************************
     *   backup and recovery
     *
     ***************************************************************************************************/
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


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.primaryColor -> TransitionHelper.startActivityWithTransition(mActivity, Intent(mActivity, CustomizationActivity::class.java))
            R.id.fontSetting -> if (mContext.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                openFontSettingDialog()
            } else {
                mActivity.confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING)
            }
//            R.id.thumbnailSetting -> {
//                openThumbnailSettingDialog()
//            }
//            R.id.sensitiveOption -> {
//                sensitiveOptionSwitcher.toggle()
//                mContext.config.diarySearchQueryCaseSensitive = sensitiveOptionSwitcher.isChecked
//            }
//            R.id.addTtfFontSetting -> {
////                openGuideView(getString(R.string.add_ttf_fonts_title))
//                performFileSearch()
//            }
//            R.id.restoreSetting -> {
//                mTaskFlag = SETTING_FLAG_IMPORT_GOOGLE_DRIVE
//                if (mContext.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
//                    recoverDiaryRealm()
//                } else { // Permission has already been granted
//                    mActivity.confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
//                }
//            }
//            R.id.backupSetting -> {
//                mTaskFlag = SETTING_FLAG_EXPORT_GOOGLE_DRIVE
//                if (mContext.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
//                    backupDiaryRealm()
//                } else { // Permission has already been granted
//                    confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
//                }
//            }
//            R.id.backupAttachPhoto -> {
//                mTaskFlag = SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE
//                when (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
//                    true -> backupDiaryPhoto()
//                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
//                }
//            }
//            R.id.recoverAttachPhoto -> {
//                mTaskFlag = SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE
//                when (mContext.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
//                    true -> recoverDiaryPhoto()
//                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
//                }
//            }
//            R.id.exportExcel -> {
//                when (mContext.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
//                    true -> exportExcel()
//                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_EXCEL)
//                }
//            }
//            R.id.sendEmailWithExcel -> {
//                sendEmailWithExcel()
//            }
//            R.id.exportRealmFile -> {
//                when (mContext.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
//                    true -> exportRealmFile()
//                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM)
//                }
//            }
//            R.id.importRealmFile -> {
//                when (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
//                    true -> importRealmFile()
//                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM)
//                }
//            }
//            R.id.rateAppSetting -> openGooglePlayBy("me.blog.korn123.easydiary")
//            R.id.licenseView -> {
//                TransitionHelper.startActivityWithTransition(this, Intent(this, MarkDownViewActivity::class.java).apply {
//                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, "https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/LICENSE.md")
//                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, getString(R.string.preferences_information_licenses))
//                })
//            }
//            R.id.releaseNotes -> checkWhatsNewDialog(false)
//            R.id.boldStyleOption -> {
//                boldStyleOptionSwitcher.toggle()
//                mContext.config.boldStyleEnable = boldStyleOptionSwitcher.isChecked
//            }
//            R.id.multiPickerOption -> {
//                multiPickerOptionSwitcher.toggle()
//                mContext.config.multiPickerEnable = multiPickerOptionSwitcher.isChecked
//            }
//            R.id.appLockSetting -> {
//                when (mContext.config.aafPinLockEnable) {
//                    true -> {
//                        if (mContext.config.fingerprintLockEnable) {
//                            showAlertDialog(getString(R.string.pin_release_need_fingerprint_disable), null)
//                        } else {
//                            appLockSettingSwitcher.isChecked = false
//                            mContext.config.aafPinLockEnable = false
//                            showAlertDialog(getString(R.string.pin_setting_release), null)
//                        }
//                    }
//                    false -> {
//                        startActivity(Intent(this, PinLockActivity::class.java).apply {
//                            putExtra(FingerprintLockActivity.LAUNCHING_MODE, PinLockActivity.ACTIVITY_SETTING)
//                        })
//                    }
//                }
//            }
//            R.id.fingerprint -> {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    when (mContext.config.fingerprintLockEnable) {
//                        true -> {
//                            fingerprintSwitcher.isChecked = false
//                            mContext.config.fingerprintLockEnable = false
//                            showAlertDialog(getString(R.string.fingerprint_setting_release), null)
//                        }
//                        false -> {
//                            when (mContext.config.aafPinLockEnable) {
//                                true -> {
//                                    startActivity(Intent(this, FingerprintLockActivity::class.java).apply {
//                                        putExtra(FingerprintLockActivity.LAUNCHING_MODE, FingerprintLockActivity.ACTIVITY_SETTING)
//                                    })
//                                }
//                                false -> {
//                                    showAlertDialog(getString(R.string.fingerprint_lock_need_pin_setting), null)
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    showAlertDialog(getString(R.string.fingerprint_not_available), null)
//                }
//            }
//            R.id.enableCardViewPolicy -> {
//                enableCardViewPolicySwitcher.toggle()
//                mContext.config.enableCardViewPolicy = enableCardViewPolicySwitcher.isChecked
//                updateCardViewPolicy(main_holder)
//            }
//            R.id.decreaseFont -> {
//                mContext.config.settingFontSize = mContext.config.settingFontSize - 5
//                initTextSize(main_holder, this)
//            }
//            R.id.increaseFont -> {
//                mContext.config.settingFontSize = mContext.config.settingFontSize + 5
//                initTextSize(main_holder, this)
//            }
//            R.id.contentsSummary -> {
//                contentsSummarySwitcher.toggle()
//                mContext.config.enableContentsSummary = contentsSummarySwitcher.isChecked
//            }
//            R.id.faq -> {
//                TransitionHelper.startActivityWithTransition(this, Intent(this, MarkDownViewActivity::class.java).apply {
//                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, getString(R.string.faq_url))
//                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, getString(R.string.faq_title))
//                })
//            }
//            R.id.privacyPolicy -> {
//                TransitionHelper.startActivityWithTransition(this, Intent(this, MarkDownViewActivity::class.java).apply {
//                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, getString(R.string.privacy_policy_url))
//                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, getString(R.string.privacy_policy_title))
//                })
//            }
//            R.id.signOutGoogleOAuth -> {
//                signOutGoogleOAuth()
//            }
//            R.id.countCharacters -> {
//                countCharactersSwitcher.toggle()
//                mContext.config.enableCountCharacters = countCharactersSwitcher.isChecked
//            }
        }
    }

    private fun bindEvent() {
        primaryColor.setOnClickListener(mOnClickListener)
        fontSetting.setOnClickListener(mOnClickListener)
        thumbnailSetting.setOnClickListener(mOnClickListener)
        sensitiveOption.setOnClickListener(mOnClickListener)
        addTtfFontSetting.setOnClickListener(mOnClickListener)
        boldStyleOption.setOnClickListener(mOnClickListener)
        multiPickerOption.setOnClickListener(mOnClickListener)
        enableCardViewPolicy.setOnClickListener(mOnClickListener)
        decreaseFont.setOnClickListener(mOnClickListener)
        increaseFont.setOnClickListener(mOnClickListener)
        contentsSummary.setOnClickListener(mOnClickListener)
        signOutGoogleOAuth.setOnClickListener(mOnClickListener)
        countCharacters.setOnClickListener(mOnClickListener)
//        devMode.setOnClickListener {
//            mDevModeClickCount++
//            if (mDevModeClickCount > 5) {
//                signOutGoogleOAuth.visibility = View.VISIBLE
//                makeSnackBar(BuildConfig.VERSION_CODE.toString())
//            }
//        }

        fontLineSpacing.configBuilder
                .min(0.2F)
                .max(1.8F)
                .progress(mContext.config.lineSpacingScaleFactor)
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
                mContext.config.lineSpacingScaleFactor = progressFloat
                setFontsStyle()
                Log.i("progress", "${mContext.config.lineSpacingScaleFactor}")
            }
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {}
            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {}
        }
        fontLineSpacing.setOnProgressChangedListener(bubbleSeekBarListener)

        progressContainer.setOnTouchListener { _, _ -> true }

        calendarStartDay.setOnCheckedChangeListener { _, i ->
            val flag = when (i) {
                R.id.startMonday -> CALENDAR_START_DAY_MONDAY
//                R.id.startTuesday -> CALENDAR_START_DAY_TUESDAY
//                R.id.startWednesday -> CALENDAR_START_DAY_WEDNESDAY
//                R.id.startThursday -> CALENDAR_START_DAY_THURSDAY
//                R.id.startFriday -> CALENDAR_START_DAY_FRIDAY
                R.id.startSaturday -> CALENDAR_START_DAY_SATURDAY
                else -> CALENDAR_START_DAY_SUNDAY
            }
            mContext.config.calendarStartDay = flag
        }
    }

    private fun initPreference() {
        fontSettingSummary.text = FontUtils.fontFileNameToDisplayName(mContext, mContext.config.settingFontName)
        sensitiveOptionSwitcher.isChecked = mContext.config.diarySearchQueryCaseSensitive
        boldStyleOptionSwitcher.isChecked = mContext.config.boldStyleEnable
        multiPickerOptionSwitcher.isChecked = mContext.config.multiPickerEnable
        enableCardViewPolicySwitcher.isChecked = mContext.config.enableCardViewPolicy
        contentsSummarySwitcher.isChecked = mContext.config.enableContentsSummary
        countCharactersSwitcher.isChecked = mContext.config.enableCountCharacters
        when (mContext.config.calendarStartDay) {
            CALENDAR_START_DAY_MONDAY -> startMonday.isChecked = true
            CALENDAR_START_DAY_SATURDAY -> startSaturday.isChecked = true
            else -> startSunday.isChecked = true
        }
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