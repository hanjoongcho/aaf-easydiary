package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.xw.repo.BubbleSeekBar
import io.github.aafactory.commons.helpers.BaseConfig
import kotlinx.android.synthetic.main.layout_settings_basic.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.CustomizationActivity
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.activities.SettingsActivity
import me.blog.korn123.easydiary.adapters.FontItemAdapter
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

class SettingsBasicFragment() : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var progressContainer: ConstraintLayout
    private lateinit var mRootView: ViewGroup
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private var mAlertDialog: AlertDialog? = null


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
        updateFragmentUI(mRootView)
        initPreference()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mRootView)
        initPreference()
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
        if (mContext.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            when (requestCode) {
                REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING -> if (mContext.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    openFontSettingDialog()
                }
            }
        } else {
            mActivity.makeSnackBar(mRootView, getString(R.string.guide_message_3))
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.fontSetting -> if (mContext.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                openFontSettingDialog()
            } else {
                confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING)
            }
            R.id.decreaseFont -> {
                mContext.config.settingFontSize = mContext.config.settingFontSize - 5
                mContext.initTextSize(mRootView, mContext)
            }
            R.id.increaseFont -> {
                mContext.config.settingFontSize = mContext.config.settingFontSize + 5
                mContext.initTextSize(mRootView, mContext)
            }
            R.id.calendarFontScale -> {
                openCalendarFontScaleDialog()
            }
            R.id.addTtfFontSetting -> {
//                openGuideView(getString(R.string.add_ttf_fonts_title))
                performFileSearch()
            }
            R.id.primaryColor -> TransitionHelper.startActivityWithTransition(mActivity, Intent(mActivity, CustomizationActivity::class.java))
            R.id.thumbnailSetting -> {
                openThumbnailSettingDialog()
            }
            R.id.contentsSummary -> {
                contentsSummarySwitcher.toggle()
                mContext.config.enableContentsSummary = contentsSummarySwitcher.isChecked
            }
            R.id.enableCardViewPolicy -> {
                enableCardViewPolicySwitcher.toggle()
                mContext.config.enableCardViewPolicy = enableCardViewPolicySwitcher.isChecked
                mContext.updateCardViewPolicy(mRootView)
            }
            R.id.multiPickerOption -> {
                multiPickerOptionSwitcher.toggle()
                mContext.config.multiPickerEnable = multiPickerOptionSwitcher.isChecked
            }
            R.id.boldStyleOption -> {
                boldStyleOptionSwitcher.toggle()
                mContext.config.boldStyleEnable = boldStyleOptionSwitcher.isChecked
            }
            R.id.sensitiveOption -> {
                sensitiveOptionSwitcher.toggle()
                mContext.config.diarySearchQueryCaseSensitive = sensitiveOptionSwitcher.isChecked
            }
            R.id.countCharacters -> {
                countCharactersSwitcher.toggle()
                mContext.config.enableCountCharacters = countCharactersSwitcher.isChecked
            }
        }
    }

    private fun bindEvent() {
        fontSetting.setOnClickListener(mOnClickListener)
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
        decreaseFont.setOnClickListener(mOnClickListener)
        increaseFont.setOnClickListener(mOnClickListener)
        calendarFontScale.setOnClickListener(mOnClickListener)
        addTtfFontSetting.setOnClickListener(mOnClickListener)
        primaryColor.setOnClickListener(mOnClickListener)
        thumbnailSetting.setOnClickListener(mOnClickListener)
        contentsSummary.setOnClickListener(mOnClickListener)
        enableCardViewPolicy.setOnClickListener(mOnClickListener)
        multiPickerOption.setOnClickListener(mOnClickListener)
        boldStyleOption.setOnClickListener(mOnClickListener)
        sensitiveOption.setOnClickListener(mOnClickListener)
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
        countCharacters.setOnClickListener(mOnClickListener)
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    private fun performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        // ACTION_OPEN_DOCUMENT
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
//             addCategory(Intent.CATEGORY_OPENABLE)

            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            type = "*/*"
        }

        startActivityForResult(intent, REQUEST_CODE_FONT_PICK)
    }

    private fun initPreference() {
        fontSettingSummary.text = FontUtils.fontFileNameToDisplayName(mContext, mContext.config.settingFontName)
        calendarFontScaleDescription.text = when (mContext.config.settingCalendarFontScale) {
            DEFAULT_CALENDAR_FONT_SCALE -> getString(R.string.calendar_font_scale_disable)
            else -> getString(R.string.calendar_font_scale_factor, mContext.config.settingCalendarFontScale)
        }
//        calendarFontScaleDescription.setPaintFlags(calendarFontScaleDescription.paintFlags or Paint.UNDERLINE_TEXT_FLAG)
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

    /**
     *  Float Formatting depends on Locale
     *  https://stackoverflow.com/questions/44541638/java-lang-numberformatexception-while-executing-in-france-machine
     */
    private fun openCalendarFontScaleDialog() {
        val builder = AlertDialog.Builder(mContext)
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.setTitle(getString(R.string.calendar_font_scale_title))
        val inflater = mContext.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_option_item, null)
        val listView = containerView.findViewById<ListView>(R.id.listView)

        var selectedIndex = 0
        val listFontScale = ArrayList<Map<String, String>>()
        listFontScale.add(mapOf("optionTitle" to getString(R.string.calendar_font_scale_disable), "optionValue" to "-1"))
        for (i in 1..20 step 1) {
            listFontScale.add(mapOf("optionTitle" to getString(R.string.calendar_font_scale_factor, i * 0.1), "optionValue" to "${i * 0.1F}"))
        }

        listFontScale.mapIndexed { index, map ->
            val fontScale = map["optionValue"] ?: "0"
            if (mContext.config.settingCalendarFontScale == fontScale.toFloat()) selectedIndex = index
        }

        val arrayAdapter = OptionItemAdapter(mActivity, R.layout.item_check_label, listFontScale, mContext.config.settingCalendarFontScale)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            fontInfo["optionValue"]?.let {
                mContext.config.settingCalendarFontScale = it.toFloat()
            }
            mAlertDialog?.cancel()
            initPreference()
        }

        builder.setView(containerView)
        mAlertDialog = builder.create()
        mAlertDialog?.show()
        listView.setSelection(selectedIndex)
    }

    private fun openThumbnailSettingDialog() {
        val builder = AlertDialog.Builder(mContext)
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.setTitle(getString(R.string.thumbnail_setting_title))
        val inflater = mContext.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_option_item, null)
        val listView = containerView.findViewById<ListView>(R.id.listView)

        var selectedIndex = 0
        val listThumbnailSize = ArrayList<Map<String, String>>()
        for (i in 40..200 step 10) {
            listThumbnailSize.add(mapOf("optionTitle" to "${i}dp x ${i}dp", "optionValue" to "$i"))
        }

        listThumbnailSize.mapIndexed { index, map ->
            val size = map["optionValue"] ?: "0"
            if (mContext.config.settingThumbnailSize == size.toFloat()) selectedIndex = index
        }

        val arrayAdapter = OptionItemAdapter(mActivity, R.layout.item_check_label, listThumbnailSize, mContext.config.settingThumbnailSize)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            fontInfo["optionValue"]?.let {
                mContext.config.settingThumbnailSize = it.toFloat()
            }
            mAlertDialog?.cancel()
        }

        builder.setView(containerView)
        mAlertDialog = builder.create()
        mAlertDialog?.show()
        listView.setSelection(selectedIndex)
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

        val arrayAdapter = FontItemAdapter(activity!!, R.layout.item_check_label, listFont)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            fontInfo["fontName"]?.let {
                mContext.config.settingFontName = it
                FontUtils.setCommonTypeface(mContext, mContext.assets)
                initPreference()
                setFontsStyle()

                (mActivity as SettingsActivity).run {
                    pauseLock()
                    updateUI()
                }
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