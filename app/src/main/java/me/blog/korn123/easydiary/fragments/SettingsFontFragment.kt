package me.blog.korn123.easydiary.fragments

import android.app.Activity
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
import kotlinx.android.synthetic.main.layout_settings_font.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.SettingsActivity
import me.blog.korn123.easydiary.adapters.FontItemAdapter
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

class SettingsFontFragment : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var progressContainer: ConstraintLayout
    private lateinit var mRootView: ViewGroup
    private val mActivity: Activity
        get() = activity!!


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.layout_settings_font, container, false) as ViewGroup
        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        progressContainer = mActivity.findViewById(R.id.progressContainer)
        mActivity.run {
            changeDrawableIconColor(config.textColor, R.drawable.minus_6)
            changeDrawableIconColor(config.textColor, R.drawable.plus_6)
        }
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
                    REQUEST_CODE_FONT_PICK -> {
                        intent.data?.let { uri ->
                            val fileName = EasyDiaryUtils.queryName(mActivity.contentResolver, uri)
                            if (FilenameUtils.getExtension(fileName).equals("ttf", true)) {
                                Thread(Runnable {
                                    val inputStream = mActivity.contentResolver.openInputStream(uri)
                                    val fontDestDir = File(EasyDiaryUtils.getApplicationDataDirectory(mActivity) + USER_CUSTOM_FONTS_DIRECTORY)
                                    FileUtils.copyToFile(inputStream, File(fontDestDir, fileName))
                                    mActivity.runOnUiThread{
                                        progressContainer.visibility = View.GONE
                                        mActivity.showAlertDialog("${FilenameUtils.getBaseName(fileName)} font file is registered.", null)
                                    }
                                }).start()
                                progressContainer.visibility = View.VISIBLE
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
                REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING -> if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
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
        mActivity.run {
            when (view.id) {
                R.id.fontSetting -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    openFontSettingDialog()
                } else {
                    confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING)
                }
                R.id.decreaseFont -> {
                    config.settingFontSize = config.settingFontSize - 5
                    initTextSize(mRootView)
                }
                R.id.increaseFont -> {
                    config.settingFontSize = config.settingFontSize + 5
                    initTextSize(mRootView)
                }
                R.id.calendarFontScale -> {
                    openCalendarFontScaleDialog()
                }
                R.id.addTtfFontSetting -> {
//                openGuideView(getString(R.string.add_ttf_fonts_title))
                    performFileSearch()
                }
                R.id.boldStyleOption -> {
                    boldStyleOptionSwitcher.toggle()
                    config.boldStyleEnable = boldStyleOptionSwitcher.isChecked
                }
            }
        }
    }

    private fun bindEvent() {
        fontSetting.setOnClickListener(mOnClickListener)
        fontLineSpacing.configBuilder
                .min(0.2F)
                .max(1.8F)
                .progress(mActivity.config.lineSpacingScaleFactor)
                .floatType()
                .secondTrackColor(config.textColor)
                .trackColor(config.textColor)
                .sectionCount(16)
                .sectionTextInterval(2)
                .showSectionText()
                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                .autoAdjustSectionMark()
                .build()
        val bubbleSeekBarListener = object : BubbleSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
                Log.i("progress", "$progress $progressFloat")
                mActivity.config.lineSpacingScaleFactor = progressFloat
                setFontsStyle()
                Log.i("progress", "${mActivity.config.lineSpacingScaleFactor}")
            }
            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {}
            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {}
        }
        fontLineSpacing.setOnProgressChangedListener(bubbleSeekBarListener)
        decreaseFont.setOnClickListener(mOnClickListener)
        increaseFont.setOnClickListener(mOnClickListener)
        calendarFontScale.setOnClickListener(mOnClickListener)
        addTtfFontSetting.setOnClickListener(mOnClickListener)
        boldStyleOption.setOnClickListener(mOnClickListener)
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
        fontSettingSummary.text = FontUtils.fontFileNameToDisplayName(mActivity, mActivity.config.settingFontName)
        calendarFontScaleDescription.text = when (mActivity.config.settingCalendarFontScale) {
            DEFAULT_CALENDAR_FONT_SCALE -> getString(R.string.calendar_font_scale_disable)
            else -> getString(R.string.calendar_font_scale_factor, mActivity.config.settingCalendarFontScale)
        }
        boldStyleOptionSwitcher.isChecked = mActivity.config.boldStyleEnable
    }

    /**
     *  Float Formatting depends on Locale
     *  https://stackoverflow.com/questions/44541638/java-lang-numberformatexception-while-executing-in-france-machine
     */
    private fun openCalendarFontScaleDialog() {
        var alertDialog: AlertDialog? = null
        val builder = AlertDialog.Builder(mActivity)
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        val inflater = mActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            if (mActivity.config.settingCalendarFontScale == fontScale.toFloat()) selectedIndex = index
        }

        val arrayAdapter = OptionItemAdapter(mActivity, R.layout.item_check_label, listFontScale, mActivity.config.settingCalendarFontScale)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            fontInfo["optionValue"]?.let {
                mActivity.config.settingCalendarFontScale = it.toFloat()
            }
            alertDialog?.cancel()
            initPreference()
        }

        alertDialog = builder.create().apply { mActivity.updateAlertDialog(this, null, containerView, getString(R.string.calendar_font_scale_title)) }
        listView.setSelection(selectedIndex)
    }

    private fun openFontSettingDialog() {
        var alertDialog: AlertDialog? = null
        EasyDiaryUtils.initWorkingDirectory(mActivity)
        val builder = AlertDialog.Builder(mActivity)
        builder.setNegativeButton(getString(android.R.string.cancel), null)
//        builder.setTitle(getString(R.string.font_setting))
        val inflater = mActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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

        val fontDir = File(EasyDiaryUtils.getApplicationDataDirectory(mActivity) + USER_CUSTOM_FONTS_DIRECTORY)
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
            if (mActivity.config.settingFontName == map["fontName"]) selectedIndex = index
        }

        val arrayAdapter = FontItemAdapter(activity!!, R.layout.item_check_label, listFont)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            fontInfo["fontName"]?.let {
                mActivity.config.settingFontName = it
                FontUtils.setCommonTypeface(mActivity)
                initPreference()
                setFontsStyle()

                (mActivity as SettingsActivity).run {
                    pauseLock()
                    updateUI()
                }
            }
            alertDialog?.cancel()
        }

        alertDialog = builder.create().apply { mActivity.updateAlertDialog(this, null, fontView, getString(R.string.font_setting)) }
        listView.setSelection(selectedIndex)
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(mActivity, mActivity.assets, null, mRootView)
    }
}