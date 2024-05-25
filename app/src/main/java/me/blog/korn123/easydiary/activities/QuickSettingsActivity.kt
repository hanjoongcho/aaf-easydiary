package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityQuickSettingsBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.AlarmWorkExecutor
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

class QuickSettingsActivity : ComponentActivity() {


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: QuickSettingsViewModel by viewModels()
            viewModel.enablePhotoHighlight.value = config.enablePhotoHighlight

            val pixelValue = config.settingFontSize
            val density = LocalDensity.current
            val currentTextUnit = with (density) {
                val temp = pixelValue.toDp()
                temp.toSp()
            }

            AppTheme {
                Scaffold(
                    topBar = {
                        EasyDiaryActionBar(currentTextUnit) {
                            onBackPressed()
                        }
                    },
                    content = { innerPadding ->
//                    it.calculateTopPadding()
                        QuickSettings(
                            context = this@QuickSettingsActivity,
                            false,
                            viewModel,
                            Modifier.padding(innerPadding)
                        )
                    },
                )

            }
        }
    }


    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun QuickSettings(context: Context, isPreview: Boolean = false, viewModel: QuickSettingsViewModel, modifier: Modifier = Modifier) {
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

        val scrollState = rememberScrollState()
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color(context.config.screenBackgroundColor))
                .padding(0.dp, 3.dp, 0.dp, 0.dp)
        ) {
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
                    currentTextUnit,
                    isPreview,
                    stringResource(R.string.markdown_setting_title),
                    stringResource(R.string.markdown_setting_summary),
                    settingCardModifier,
                    enableMarkdown
                ) {
                    context.config.enableMarkdown = !enableMarkdown
                    enableMarkdown = !enableMarkdown
                }
                SwitchCard(
                    currentTextUnit,
                    isPreview,
                    stringResource(R.string.enable_welcome_dashboard_popup_title),
                    stringResource(R.string.enable_welcome_dashboard_popup_description),
                    settingCardModifier,
                    enableWelcomeDashboardPopup
                ) {
                    context.config.enableWelcomeDashboardPopup = !enableWelcomeDashboardPopup
                    enableWelcomeDashboardPopup = !enableWelcomeDashboardPopup
                }
                SwitchCard(
                    currentTextUnit,
                    isPreview,
                    stringResource(R.string.enable_photo_highlight_title),
                    stringResource(R.string.enable_photo_highlight_description),
                    settingCardModifier,
                    isOn
                ) {
                    viewModel.toggle()
                    context.config.enablePhotoHighlight = !context.config.enablePhotoHighlight
                }
                SwitchCard(
                    currentTextUnit,
                    isPreview,
                    stringResource(R.string.enable_card_view_policy_title),
                    stringResource(R.string.enable_card_view_policy_summary),
                    settingCardModifier,
                    enableCardViewPolicy
                ) {
                    context.config.enableCardViewPolicy = !enableCardViewPolicy
                    enableCardViewPolicy = !enableCardViewPolicy
                }
                SwitchCard(
                    currentTextUnit,
                    isPreview,
                    "미래일정 숨김",
                    "미래일정을 메인화면 목록에서 보이지 않도록 설정합니다.",
                    settingCardModifier,
                    disableFutureDiary
                ) {
                    context.config.disableFutureDiary = !disableFutureDiary
                    disableFutureDiary = !disableFutureDiary
                }

                SimpleCard(
                    currentTextUnit,
                    isPreview,
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
    class QuickSettingsViewModel : ViewModel() {
        var enablePhotoHighlight: MutableLiveData<Boolean> = MutableLiveData()
            private set

        fun toggle() {
            enablePhotoHighlight.value = enablePhotoHighlight.value != true
        }
    }
}