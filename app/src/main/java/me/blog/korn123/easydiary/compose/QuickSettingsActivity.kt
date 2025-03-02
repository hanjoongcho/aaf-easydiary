package me.blog.korn123.easydiary.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.helper.AlarmWorkExecutor
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.ui.components.CardContainer
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

class QuickSettingsActivity : EasyDiaryComposeBaseActivity() {


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://velog.io/@hyemdooly/enableEdgeToEdge-%EB%82%B4%EB%B6%80-%EC%BD%94%EB%93%9C-%EC%95%8C%EA%B3%A0-%EC%93%B0%EA%B8%B0
//        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.auto(config.primaryColor, config.primaryColor)
//        )

        setContent {
            val viewModel: QuickSettingsViewModel by viewModels()
            viewModel.enablePhotoHighlight.value = config.enablePhotoHighlight

            AppTheme {
                Scaffold(
                    topBar = {
                        EasyDiaryActionBar(title = "QuickSettings", subTitle = "\uD83D\uDCF1\uD83D\uDC4B Shake the device to open") {
                            finishActivityWithTransition()
                        }
                    },
                    content = { innerPadding ->
                        QuickSettings(
                            viewModel,
                            Modifier.padding(innerPadding)
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { finishActivityWithTransition() },
                            containerColor = Color(config.primaryColor),
                            contentColor = Color.White,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(8.dp),
                            modifier = Modifier.size(40.dp)


                        ) {
//                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite Icon")
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cross),
                                contentDescription = "Finish Activity"
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center,
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
    fun QuickSettings(viewModel: QuickSettingsViewModel, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val isOn: Boolean by viewModel.enablePhotoHighlight.observeAsState(context.config.enablePhotoHighlight)
        var disableFutureDiary by remember { mutableStateOf(context.config.disableFutureDiary) }
        var enableWelcomeDashboardPopup by remember { mutableStateOf(context.config.enableWelcomeDashboardPopup) }
        var enableMarkdown by remember { mutableStateOf(context.config.enableMarkdown) }
        var enableCardViewPolicy by remember { mutableStateOf(context.config.enableCardViewPolicy) }
        val maxItemsInEachRow = when {
            LocalInspectionMode.current -> 2
            isLandScape() -> 2
            else -> 1
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color(context.config.screenBackgroundColor))
        ) {
            CardContainer(enableCardViewPolicy) {
                FlowRow(
                    modifier = modifier.fillMaxHeight(1f).fillMaxWidth(),
//                    .padding(3.dp, 3.dp)
//                    .fillMaxWidth(1f)
//                horizontalArrangement = Arrangement.spacedBy(3.dp),
//                verticalArrangement = Arrangement.spacedBy(3.dp),
//                overflow = FlowRowOverflow.Clip,
                    maxItemsInEachRow = maxItemsInEachRow
                ) {
                    // Pass modifier using mutableState to recompose when enableCardViewPolicy is changed.
                    val settingCardModifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                    SwitchCard(
                        stringResource(R.string.markdown_setting_title),
                        stringResource(R.string.markdown_setting_summary),
                        settingCardModifier,
                        enableMarkdown,
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
                        settingCardModifier.padding(0.dp, 0.dp, 0.dp, 70.dp),
                        enableCardViewPolicy
                    ) {
                        val alarm = Alarm().apply {
                            sequence = Int.MAX_VALUE
                            workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                            label = "Quick Settings"
                        }
                        AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
                    }
                }
            }

        }
    }

    @Preview(heightDp = 1100)
//    @Preview(name = "Landscape Pixel 4 XL", device = "spec:width=1280dp,height=720dp")
    @Composable
    private fun QuickSettingsPreview() {
        AppTheme {
            Scaffold(
                topBar = {
                    EasyDiaryActionBar(title = "QuickSettings", subTitle = "\uD83D\uDCF1\uD83D\uDC4B Shake the device to open") {}
                },
                content = { innerPadding ->
                    QuickSettings(
                        viewModel(),
                        Modifier.padding(innerPadding)
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { onBackPressed() },
                        containerColor = Color(LocalContext.current.config.primaryColor),
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                        modifier = Modifier.size(40.dp)


                    ) {
//                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite Icon")
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "액션 아이콘"
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.Center,
            )

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


