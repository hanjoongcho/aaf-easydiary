package me.blog.korn123.easydiary.fragments

import android.app.Activity
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
import kotlinx.android.synthetic.main.layout_settings_basic.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.CustomizationActivity
import me.blog.korn123.easydiary.activities.EasyDiaryActivity
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import java.util.*

class SettingsBasicFragment() : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var progressContainer: ConstraintLayout
    private lateinit var mRootView: ViewGroup
    private val mActivity: Activity get() = activity!!


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
        progressContainer = mActivity.findViewById(R.id.progressContainer)

        bindEvent()
        updateFragmentUI(mRootView)
        initPreference()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mRootView)
        initPreference()
        if (mActivity.config.isThemeChanged) {
            mActivity.config.isThemeChanged = false
            mActivity.startMainActivityWithClearTask()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        mActivity.run {
            pauseLock()

            when (requestCode) {
                REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS -> {
                    makeSnackBar(if (isLocationEnabled()) "GPS provider setting is activated." else "The request operation did not complete normally.")
                }
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        mActivity.run {
            when (view.id) {
                R.id.primaryColor -> TransitionHelper.startActivityWithTransition(this, Intent(this, CustomizationActivity::class.java))
                R.id.thumbnailSetting -> {
                    openThumbnailSettingDialog()
                }
                R.id.contentsSummary -> {
                    contentsSummarySwitcher.toggle()
                    config.enableContentsSummary = contentsSummarySwitcher.isChecked
                    maxLines.visibility = if (contentsSummarySwitcher.isChecked) View.VISIBLE else View.GONE
                }
                R.id.enableCardViewPolicy -> {
                    enableCardViewPolicySwitcher.toggle()
                    config.enableCardViewPolicy = enableCardViewPolicySwitcher.isChecked
                    updateCardViewPolicy(mRootView)
                }
                R.id.multiPickerOption -> {
                    multiPickerOptionSwitcher.toggle()
                    config.multiPickerEnable = multiPickerOptionSwitcher.isChecked
                }
                R.id.sensitiveOption -> {
                    sensitiveOptionSwitcher.toggle()
                    config.diarySearchQueryCaseSensitive = sensitiveOptionSwitcher.isChecked
                }
                R.id.countCharacters -> {
                    countCharactersSwitcher.toggle()
                    config.enableCountCharacters = countCharactersSwitcher.isChecked
                }
                R.id.locationInfo -> {
                    locationInfoSwitcher.toggle()
                    when (locationInfoSwitcher.isChecked) {
                        true -> {
                            run {
                                when (hasGPSPermissions()) {
                                    true -> {
                                        config.enableLocationInfo = locationInfoSwitcher.isChecked
                                    }
                                    false -> {
                                        locationInfoSwitcher.isChecked = false
                                        if (this is EasyDiaryActivity) {
                                            acquireGPSPermissions {
                                                locationInfoSwitcher.isChecked = true
                                                config.enableLocationInfo = locationInfoSwitcher.isChecked
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        false -> {
                            config.enableLocationInfo = locationInfoSwitcher.isChecked
                        }
                    }
                }
                R.id.holdPositionEnterEditScreen -> {
                    holdPositionSwitcher.toggle()
                    config.holdPositionEnterEditScreen = holdPositionSwitcher.isChecked
                }
                R.id.maxLines -> {
                    openMaxLinesSettingDialog()
                }
                R.id.taskSymbolTopOrder -> {
                    taskSymbolTopOrderSwitcher.toggle()
                    config.enableTaskSymbolTopOrder = taskSymbolTopOrderSwitcher.isChecked
                }
            }
        }
    }

    private fun bindEvent() {
        primaryColor.setOnClickListener(mOnClickListener)
        thumbnailSetting.setOnClickListener(mOnClickListener)
        contentsSummary.setOnClickListener(mOnClickListener)
        enableCardViewPolicy.setOnClickListener(mOnClickListener)
        multiPickerOption.setOnClickListener(mOnClickListener)
        sensitiveOption.setOnClickListener(mOnClickListener)
        maxLines.setOnClickListener(mOnClickListener)
        calendarStartDay.setOnCheckedChangeListener { _, i ->
            mActivity.config.calendarStartDay = when (i) {
                R.id.startMonday -> CALENDAR_START_DAY_MONDAY
//                R.id.startTuesday -> CALENDAR_START_DAY_TUESDAY
//                R.id.startWednesday -> CALENDAR_START_DAY_WEDNESDAY
//                R.id.startThursday -> CALENDAR_START_DAY_THURSDAY
//                R.id.startFriday -> CALENDAR_START_DAY_FRIDAY
                R.id.startSaturday -> CALENDAR_START_DAY_SATURDAY
                else -> CALENDAR_START_DAY_SUNDAY
            }
        }
        calendarSorting.setOnCheckedChangeListener { _, i ->
            mActivity.config.calendarSorting = when (i) {
                R.id.ascending -> CALENDAR_SORTING_ASC
                else -> CALENDAR_SORTING_DESC
            }
        }
        countCharacters.setOnClickListener(mOnClickListener)
        locationInfo.setOnClickListener(mOnClickListener)
        holdPositionEnterEditScreen.setOnClickListener(mOnClickListener)
        taskSymbolTopOrder.setOnClickListener(mOnClickListener)
    }

    private fun initPreference() {
        sensitiveOptionSwitcher.isChecked = mActivity.config.diarySearchQueryCaseSensitive
        multiPickerOptionSwitcher.isChecked = mActivity.config.multiPickerEnable
        enableCardViewPolicySwitcher.isChecked = mActivity.config.enableCardViewPolicy
        contentsSummarySwitcher.isChecked = mActivity.config.enableContentsSummary
        countCharactersSwitcher.isChecked = mActivity.config.enableCountCharacters
        locationInfoSwitcher.isChecked = mActivity.config.enableLocationInfo
        taskSymbolTopOrderSwitcher.isChecked = mActivity.config.enableTaskSymbolTopOrder

        when (mActivity.config.calendarStartDay) {
            CALENDAR_START_DAY_MONDAY -> startMonday.isChecked = true
            CALENDAR_START_DAY_SATURDAY -> startSaturday.isChecked = true
            else -> startSunday.isChecked = true
        }
        when (mActivity.config.calendarSorting) {
            CALENDAR_SORTING_ASC -> ascending.isChecked = true
            CALENDAR_SORTING_DESC -> descending.isChecked = true
        }
        holdPositionSwitcher.isChecked = mActivity.config.holdPositionEnterEditScreen
        thumbnailSettingDescription.text = "${mActivity.config.settingThumbnailSize.toInt()}dp x ${mActivity.config.settingThumbnailSize.toInt()}dp"
        maxLines.visibility = if (contentsSummarySwitcher.isChecked) View.VISIBLE else View.GONE
        maxLinesValue.text = getString(R.string.max_lines_value, mActivity.config.summaryMaxLines)
    }

    private fun openThumbnailSettingDialog() {
        var alertDialog: AlertDialog? = null
        val builder = AlertDialog.Builder(mActivity)
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        val inflater = mActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_option_item, null)
        val listView = containerView.findViewById<ListView>(R.id.listView)

        var selectedIndex = 0
        val listThumbnailSize = ArrayList<Map<String, String>>()
        for (i in 40..200 step 10) {
            listThumbnailSize.add(mapOf("optionTitle" to "${i}dp x ${i}dp", "optionValue" to "$i"))
        }

        listThumbnailSize.mapIndexed { index, map ->
            val size = map["optionValue"] ?: "0"
            if (mActivity.config.settingThumbnailSize == size.toFloat()) selectedIndex = index
        }

        val arrayAdapter = OptionItemAdapter(mActivity, R.layout.item_check_label, listThumbnailSize, mActivity.config.settingThumbnailSize)
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            @Suppress("UNCHECKED_CAST") val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
            fontInfo["optionValue"]?.let {
                mActivity.config.settingThumbnailSize = it.toFloat()
                initPreference()
            }
            alertDialog?.cancel()
        }
        alertDialog = builder.create().apply { mActivity.updateAlertDialog(this, null, containerView, getString(R.string.thumbnail_setting_title)) }

        listView.setSelection(selectedIndex)
    }

    private fun openMaxLinesSettingDialog() {
        var alertDialog: AlertDialog? = null
        val builder = AlertDialog.Builder(mActivity)
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        val inflater = mActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_option_item, null)
        val listView = containerView.findViewById<ListView>(R.id.listView)

        var selectedIndex = 0
        val listMaxLines = ArrayList<Map<String, String>>()
        for (i in 1..20) {
            listMaxLines.add(mapOf("optionTitle" to getString(R.string.max_lines_value, i), "optionValue" to "$i"))
        }

        listMaxLines.mapIndexed { index, map ->
            val size = map["optionValue"] ?: 0
            if (mActivity.config.summaryMaxLines == size) selectedIndex = index
        }

        val arrayAdapter = OptionItemAdapter(mActivity, R.layout.item_check_label, listMaxLines, mActivity.config.summaryMaxLines.toFloat())
        listView.adapter = arrayAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            @Suppress("UNCHECKED_CAST") val optionInfo = parent.adapter.getItem(position) as HashMap<String, String>
            optionInfo["optionValue"]?.let {
                mActivity.config.summaryMaxLines = it.toInt()
                initPreference()
            }
            alertDialog?.cancel()
        }

        alertDialog = builder.create().apply { mActivity.updateAlertDialog(this, null, containerView, getString(R.string.max_lines_title)) }
        listView.setSelection(selectedIndex)
    }
}