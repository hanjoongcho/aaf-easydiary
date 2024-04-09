package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityBaseDevBinding
import me.blog.korn123.easydiary.databinding.ActivityBaseSettingsBinding
import me.blog.korn123.easydiary.databinding.ActivityQuickSettingsBinding
import me.blog.korn123.easydiary.extensions.acquireGPSPermissions
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.hasGPSPermissions
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_DESC
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_MONDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SATURDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SUNDAY
import me.blog.korn123.easydiary.helper.TransitionHelper

class QuickSettingsActivity : EasyDiaryActivity() {

    private lateinit var mBinding: ActivityQuickSettingsBinding

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityQuickSettingsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            setTitle("Quick Settings")
            setDisplayHomeAsUpEnabled(true)
        }

        bindEvent()
        initPreference()
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        mBinding.run {
            when (view.id) {
//                R.id.primaryColor -> TransitionHelper.startActivityWithTransition(
//                    this@activity,
//                    Intent(this@activity, CustomizationActivity::class.java)
//                )
//
//                R.id.thumbnailSetting -> {
//                    openThumbnailSettingDialog()
//                }
//
//                cardDatetimeSetting.id -> {
//                    openDatetimeFormattingSettingDialog()
//                }
//
//                R.id.contentsSummary -> {
//                    contentsSummarySwitcher.toggle()
//                    config.enableContentsSummary = contentsSummarySwitcher.isChecked
//                    maxLines.visibility =
//                        if (contentsSummarySwitcher.isChecked) View.VISIBLE else View.GONE
//                }
//
//                R.id.enableCardViewPolicy -> {
//                    enableCardViewPolicySwitcher.toggle()
//                    config.enableCardViewPolicy = enableCardViewPolicySwitcher.isChecked
//                    updateCardViewPolicy(this.root)
//                }
//
//                R.id.multiPickerOption -> {
//                    multiPickerOptionSwitcher.toggle()
//                    config.multiPickerEnable = multiPickerOptionSwitcher.isChecked
//                }
//
//                R.id.sensitiveOption -> {
//                    sensitiveOptionSwitcher.toggle()
//                    config.diarySearchQueryCaseSensitive = sensitiveOptionSwitcher.isChecked
//                }
//
//                R.id.countCharacters -> {
//                    countCharactersSwitcher.toggle()
//                    config.enableCountCharacters = countCharactersSwitcher.isChecked
//                }
//
//                R.id.locationInfo -> {
//                    locationInfoSwitcher.toggle()
//                    when (locationInfoSwitcher.isChecked) {
//                        true -> {
//                            run {
//                                when (hasGPSPermissions()) {
//                                    true -> {
//                                        config.enableLocationInfo = locationInfoSwitcher.isChecked
//                                    }
//
//                                    false -> {
//                                        locationInfoSwitcher.isChecked = false
//                                        if (this@activity is EasyDiaryActivity) {
//                                            acquireGPSPermissions(mRequestLocationSourceLauncher) {
//                                                locationInfoSwitcher.isChecked = true
//                                                config.enableLocationInfo =
//                                                    locationInfoSwitcher.isChecked
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        false -> {
//                            config.enableLocationInfo = locationInfoSwitcher.isChecked
//                        }
//                    }
//                }
//
//                R.id.holdPositionEnterEditScreen -> {
//                    holdPositionSwitcher.toggle()
//                    config.holdPositionEnterEditScreen = holdPositionSwitcher.isChecked
//                }
//
//                R.id.maxLines -> {
//                    openMaxLinesSettingDialog()
//                }
//
//                R.id.taskSymbolTopOrder -> {
//                    taskSymbolTopOrderSwitcher.toggle()
//                    config.enableTaskSymbolTopOrder = taskSymbolTopOrderSwitcher.isChecked
//                }
//
//                R.id.enableReviewFlow -> {
//                    enableReviewFlowSwitcher.toggle()
//                    config.enableReviewFlow = enableReviewFlowSwitcher.isChecked
//                }

                R.id.enable_photo_highlight -> {
                    enablePhotoHighlightSwitcher.toggle()
                    config.enablePhotoHighlight = enablePhotoHighlightSwitcher.isChecked
                }
                R.id.disable_future_diary -> {
                    disableFutureDiarySwitcher.toggle()
                    config.disableFutureDiary = disableFutureDiarySwitcher.isChecked
                }

//                R.id.enable_welcome_dashboard_popup -> {
//                    enableWelcomeDashboardPopupSwitcher.toggle()
//                    config.enableWelcomeDashboardPopup =
//                        enableWelcomeDashboardPopupSwitcher.isChecked
//                }
//
//                R.id.card_markdown_setting -> {
//                    switchMarkdownSetting.toggle()
//                    config.enableMarkdown = switchMarkdownSetting.isChecked
//                }
            }
        }
    }

    private fun bindEvent() {
        mBinding.run {
//            primaryColor.setOnClickListener(mOnClickListener)
//            thumbnailSetting.setOnClickListener(mOnClickListener)
//            cardDatetimeSetting.setOnClickListener(mOnClickListener)
//            contentsSummary.setOnClickListener(mOnClickListener)
//            enableCardViewPolicy.setOnClickListener(mOnClickListener)
//            multiPickerOption.setOnClickListener(mOnClickListener)
//            sensitiveOption.setOnClickListener(mOnClickListener)
//            maxLines.setOnClickListener(mOnClickListener)
//            countCharacters.setOnClickListener(mOnClickListener)
//            locationInfo.setOnClickListener(mOnClickListener)
//            holdPositionEnterEditScreen.setOnClickListener(mOnClickListener)
//            taskSymbolTopOrder.setOnClickListener(mOnClickListener)
//            enableReviewFlow.setOnClickListener(mOnClickListener)
            enablePhotoHighlight.setOnClickListener(mOnClickListener)
            disableFutureDiary.setOnClickListener(mOnClickListener)
//            enableWelcomeDashboardPopup.setOnClickListener(mOnClickListener)
//            cardMarkdownSetting.setOnClickListener(mOnClickListener)
//            calendarStartDay.setOnCheckedChangeListener { _, i ->
//                requireActivity().config.calendarStartDay = when (i) {
//                    R.id.startMonday -> CALENDAR_START_DAY_MONDAY
////                R.id.startTuesday -> CALENDAR_START_DAY_TUESDAY
////                R.id.startWednesday -> CALENDAR_START_DAY_WEDNESDAY
////                R.id.startThursday -> CALENDAR_START_DAY_THURSDAY
////                R.id.startFriday -> CALENDAR_START_DAY_FRIDAY
//                    R.id.startSaturday -> CALENDAR_START_DAY_SATURDAY
//                    else -> CALENDAR_START_DAY_SUNDAY
//                }
//            }
//            calendarSorting.setOnCheckedChangeListener { _, i ->
//                requireActivity().config.calendarSorting = when (i) {
//                    R.id.ascending -> CALENDAR_SORTING_ASC
//                    else -> CALENDAR_SORTING_DESC
//                }
//            }
        }
    }

    private fun initPreference() {
            mBinding.run {
//                sensitiveOptionSwitcher.isChecked = config.diarySearchQueryCaseSensitive
//                multiPickerOptionSwitcher.isChecked = config.multiPickerEnable
//                enableCardViewPolicySwitcher.isChecked = config.enableCardViewPolicy
//                contentsSummarySwitcher.isChecked = config.enableContentsSummary
//                countCharactersSwitcher.isChecked = config.enableCountCharacters
//                locationInfoSwitcher.isChecked = config.enableLocationInfo
//                taskSymbolTopOrderSwitcher.isChecked = config.enableTaskSymbolTopOrder
//                enableReviewFlowSwitcher.isChecked = config.enableReviewFlow
                enablePhotoHighlightSwitcher.isChecked = config.enablePhotoHighlight
                disableFutureDiarySwitcher.isChecked = config.disableFutureDiary
//                enableWelcomeDashboardPopupSwitcher.isChecked = config.enableWelcomeDashboardPopup

//                when (config.calendarStartDay) {
//                    CALENDAR_START_DAY_MONDAY -> startMonday.isChecked = true
//                    CALENDAR_START_DAY_SATURDAY -> startSaturday.isChecked = true
//                    else -> startSunday.isChecked = true
//                }
//                when (config.calendarSorting) {
//                    CALENDAR_SORTING_ASC -> ascending.isChecked = true
//                    CALENDAR_SORTING_DESC -> descending.isChecked = true
//                }
//                holdPositionSwitcher.isChecked = config.holdPositionEnterEditScreen
//                thumbnailSettingDescription.text = "${config.settingThumbnailSize.toInt()}dp x ${config.settingThumbnailSize.toInt()}dp"
//                maxLines.visibility = if (contentsSummarySwitcher.isChecked) View.VISIBLE else View.GONE
//                maxLinesValue.text = getString(R.string.max_lines_value, config.summaryMaxLines)
//
//                textDatetimeSettingDescription.text = DateUtils.getDateTimeStringForceFormatting(
//                    System.currentTimeMillis(), requireContext()
//                )
//                switchMarkdownSetting.isChecked = config.enableMarkdown
            }
    }
}