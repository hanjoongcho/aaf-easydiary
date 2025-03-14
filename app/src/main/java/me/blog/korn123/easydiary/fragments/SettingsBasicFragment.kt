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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
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
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.databinding.FragmentSettingsBasicBinding
import me.blog.korn123.easydiary.enums.DateTimeFormat
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.acquireGPSPermissions
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.hasGPSPermissions
import me.blog.korn123.easydiary.extensions.isLocationEnabled
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.startMainActivityWithClearTask
import me.blog.korn123.easydiary.extensions.updateAlertDialogWithIcon
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_DESC
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_MONDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SATURDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SUNDAY
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.ui.components.RadioGroupCard
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.components.SwitchCardTodo
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.SettingsViewModel
import me.blog.korn123.easydiary.viewmodels.SwitchViewModel
import java.text.SimpleDateFormat

class SettingsBasicFragment : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentSettingsBasicBinding
    private lateinit var mRequestLocationSourceLauncher: ActivityResultLauncher<Intent>
    private val mSettingsViewModel: SettingsViewModel by viewModels()


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
                        config.enableLocationInfo = true
                        mSettingsViewModel.setEnableLocationInfo(true)
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
        if (BuildConfig.FLAVOR == "foss") mSettingsViewModel.setEnableReviewFlowVisible(false)
        mSettingsViewModel.setEnableCardViewPolicy(requireActivity().config.enableCardViewPolicy)

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

                    val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(true)

                    SimpleCard(
                        title = getString(R.string.setting_primary_color_title),
                        description = getString(R.string.setting_primary_color_summary),
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        TransitionHelper.startActivityWithTransition(
                            requireActivity()
                            , Intent(requireActivity(), CustomizationActivity::class.java)
                        )
                    }

                    var enableMarkdown by remember { mutableStateOf(requireContext().config.enableMarkdown) }
                    SwitchCard(
                        title = getString(R.string.markdown_setting_title),
                        description = getString(R.string.markdown_setting_summary),
                        modifier = settingCardModifier,
                        isOn = enableMarkdown,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        requireActivity().run {
                            enableMarkdown = enableMarkdown.not()
                            config.enableMarkdown = enableMarkdown
                        }
                    }

                    val viewModel: SwitchViewModel by viewModels()
                    val enableShakeDetector: Boolean by viewModel.isOn.observeAsState(requireActivity().config.enableShakeDetector)
                    SwitchCard(
                        title = getString(R.string.quick_setting_title),
                        description = getString(R.string.quick_setting_summary),
                        modifier = settingCardModifier,
                        isOn = enableShakeDetector,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        requireActivity().run {
                            config.enableShakeDetector = enableShakeDetector.not()
                            viewModel.isOn.value = config.enableShakeDetector
                        }
                    }

                    var enableWelcomeDashboardPopup by remember { mutableStateOf(requireContext().config.enableWelcomeDashboardPopup) }
                    SwitchCard(
                        title = getString(R.string.enable_welcome_dashboard_popup_title),
                        description = getString(R.string.enable_welcome_dashboard_popup_description),
                        modifier = settingCardModifier,
                        isOn = enableWelcomeDashboardPopup,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        requireActivity().run {
                            enableWelcomeDashboardPopup = enableWelcomeDashboardPopup.not()
                            config.enableWelcomeDashboardPopup = enableWelcomeDashboardPopup
                        }
                    }

                    var enablePhotoHighlight by remember { mutableStateOf(requireContext().config.enablePhotoHighlight) }
                    SwitchCard(
                        title = getString(R.string.enable_photo_highlight_title),
                        description = getString(R.string.enable_photo_highlight_description),
                        modifier = settingCardModifier,
                        isOn = enablePhotoHighlight,
                        enableCardViewPolicy = enableCardViewPolicy
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
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        enableTaskSymbolTopOrder = enableTaskSymbolTopOrder.not()
                        config.enableTaskSymbolTopOrder = enableTaskSymbolTopOrder
                    }


                    val enableLocationInfo: Boolean by mSettingsViewModel.enableLocationInfo.observeAsState(requireActivity().config.enableLocationInfo)
                    SwitchCard(
                        title = getString(R.string.location_info_title)
                        , description = getString(R.string.location_info_description)
                        , modifier = settingCardModifier
                        , isOn = enableLocationInfo
                        , enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        requireActivity().run {
                            mSettingsViewModel.setEnableLocationInfo(enableLocationInfo.not())
                            when (mSettingsViewModel.enableLocationInfoIsOn()) {
                                true -> {
                                    when (hasGPSPermissions()) {
                                        true -> {
                                            config.enableLocationInfo = true
                                        }
                                        false -> {
                                            config.enableLocationInfo = false
                                            mSettingsViewModel.setEnableLocationInfo(false)
                                            requireActivity().run {
                                                if (this is EasyDiaryActivity) {
                                                    acquireGPSPermissions(mRequestLocationSourceLauncher) {
                                                        config.enableLocationInfo = true
                                                        mSettingsViewModel.setEnableLocationInfo(true)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                false -> {
                                    config.enableLocationInfo = false
                                }
                            }
                        }
                    }

                    val settingThumbnailSize: String by mSettingsViewModel.thumbnailSizeSubDescription.observeAsState("")
                    SimpleCard(
                        title = getString(R.string.thumbnail_setting_title),
                        description = getString(R.string.thumbnail_setting_summary),
                        subDescription = settingThumbnailSize,
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        openThumbnailSettingDialog()
                    }

                    val settingDatetimeFormat: String by mSettingsViewModel.datetimeFormatSubDescription.observeAsState("")
                    SimpleCard(
                        title = getString(R.string.datetime_setting_title),
                        description = getString(R.string.datetime_setting_summary),
                        subDescription = settingDatetimeFormat,
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        openDatetimeFormattingSettingDialog()
                    }

                    var enableContentsSummary by remember { mutableStateOf(requireContext().config.enableContentsSummary) }
                    SwitchCard(
                        title = getString(R.string.contents_summary_title),
                        description = getString(R.string.contents_summary_description),
                        modifier = settingCardModifier,
                        isOn = enableContentsSummary,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        requireActivity().run {
                            enableContentsSummary = enableContentsSummary.not()
                            config.enableContentsSummary = enableContentsSummary
                            initPreference()
                        }
                    }

                    if (enableContentsSummary) {
                        val summaryMaxLines: String by mSettingsViewModel.summaryMaxLinesSubDescription.observeAsState("")
                        SimpleCard(
                            title = getString(R.string.max_lines_title),
                            description = getString(R.string.max_lines_summary),
                            subDescription = summaryMaxLines,
                            modifier = settingCardModifier,
                            enableCardViewPolicy = enableCardViewPolicy
                        ) {
                            openMaxLinesSettingDialog()
                        }
                    }

                    SwitchCard(
                        title = getString(R.string.enable_card_view_policy_title),
                        description = getString(R.string.enable_card_view_policy_summary),
                        modifier = settingCardModifier,
                        isOn = enableCardViewPolicy,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        requireActivity().run {
                            config.enableCardViewPolicy = enableCardViewPolicy.not()
                            mSettingsViewModel.setEnableCardViewPolicy(config.enableCardViewPolicy)
                        }
                    }

                    var diarySearchQueryCaseSensitive by remember { mutableStateOf(requireContext().config.diarySearchQueryCaseSensitive) }
                    SwitchCard(
                        title = getString(R.string.diary_search_keyword_case_sensitive_title),
                        description = getString(R.string.diary_search_keyword_case_sensitive_summary),
                        modifier = settingCardModifier,
                        isOn = diarySearchQueryCaseSensitive,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        requireActivity().run {
                            diarySearchQueryCaseSensitive = diarySearchQueryCaseSensitive.not()
                            config.enableContentsSummary = diarySearchQueryCaseSensitive
                        }
                    }

                    var calendarStartDay by remember { mutableIntStateOf(requireContext().config.calendarStartDay) }
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

                    var calendarSorting by remember { mutableIntStateOf(requireContext().config.calendarSorting) }
                    RadioGroupCard(
                        title = getString(R.string.calendar_sort_title),
                        description = getString(R.string.calendar_sort_summary),
                        modifier = settingCardModifier,
                        options = listOf(
                            mapOf(
                                "title" to LocalContext.current.getString(R.string.calendar_sort_ascending),
                                "key" to CALENDAR_SORTING_ASC
                            ),
                            mapOf(
                                "title" to LocalContext.current.getString(R.string.calendar_sort_descending),
                                "key" to CALENDAR_SORTING_DESC
                            ),
                        ),
                        selectedKey = calendarSorting
                    ) { key ->
                        config.calendarSorting = key
                        calendarSorting = key
                    }

                    var enableCountCharacters by remember { mutableStateOf(requireContext().config.enableCountCharacters) }
                    SwitchCard(
                        title = getString(R.string.count_characters_title),
                        description = getString(R.string.count_characters_summary),
                        modifier = settingCardModifier,
                        isOn = enableCountCharacters,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        requireActivity().run {
                            enableCountCharacters = enableCountCharacters.not()
                            config.enableCountCharacters = enableCountCharacters
                        }
                    }

                    var holdPositionEnterEditScreen by remember { mutableStateOf(requireContext().config.holdPositionEnterEditScreen) }
                    SwitchCard(
                        title = getString(R.string.hold_position_title),
                        description = getString(R.string.hold_position_summary),
                        modifier = settingCardModifier,
                        isOn = holdPositionEnterEditScreen,
                        enableCardViewPolicy = enableCardViewPolicy
                    ) {
                        requireActivity().run {
                            holdPositionEnterEditScreen = holdPositionEnterEditScreen.not()
                            config.holdPositionEnterEditScreen = holdPositionEnterEditScreen
                        }
                    }

                    if (mSettingsViewModel.enableReviewFlowVisibleIsOn()) {
                        var enableReviewFlow by remember { mutableStateOf(requireContext().config.enableReviewFlow) }
                        SwitchCard(
                            title = getString(R.string.enable_review_flow_title),
                            description = getString(R.string.enable_review_flow_summary),
                            modifier = settingCardModifier,
                            isOn = enableReviewFlow,
                            enableCardViewPolicy = enableCardViewPolicy
                        ) {
                            requireActivity().run {
                                enableReviewFlow = enableReviewFlow.not()
                                config.enableReviewFlow = enableReviewFlow
                            }
                        }
                    }
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
    @SuppressLint("SetTextI18n")
    private fun initPreference() {
        requireActivity().run {
            mBinding.run {
                mSettingsViewModel.setThumbnailSizeSubDescription("${config.settingThumbnailSize}dp x ${config.settingThumbnailSize}dp")
                mSettingsViewModel.setDatetimeFormatSubDescription(
                    DateUtils.getDateTimeStringForceFormatting(
                        System.currentTimeMillis(), requireContext()
                    )
                )
                mSettingsViewModel.setSummaryMaxLinesSubDescription(getString(R.string.max_lines_value, config.summaryMaxLines))

                if (!hasGPSPermissions()) {
                    config.enableLocationInfo = false
                    mSettingsViewModel.setEnableLocationInfo(false)
                }
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