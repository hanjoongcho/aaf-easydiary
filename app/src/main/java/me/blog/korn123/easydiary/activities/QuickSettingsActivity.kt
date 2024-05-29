package me.blog.korn123.easydiary.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.compose.QuickSettingsActivity
import me.blog.korn123.easydiary.databinding.ActivityQuickSettingsBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.AlarmWorkExecutor
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

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
            title = "Quick Settings"
            setHomeAsUpIndicator(R.drawable.ic_cross)
            setDisplayHomeAsUpEnabled(true)
        }

        val viewModel: QuickSettingsViewModel by viewModels()
        viewModel.enablePhotoHighlight.value = config.enablePhotoHighlight

        mBinding.run {
            composeView.setContent {
                AppTheme {
                    QuickSettings(context = this@QuickSettingsActivity, false, viewModel)
                }
            }

            enablePhotoHighlight.setOnClickListener { view ->
                when (view.id) {
                    R.id.enable_photo_highlight -> {
                        enablePhotoHighlightSwitcher.toggle()
                        config.enablePhotoHighlight = enablePhotoHighlightSwitcher.isChecked
                        viewModel.toggle()
                    }
                }

                updateCardAlpha()
            }
        }

        initPreference()
    }


    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun QuickSettings(context: Context, isPreview: Boolean = false, viewModel: QuickSettingsViewModel) {
        val pixelValue = context.config.settingFontSize
        val density = LocalDensity.current
        val currentTextUnit = with (density) {
            val temp = pixelValue.toDp()
            temp.toSp()
        }
        val isOn: Boolean by viewModel.enablePhotoHighlight.observeAsState(context.config.enablePhotoHighlight)
        var disableFutureDiary by remember { mutableStateOf(context.config.disableFutureDiary) }
        var enableWelcomeDashboardPopup by remember { mutableStateOf(context.config.enableWelcomeDashboardPopup) }
        var enableMarkdown by remember { mutableStateOf(context.config.enableMarkdown) }
        var enableCardViewPolicy by remember { mutableStateOf(context.config.enableCardViewPolicy) }

        Column {
            FlowRow(
                modifier = Modifier,
//                    .padding(3.dp, 3.dp)
//                    .fillMaxWidth(1f)
//                    .fillMaxHeight(1f),
//                horizontalArrangement = Arrangement.spacedBy(3.dp),
//                verticalArrangement = Arrangement.spacedBy(3.dp),
//                overflow = FlowRowOverflow.Clip,
                maxItemsInEachRow = 2
            ) {
                val settingCardModifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)

                SwitchCard(
                    stringResource(R.string.markdown_setting_title),
                    stringResource(R.string.markdown_setting_summary),
                    settingCardModifier,
                    enableMarkdown
                ) {
                    context.config.enableMarkdown = !enableMarkdown
                    enableMarkdown = !enableMarkdown
                }
                SwitchCard(
                    stringResource(R.string.enable_welcome_dashboard_popup_title),
                    stringResource(R.string.enable_welcome_dashboard_popup_description),
                    settingCardModifier,
                    enableWelcomeDashboardPopup
                ) {
                    context.config.enableWelcomeDashboardPopup = !enableWelcomeDashboardPopup
                    enableWelcomeDashboardPopup = !enableWelcomeDashboardPopup
                }
                SwitchCard(
                    stringResource(R.string.enable_photo_highlight_title),
                    stringResource(R.string.enable_photo_highlight_description),
                    settingCardModifier,
                    isOn
                ) {
                    viewModel.toggle()
                    context.config.enablePhotoHighlight = !context.config.enablePhotoHighlight
                    initPreference()
                }
                SwitchCard(
                    stringResource(R.string.enable_card_view_policy_title),
                    stringResource(R.string.enable_card_view_policy_summary),
                    settingCardModifier,
                    enableCardViewPolicy
                ) {
                    context.config.enableCardViewPolicy = !enableCardViewPolicy
                    enableCardViewPolicy = !enableCardViewPolicy
                }
                SwitchCard(
                    "미래일정 숨김",
                    "미래일정을 메인화면 목록에서 보이지 않도록 설정합니다.",
                    settingCardModifier,
                    disableFutureDiary
                ) {
                    context.config.disableFutureDiary = !disableFutureDiary
                    disableFutureDiary = !disableFutureDiary
                }

                SimpleCard(
                    stringResource(id = R.string.sync_google_calendar_event_title),
                    stringResource(id = R.string.sync_google_calendar_event_summary),
                    settingCardModifier,
                ) {
                    val alarm = Alarm().apply {
                        sequence = Int.MAX_VALUE
                        workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                        label = "Quick Settings"
                    }
                    AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
                }
                SimpleCard(
                    "Quick Settings With Compose",
                    null,
                    settingCardModifier,
                ) {
                    TransitionHelper.startActivityWithTransition(
                        this@QuickSettingsActivity,
                        Intent(this@QuickSettingsActivity, QuickSettingsActivity::class.java)
                    )
                    finish()
                }
//
//                val itemModifier = settingCardModifier
//                    .padding(4.dp)
//                    .height(80.dp)
//                    .clip(RoundedCornerShape(8.dp))
//                    .background(Color(context.config.backgroundColor))
//
//                val spaceModifier = Modifier
//                repeat(3) { item ->
//                    if ((item + 1) % 3 == 0) {
//                        Spacer(modifier = itemModifier.fillMaxWidth())
//                    } else {
//                        Spacer(modifier = itemModifier.weight(0.5f))
//                    }
//                }
            }
        }
    }

    @Preview(heightDp = 2000)
    @Composable
    private fun QuickSettingsPreview() {
        AppTheme {
            QuickSettings(LocalContext.current, true, viewModel())
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initPreference() {
        mBinding.run {
            enablePhotoHighlightSwitcher.isChecked = config.enablePhotoHighlight
            updateCardAlpha()
        }
    }

    private fun updateCardAlpha() {
        mBinding.run {
            enablePhotoHighlight.alpha = if (enablePhotoHighlightSwitcher.isChecked) 1.0f else 0.5f
        }
    }

    class QuickSettingsViewModel : ViewModel() {
        var enablePhotoHighlight: MutableLiveData<Boolean> = MutableLiveData()
            private set

        fun toggle() {
            enablePhotoHighlight.value = enablePhotoHighlight.value != true
        }
    }
}