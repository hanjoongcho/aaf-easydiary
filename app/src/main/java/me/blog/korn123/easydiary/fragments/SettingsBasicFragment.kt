package me.blog.korn123.easydiary.fragments

import android.annotation.SuppressLint
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
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.CustomizationActivity
import me.blog.korn123.easydiary.activities.EasyDiaryActivity
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.databinding.PartialSettingsBasicBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import java.util.*

class SettingsBasicFragment : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: PartialSettingsBasicBinding
    private val mRequestLocationSourceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        requireActivity().run {
            makeSnackBar(if (isLocationEnabled()) "GPS provider setting is activated!!!" else "The request operation did not complete normally.")
        }
    }

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = PartialSettingsBasicBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.FLAVOR == "foss") mBinding.enableReviewFlow.visibility = View.GONE
        bindEvent()
        updateFragmentUI(mBinding.root)
        initPreference()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mBinding.root)
        initPreference()
        requireActivity().run {
            if (config.isThemeChanged) {
                config.isThemeChanged = false
                startMainActivityWithClearTask()
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        requireActivity().run activity@ {
            mBinding.run {
                when (view.id) {
                    R.id.primaryColor -> TransitionHelper.startActivityWithTransition(this@activity, Intent(this@activity, CustomizationActivity::class.java))
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
                        updateCardViewPolicy(this.root)
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
                                            if (this@activity is EasyDiaryActivity) {
                                                acquireGPSPermissions(mRequestLocationSourceLauncher) {
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
                    R.id.enableReviewFlow -> {
                        enableReviewFlowSwitcher.toggle()
                        config.enableReviewFlow = enableReviewFlowSwitcher.isChecked
                    }
                }
            }
        }
    }

    private fun bindEvent() {
        mBinding.run {
            primaryColor.setOnClickListener(mOnClickListener)
            thumbnailSetting.setOnClickListener(mOnClickListener)
            contentsSummary.setOnClickListener(mOnClickListener)
            enableCardViewPolicy.setOnClickListener(mOnClickListener)
            multiPickerOption.setOnClickListener(mOnClickListener)
            sensitiveOption.setOnClickListener(mOnClickListener)
            maxLines.setOnClickListener(mOnClickListener)
            countCharacters.setOnClickListener(mOnClickListener)
            locationInfo.setOnClickListener(mOnClickListener)
            holdPositionEnterEditScreen.setOnClickListener(mOnClickListener)
            taskSymbolTopOrder.setOnClickListener(mOnClickListener)
            enableReviewFlow.setOnClickListener(mOnClickListener)
            calendarStartDay.setOnCheckedChangeListener { _, i ->
                requireActivity().config.calendarStartDay = when (i) {
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
                requireActivity().config.calendarSorting = when (i) {
                    R.id.ascending -> CALENDAR_SORTING_ASC
                    else -> CALENDAR_SORTING_DESC
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initPreference() {
        requireActivity().run {
            mBinding.run {
                sensitiveOptionSwitcher.isChecked = config.diarySearchQueryCaseSensitive
                multiPickerOptionSwitcher.isChecked = config.multiPickerEnable
                enableCardViewPolicySwitcher.isChecked = config.enableCardViewPolicy
                contentsSummarySwitcher.isChecked = config.enableContentsSummary
                countCharactersSwitcher.isChecked = config.enableCountCharacters
                locationInfoSwitcher.isChecked = config.enableLocationInfo
                taskSymbolTopOrderSwitcher.isChecked = config.enableTaskSymbolTopOrder
                enableReviewFlowSwitcher.isChecked = config.enableReviewFlow

                when (config.calendarStartDay) {
                    CALENDAR_START_DAY_MONDAY -> startMonday.isChecked = true
                    CALENDAR_START_DAY_SATURDAY -> startSaturday.isChecked = true
                    else -> startSunday.isChecked = true
                }
                when (config.calendarSorting) {
                    CALENDAR_SORTING_ASC -> ascending.isChecked = true
                    CALENDAR_SORTING_DESC -> descending.isChecked = true
                }
                holdPositionSwitcher.isChecked = config.holdPositionEnterEditScreen
                thumbnailSettingDescription.text = "${config.settingThumbnailSize.toInt()}dp x ${config.settingThumbnailSize.toInt()}dp"
                maxLines.visibility = if (contentsSummarySwitcher.isChecked) View.VISIBLE else View.GONE
                maxLinesValue.text = getString(R.string.max_lines_value, config.summaryMaxLines)
            }
        }
    }

    private fun openThumbnailSettingDialog() {
        requireActivity().run {
            var alertDialog: AlertDialog? = null
            val builder = AlertDialog.Builder(this)
            builder.setNegativeButton(getString(android.R.string.cancel), null)
            val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val containerView = inflater.inflate(R.layout.dialog_option_item, mBinding.root, false)
            val listView = containerView.findViewById<ListView>(R.id.listView)

            var selectedIndex = 0
            val listThumbnailSize = ArrayList<Map<String, String>>()
            for (i in 40..200 step 10) {
                listThumbnailSize.add(mapOf("optionTitle" to "${i}dp x ${i}dp", "optionValue" to "$i"))
            }

            listThumbnailSize.mapIndexed { index, map ->
                val size = map["optionValue"] ?: "0"
                if (config.settingThumbnailSize == size.toFloat()) selectedIndex = index
            }

            val arrayAdapter = OptionItemAdapter(this, R.layout.item_check_label, listThumbnailSize, config.settingThumbnailSize)
            listView.adapter = arrayAdapter
            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                @Suppress("UNCHECKED_CAST") val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
                fontInfo["optionValue"]?.let {
                    config.settingThumbnailSize = it.toFloat()
                    initPreference()
                }
                alertDialog?.cancel()
            }
            alertDialog = builder.create().apply { updateAlertDialog(this, null, containerView, getString(R.string.thumbnail_setting_title)) }

            listView.setSelection(selectedIndex)
        }
    }

    private fun openMaxLinesSettingDialog() {
        requireActivity().run {
            var alertDialog: AlertDialog? = null
            val builder = AlertDialog.Builder(this)
            builder.setNegativeButton(getString(android.R.string.cancel), null)
            val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val containerView = inflater.inflate(R.layout.dialog_option_item, mBinding.root, false)
            val listView = containerView.findViewById<ListView>(R.id.listView)

            var selectedIndex = 0
            val listMaxLines = ArrayList<Map<String, String>>()
            for (i in 1..20) {
                listMaxLines.add(mapOf("optionTitle" to getString(R.string.max_lines_value, i), "optionValue" to "$i"))
            }

            listMaxLines.mapIndexed { index, map ->
                val size = map["optionValue"] ?: 0
                if (config.summaryMaxLines == size) selectedIndex = index
            }

            val arrayAdapter = OptionItemAdapter(this, R.layout.item_check_label, listMaxLines, config.summaryMaxLines.toFloat())
            listView.adapter = arrayAdapter
            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                @Suppress("UNCHECKED_CAST") val optionInfo = parent.adapter.getItem(position) as HashMap<String, String>
                optionInfo["optionValue"]?.let {
                    config.summaryMaxLines = it.toInt()
                    initPreference()
                }
                alertDialog?.cancel()
            }

            alertDialog = builder.create().apply { updateAlertDialog(this, null, containerView, getString(R.string.max_lines_title)) }
            listView.setSelection(selectedIndex)
        }
    }
}