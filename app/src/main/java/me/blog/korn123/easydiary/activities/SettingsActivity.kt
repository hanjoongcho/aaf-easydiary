package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.drive.GoogleDriveDownloader
import com.google.android.gms.drive.GoogleDriveUploader
import io.github.hanjoongcho.commons.activities.BaseWebViewActivity
import io.github.hanjoongcho.commons.helpers.BaseConfig
import kotlinx.android.synthetic.main.activity_settings.*
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.constants.Path
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.DialogUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.commons.utils.PermissionUtils
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.FontItemAdapter
import me.blog.korn123.easydiary.extensions.openGooglePlayBy
import me.blog.korn123.easydiary.helper.TransitionHelper
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-11-04.
 */

class SettingsActivity : EasyDiaryActivity() {
    private var mAlertDialog: AlertDialog? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setTitle(R.string.setting_title)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onResume() {
        super.onResume()
        initPreference()
        setFontsStyle()

        if (BaseConfig(this).isThemeChanged) {
            BaseConfig(this).isThemeChanged = false
            val readDiaryIntent = Intent(this, DiaryMainActivity::class.java)
            readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(readDiaryIntent)
            this.overridePendingTransition(0, 0)
        }
    }

    @OnClick(R.id.primaryColor, R.id.fontSetting, R.id.sensitiveOption, R.id.guideView, R.id.appLockSetting, R.id.lockNumberSetting, R.id.restoreSetting, R.id.backupSetting, R.id.rateAppSetting, R.id.licenseView, R.id.easyPhotoMap, R.id.easyPassword)
    fun onClick(view: View) {
        when (view.id) {
            R.id.primaryColor -> TransitionHelper.startActivityWithTransition(this@SettingsActivity, Intent(this@SettingsActivity, CustomizationActivity::class.java))
            R.id.fontSetting -> if (PermissionUtils.checkPermission(this@SettingsActivity, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                openFontSettingDialog()
            } else {
                PermissionUtils.confirmPermission(this@SettingsActivity, this@SettingsActivity, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING)
            }
            R.id.sensitiveOption -> {
                sensitiveOptionSwitcher.toggle()
                CommonUtils.saveBooleanPreference(this@SettingsActivity, Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE, sensitiveOptionSwitcher.isChecked)
            }
            R.id.guideView -> {
                val guideIntent = Intent(this, WebViewActivity::class.java)
                guideIntent.putExtra(BaseWebViewActivity.OPEN_URL_INFO, getString(R.string.add_ttf_fonts_info_url))
                startActivity(guideIntent)
            }
            R.id.appLockSetting -> {
                appLockSettingSwitcher.toggle()
                CommonUtils.saveBooleanPreference(this@SettingsActivity, Constants.APP_LOCK_ENABLE, appLockSettingSwitcher.isChecked)
            }
            R.id.lockNumberSetting -> {
                val lockSettingIntent = Intent(this@SettingsActivity, LockSettingActivity::class.java)
                startActivityForResult(lockSettingIntent, Constants.REQUEST_CODE_LOCK_SETTING)
            }
            R.id.restoreSetting -> {
                mTaskFlag = Constants.SETTING_FLAG_IMPORT_GOOGLE_DRIVE
                if (PermissionUtils.checkPermission(this@SettingsActivity, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                    val downloadIntent = Intent(this@SettingsActivity, GoogleDriveDownloader::class.java)
                    startActivity(downloadIntent)
                } else {
                    // API Level 23 이상이면서 권한취득 안한경우
                    PermissionUtils.confirmPermission(this@SettingsActivity, this@SettingsActivity, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.backupSetting -> {
                mTaskFlag = Constants.SETTING_FLAG_EXPORT_GOOGLE_DRIVE
                if (PermissionUtils.checkPermission(this@SettingsActivity, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                    // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                    val uploadIntent = Intent(this@SettingsActivity, GoogleDriveUploader::class.java)
                    startActivity(uploadIntent)
                } else {
                    // API Level 23 이상이면서 권한취득 안한경우
                    PermissionUtils.confirmPermission(this@SettingsActivity, this@SettingsActivity, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }
            R.id.rateAppSetting -> openGooglePlayBy("me.blog.korn123.easydiary")
            R.id.licenseView -> {
                val licenseIntent = Intent(this, WebViewActivity::class.java)
                licenseIntent.putExtra(BaseWebViewActivity.OPEN_URL_INFO, "https://github.com/hanjoongcho/aaf-easydiary/blob/master/LICENSE.md")
                startActivity(licenseIntent)
            }
            R.id.easyPhotoMap -> openGooglePlayBy("me.blog.korn123.easyphotomap")
            R.id.easyPassword -> openGooglePlayBy("io.github.hanjoongcho.easypassword")
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val password = data.getStringExtra(Constants.APP_LOCK_REQUEST_PASSWORD)
            CommonUtils.saveStringPreference(applicationContext, Constants.APP_LOCK_SAVED_PASSWORD, password)
            lockNumberSettingSummary.text = "${getString(R.string.lock_number)} $password"
        }
        CommonUtils.saveLongPreference(applicationContext, Constants.SETTING_PAUSE_MILLIS, System.currentTimeMillis())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.REQUEST_CODE_EXTERNAL_STORAGE -> if (PermissionUtils.checkPermission(applicationContext, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                // 권한이 있는경우
                if (mTaskFlag == Constants.SETTING_FLAG_EXPORT_GOOGLE_DRIVE) {
                    //                            FileUtils.copyFile(new File(EasyDiaryDbHelper.getRealmInstance().getPath()), new File(Path.WORKING_DIRECTORY + Path.DIARY_DB_NAME));
                    val uploadIntent = Intent(applicationContext, GoogleDriveUploader::class.java)
                    startActivity(uploadIntent)
                } else if (mTaskFlag == Constants.SETTING_FLAG_IMPORT_GOOGLE_DRIVE) {
                    val downloadIntent = Intent(applicationContext, GoogleDriveDownloader::class.java)
                    startActivity(downloadIntent)
                }
            } else {
                // 권한이 없는경우
                DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
            }
            Constants.REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING -> if (PermissionUtils.checkPermission(applicationContext, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                openFontSettingDialog()
            } else {
                DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
            }
            else -> {
            }
        }
    }

    private fun openFontSettingDialog() {
        val builder = AlertDialog.Builder(this@SettingsActivity)
        builder.setNegativeButton("CANCEL", null)
        builder.setTitle(getString(R.string.font_setting))
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val fontView = inflater.inflate(R.layout.dialog_fonts, null)
        val listView = fontView.findViewById<ListView>(R.id.listFont)

        val fontNameArray = resources.getStringArray(R.array.pref_list_fonts_title)
        val fontPathArray = resources.getStringArray(R.array.pref_list_fonts_values)
        val listFont = ArrayList<Map<String, String>>()
        for (i in fontNameArray.indices) {
            val map = HashMap<String, String>()
            map.put("disPlayFontName", fontNameArray[i])
            map.put("fontName", fontPathArray[i])
            listFont.add(map)
        }

        val fontDir = File(Environment.getExternalStorageDirectory().absolutePath + Path.USER_CUSTOM_FONTS_DIRECTORY)
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


        val arrayAdapter = FontItemAdapter(this@SettingsActivity, R.layout.item_font, listFont)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            CommonUtils.saveStringPreference(this@SettingsActivity, Constants.SETTING_FONT_NAME, fontInfo["fontName"])
            FontUtils.setCommonTypeface(this@SettingsActivity, assets)
            initPreference()
            setFontsStyle()
            mAlertDialog?.cancel()
        }

        builder.setView(fontView)
        mAlertDialog = builder.create()
        mAlertDialog?.show()
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<ViewGroup>(android.R.id.content))
    }

    private fun initPreference() {
        fontSettingSummary.text = FontUtils.fontFileNameToDisplayName(applicationContext, CommonUtils.loadStringPreference(this@SettingsActivity, Constants.SETTING_FONT_NAME, Constants.CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT))
        sensitiveOptionSwitcher.isChecked = CommonUtils.loadBooleanPreference(this@SettingsActivity, Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE)
        appLockSettingSwitcher.isChecked = CommonUtils.loadBooleanPreference(this@SettingsActivity, Constants.APP_LOCK_ENABLE)
        lockNumberSettingSummary.text = "${getString(R.string.lock_number)} ${CommonUtils.loadStringPreference(this@SettingsActivity, Constants.APP_LOCK_SAVED_PASSWORD, "0000")}"
        rateAppSettingSummary.text = String.format("Easy Diary v %s", BuildConfig.VERSION_NAME)
    }

    companion object {
        private var mTaskFlag = 0
    }
}