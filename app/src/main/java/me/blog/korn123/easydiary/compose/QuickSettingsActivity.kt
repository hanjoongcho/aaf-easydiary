package me.blog.korn123.easydiary.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.QuickSettingsActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getStatusBarColor
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.helper.AlarmWorkExecutor
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Alarm
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

        // https://velog.io/@hyemdooly/enableEdgeToEdge-%EB%82%B4%EB%B6%80-%EC%BD%94%EB%93%9C-%EC%95%8C%EA%B3%A0-%EC%93%B0%EA%B8%B0
//        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.auto(config.primaryColor, config.primaryColor)
//        )

        window.statusBarColor = getStatusBarColor(config.primaryColor)

        setContent {
            val viewModel: QuickSettingsViewModel by viewModels()
            viewModel.enablePhotoHighlight.value = config.enablePhotoHighlight

            AppTheme {
                Scaffold(
//                    topBar = {
//                        EasyDiaryActionBar(subTitle = "Shake the device to open the quick settings screen.") {
//                            onBackPressed()
//                        }
//                    },
                    content = { innerPadding ->
                        QuickSettings(
                            viewModel,
                            Modifier.padding(innerPadding)
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { onBackPressed() },
                            containerColor = Color(config.primaryColor),
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        pauseLock()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun QuickSettings(viewModel: QuickSettingsViewModel, modifier: Modifier = Modifier) {
        val context = LocalContext.current
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.background(Color(context.config.screenBackgroundColor))

        ) {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(Color(context.config.primaryColor)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "\uD83D\uDCF1\uD83D\uDC4B This screen is the quick settings screen that activates when you shake the device.",
                        style = TextStyle(
                            fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = TextUnit(currentTextUnit.value, TextUnitType.Sp),
                        ),
                    )
                }
            }

            Column(
                modifier = modifier
//                    .fillMaxSize()
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)

                    .padding(0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                FlowRow(
                    modifier = Modifier,
//                    .padding(3.dp, 3.dp)
//                    .fillMaxWidth(1f)
//                    .fillMaxHeight(1f),
//                horizontalArrangement = Arrangement.spacedBy(3.dp),
//                verticalArrangement = Arrangement.spacedBy(3.dp),
//                overflow = FlowRowOverflow.Clip,
                    maxItemsInEachRow = 1
                ) {

                    val settingCardModifier =
                        if (enableCardViewPolicy) Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(
                                3.dp,
                                3.dp
                            ) else Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(1.dp, 1.dp)

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
                    ) {
                        val alarm = Alarm().apply {
                            sequence = Int.MAX_VALUE
                            workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                            label = "Quick Settings"
                        }
                        AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
                    }
//                    SimpleCard(
//                        "Quick Settings With Leagacy View",
//                        null,
//                        settingCardModifier,
//                    ) {
//                        TransitionHelper.startActivityWithTransition(
//                            this@QuickSettingsActivity,
//                            Intent(this@QuickSettingsActivity, QuickSettingsActivity::class.java)
//                        )
//                        finish()
//                    }
                }
            }

        }
    }

    @Preview(heightDp = 1200)
    @Composable
    private fun QuickSettingsPreview() {
        AppTheme {
            Scaffold(
//                topBar = {
//                    EasyDiaryActionBar(subTitle = "Shake the device to open the quick settings screen.") {
//                        onBackPressed()
//                    }
//                },
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