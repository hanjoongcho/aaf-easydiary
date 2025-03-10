package me.blog.korn123.easydiary.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.viewModels
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.CustomizationActivity
import me.blog.korn123.easydiary.activities.EasyDiaryActivity
import me.blog.korn123.easydiary.activities.FingerprintLockActivity
import me.blog.korn123.easydiary.activities.PinLockActivity
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.compose.QuickSettingsActivity.QuickSettingsViewModel
import me.blog.korn123.easydiary.databinding.FragmentSettingsBasicBinding
import me.blog.korn123.easydiary.enums.DateTimeFormat
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.enums.Launcher
import me.blog.korn123.easydiary.extensions.acquireGPSPermissions
import me.blog.korn123.easydiary.extensions.applyPolicyForRecentApps
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.hasGPSPermissions
import me.blog.korn123.easydiary.extensions.isLocationEnabled
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.startMainActivityWithClearTask
import me.blog.korn123.easydiary.extensions.toggleLauncher
import me.blog.korn123.easydiary.extensions.updateAlertDialogWithIcon
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_DESC
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_MONDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SATURDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SUNDAY
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.ui.components.RadioGroupCard
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SimpleCardWithImage
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.components.SwitchCardTodo
import me.blog.korn123.easydiary.ui.components.SwitchCardWithImage
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.SwitchViewModel
import java.text.SimpleDateFormat

class SettingsBasicFragment : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentSettingsBasicBinding
    private lateinit var mRequestLocationSourceLauncher: ActivityResultLauncher<Intent>

    
    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRequestLocationSourceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requireActivity().run {
                pauseLock()
                when (isLocationEnabled()) {
                    true -> {
                        mBinding.locationInfoSwitcher.isChecked = true
                        config.enableLocationInfo = mBinding.locationInfoSwitcher.isChecked
                        makeSnackBar("GPS provider setting is activated!!!")
                    }
                    false -> makeSnackBar("The request operation did not complete normally.")
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragmentSettingsBasicBinding.inflate(layoutInflater)
        return mBinding.root
    }

    @OptIn(ExperimentalLayoutApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.FLAVOR == "foss") mBinding.enableReviewFlow.visibility = View.GONE
        bindEvent()
        updateFragmentUI(mBinding.root)
        initPreference()

        mBinding.composeView.setContent {
            AppTheme {
                val configuration = LocalConfiguration.current
                FlowRow(
                    maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 2,
                    modifier = Modifier
                ) {
                    val settingCardModifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)

                    SimpleCard(
                        title = getString(R.string.setting_primary_color_title),
                        description = getString(R.string.setting_primary_color_summary),
                        modifier = settingCardModifier
                    ) {
                        TransitionHelper.startActivityWithTransition(
                            requireActivity()
                            , Intent(requireActivity(), CustomizationActivity::class.java)
                        )
                    }

                    var enableMarkdown by remember { mutableStateOf(requireContext().config.enableMarkdown) }
                    SwitchCard(
                        title = "${getString(R.string.markdown_setting_title)} $enableMarkdown"
                        , description = getString(R.string.markdown_setting_summary)
                        , modifier = settingCardModifier
                        , isOn = enableMarkdown
                    ) {
                        requireActivity().run {
                            enableMarkdown = enableMarkdown.not()
                            config.enableMarkdown = enableMarkdown
                        }
                    }

                    var calendarStartDay by remember { mutableStateOf(requireContext().config.calendarStartDay) }
                    RadioGroupCard(
                        title = getString(R.string.calendar_start_day_title),
                        description = getString(R.string.calendar_start_day_summary),
                        modifier = settingCardModifier,
                        options = listOf(
                            mapOf(
                                "title" to LocalContext.current.getString(R.string.calendar_start_day_saturday),
                                "key" to CALENDAR_START_DAY_SATURDAY
                            ),
                            mapOf(
                                "title" to LocalContext.current.getString(R.string.calendar_start_day_sunday),
                                "key" to CALENDAR_START_DAY_SUNDAY
                            ),
                            mapOf(
                                "title" to LocalContext.current.getString(R.string.calendar_start_day_monday),
                                "key" to CALENDAR_START_DAY_MONDAY
                            )
                        ),
                        selectedKey = calendarStartDay
                    ) { key ->
                        calendarStartDay = key
                        config.calendarStartDay = calendarStartDay
                    }

                    val viewModel: SwitchViewModel by viewModels()
                    val enableShakeDetector: Boolean by viewModel.isOn.observeAsState(requireActivity().config.enableShakeDetector)
                    SwitchCard(
                        title = "${getString(R.string.quick_setting_title)} $enableShakeDetector"
                        , description = getString(R.string.quick_setting_summary)
                        , modifier = settingCardModifier
                        , isOn = enableShakeDetector
                    ) {
                        requireActivity().run {
                            viewModel.toggle()
                            config.enableShakeDetector = enableShakeDetector
                        }
                    }

                    var enableWelcomeDashboardPopup by remember { mutableStateOf(requireContext().config.enableWelcomeDashboardPopup) }
                    SwitchCard(
                        title = getString(R.string.enable_welcome_dashboard_popup_title)
                        , description = getString(R.string.enable_welcome_dashboard_popup_description)
                        , modifier = settingCardModifier
                        , isOn = enableWelcomeDashboardPopup
                    ) {
                        requireActivity().run {
                            enableWelcomeDashboardPopup = enableWelcomeDashboardPopup.not()
                            config.enableWelcomeDashboardPopup = enableWelcomeDashboardPopup
                        }
                    }

                    var enablePhotoHighlight by remember { mutableStateOf(requireContext().config.enablePhotoHighlight) }
                    SwitchCard(
                        title = getString(R.string.enable_photo_highlight_title)
                        , description = getString(R.string.enable_photo_highlight_description)
                        , modifier = settingCardModifier
                        , isOn = enablePhotoHighlight
                    ) {
                        requireActivity().run {
                            enablePhotoHighlight = enablePhotoHighlight.not()
                            config.enablePhotoHighlight = enablePhotoHighlight
                        }
                    }

                    var enableTaskSymbolTopOrder by remember { mutableStateOf(requireContext().config.enableTaskSymbolTopOrder) }
                    SwitchCardTodo(
                        title = getString(R.string.task_symbol_top_order_title),
                        description = getString(R.string.task_symbol_top_order_description),
                        modifier = settingCardModifier,
                        isOn = enableTaskSymbolTopOrder,
                    ) {
                        enableTaskSymbolTopOrder = enableTaskSymbolTopOrder.not()
                        config.enableTaskSymbolTopOrder = enableTaskSymbolTopOrder
                    }

                    SimpleCard(
                        title = "🍟🍔🍕🌭🍿",
                        description = null,
                        modifier = settingCardModifier
                    )
                }
            }
        }
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
    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        requireActivity().run activity@ {
            mBinding.run {
                when (view.id) {
                    R.id.thumbnailSetting -> {
                        openThumbnailSettingDialog()
                    }

                    cardDatetimeSetting.id -> {
                        openDatetimeFormattingSettingDialog()
                    }

                    R.id.contentsSummary -> {
                        contentsSummarySwitcher.toggle()
                        config.enableContentsSummary = contentsSummarySwitcher.isChecked
                        maxLines.visibility =
                            if (contentsSummarySwitcher.isChecked) View.VISIBLE else View.GONE
                    }

                    R.id.enableCardViewPolicy -> {
                        enableCardViewPolicySwitcher.toggle()
                        config.enableCardViewPolicy = enableCardViewPolicySwitcher.isChecked
                        updateCardViewPolicy(this.root)
                    }

//                    R.id.multiPickerOption -> {
//                        multiPickerOptionSwitcher.toggle()
//                        config.multiPickerEnable = multiPickerOptionSwitcher.isChecked
//                    }

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
                                            config.enableLocationInfo =
                                                locationInfoSwitcher.isChecked
                                        }

                                        false -> {
                                            locationInfoSwitcher.isChecked = false
                                            if (this@activity is EasyDiaryActivity) {
                                                acquireGPSPermissions(mRequestLocationSourceLauncher) {
                                                    locationInfoSwitcher.isChecked = true
                                                    config.enableLocationInfo =
                                                        locationInfoSwitcher.isChecked
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
            thumbnailSetting.setOnClickListener(mOnClickListener)
            cardDatetimeSetting.setOnClickListener(mOnClickListener)
            contentsSummary.setOnClickListener(mOnClickListener)
            enableCardViewPolicy.setOnClickListener(mOnClickListener)
//            multiPickerOption.setOnClickListener(mOnClickListener)
            sensitiveOption.setOnClickListener(mOnClickListener)
            maxLines.setOnClickListener(mOnClickListener)
            countCharacters.setOnClickListener(mOnClickListener)
            locationInfo.setOnClickListener(mOnClickListener)
            holdPositionEnterEditScreen.setOnClickListener(mOnClickListener)
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
//                multiPickerOptionSwitcher.isChecked = config.multiPickerEnable
                enableCardViewPolicySwitcher.isChecked = config.enableCardViewPolicy
                contentsSummarySwitcher.isChecked = config.enableContentsSummary
                countCharactersSwitcher.isChecked = config.enableCountCharacters
                locationInfoSwitcher.isChecked = config.enableLocationInfo
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

                textDatetimeSettingDescription.text = DateUtils.getDateTimeStringForceFormatting(
                    System.currentTimeMillis(), requireContext()
                )
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
            alertDialog = builder.create().apply {
                updateAlertDialogWithIcon(DialogMode.SETTING, this, null, containerView, getString(R.string.thumbnail_setting_title))
            }

            listView.setSelection(selectedIndex)
        }
    }

    private fun openDatetimeFormattingSettingDialog() {
        requireActivity().run {
            var alertDialog: AlertDialog? = null
            val builder = AlertDialog.Builder(this)
            builder.setNegativeButton(getString(android.R.string.cancel), null)
            val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val containerView = inflater.inflate(R.layout.dialog_option_item, mBinding.root, false)
            val listView = containerView.findViewById<ListView>(R.id.listView)

            val listThumbnailSize = ArrayList<Map<String, String>>()
            listThumbnailSize.add(
                mapOf(
                    "optionTitle" to DateUtils.getDateTimeStringFromTimeMillis(
                        System.currentTimeMillis(),
                        SimpleDateFormat.FULL,
                        SimpleDateFormat.FULL
                    ), "optionValue" to DateTimeFormat.DATE_FULL_AND_TIME_FULL.toString()
                )
            )
            listThumbnailSize.add(
                mapOf(
                    "optionTitle" to DateUtils.getDateTimeStringFromTimeMillis(
                        System.currentTimeMillis(),
                        SimpleDateFormat.FULL,
                        SimpleDateFormat.SHORT
                    ), "optionValue" to DateTimeFormat.DATE_FULL_AND_TIME_SHORT.toString()
                )
            )
            listThumbnailSize.add(
                mapOf(
                    "optionTitle" to DateUtils.getDateTimeStringFromTimeMillis(
                        System.currentTimeMillis(),
                        SimpleDateFormat.LONG,
                        SimpleDateFormat.LONG
                    ), "optionValue" to DateTimeFormat.DATE_LONG_AND_TIME_LONG.toString()
                )
            )
            listThumbnailSize.add(
                mapOf(
                    "optionTitle" to DateUtils.getDateTimeStringFromTimeMillis(
                        System.currentTimeMillis(),
                        SimpleDateFormat.MEDIUM,
                        SimpleDateFormat.MEDIUM
                    ), "optionValue" to DateTimeFormat.DATE_MEDIUM_AND_TIME_MEDIUM.toString()
                )
            )
            listThumbnailSize.add(
                mapOf(
                    "optionTitle" to DateUtils.getDateTimeStringFromTimeMillis(
                        System.currentTimeMillis(),
                        SimpleDateFormat.MEDIUM,
                        SimpleDateFormat.SHORT
                    ), "optionValue" to DateTimeFormat.DATE_MEDIUM_AND_TIME_SHORT.toString()
                )
            )
            listThumbnailSize.add(
                mapOf(
                    "optionTitle" to DateUtils.getDateTimeStringFromTimeMillis(
                        System.currentTimeMillis(),
                        SimpleDateFormat.SHORT,
                        SimpleDateFormat.SHORT
                    ), "optionValue" to DateTimeFormat.DATE_SHORT_AND_TIME_SHORT.toString()
                )
            )

            val arrayAdapter = OptionItemAdapter(this, R.layout.item_check_label, listThumbnailSize, null, config.settingDatetimeFormat)
            listView.adapter = arrayAdapter
            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                @Suppress("UNCHECKED_CAST") val fontInfo = parent.adapter.getItem(position) as HashMap<String, String>
                fontInfo["optionValue"]?.let {
                    config.settingDatetimeFormat = it
                    initPreference()
                }
                alertDialog?.cancel()
            }
            alertDialog = builder.create().apply {
                updateAlertDialogWithIcon(DialogMode.SETTING, this, null, containerView, "Datetime formatting")
            }

            var selectedIndex = 0
            listThumbnailSize.mapIndexed { index, map ->
                val optionValue = map["optionValue"] ?: "0"
                if (config.settingDatetimeFormat == optionValue) selectedIndex = index
            }
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

            alertDialog = builder.create().apply {
                updateAlertDialogWithIcon(DialogMode.SETTING, this, null, containerView, getString(R.string.max_lines_title))
            }
            listView.setSelection(selectedIndex)
        }
    }
}