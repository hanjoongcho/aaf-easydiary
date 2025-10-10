package me.blog.korn123.easydiary.compose

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.navigationBarHeight
import me.blog.korn123.easydiary.extensions.showBetaFeatureMessage
import me.blog.korn123.easydiary.helper.AlarmWorkExecutor
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

class QuickSettingsActivity : EasyDiaryComposeBaseActivity() {


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            mSettingsViewModel = initSettingsViewModel()
            val topAppBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
            val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
            AppTheme {
                Scaffold(
//                    contentWindowInsets = WindowInsets(0, 0, 0, 0), // 기본 inset 제거
                    contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                    topBar = {
                        EasyDiaryActionBar(
                            title = "QuickSettings"
                            , subTitle = "\uD83D\uDCF1\uD83D\uDC4B Shake the device to open"
                            , scrollBehavior = scrollBehavior
                        ) {
                            finishActivityWithTransition()
                        }
                    },
                    containerColor = Color(config.screenBackgroundColor),
                    content = { innerPadding ->
                        QuickSettings(
                            Modifier
                                .padding(innerPadding)
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                            ,
                            state = rememberLazyGridState()
                        )
                    },
                    floatingActionButton = {
                        Box(modifier = Modifier.padding(bottom = bottomPadding)) {
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
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                )
            }
        }
        showBetaFeatureMessage()
    }


    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @Composable
    fun QuickSettings(
        modifier: Modifier = Modifier,
        state: LazyGridState = rememberLazyGridState()
    ) {
        val context = LocalContext.current
        var disableFutureDiary by remember { mutableStateOf(context.config.disableFutureDiary) }
        var enableWelcomeDashboardPopup by remember { mutableStateOf(context.config.enableWelcomeDashboardPopup) }
        var enableMarkdown by remember { mutableStateOf(context.config.enableMarkdown) }
        val settingCardModifier = Modifier.fillMaxWidth()
        val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
            context.config.enableCardViewPolicy
        )
        val maxItemsInEachRow = when {
            LocalInspectionMode.current -> 1
            isLandScape() -> 2
            else -> 1
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(maxItemsInEachRow),
            modifier = modifier
                .fillMaxWidth()
                .background(Color(context.config.screenBackgroundColor)),
            state = state,
        ) {
            item {
                SwitchCard(
                    title = stringResource(R.string.markdown_setting_title),
                    description = stringResource(R.string.markdown_setting_summary),
                    modifier = settingCardModifier,
                    isOn = enableMarkdown,
                    enableCardViewPolicy = enableCardViewPolicy
                ) {
                    context.config.enableMarkdown = !enableMarkdown
                    enableMarkdown = !enableMarkdown
                }
            }
            item {
                SwitchCard(
                    stringResource(R.string.enable_welcome_dashboard_popup_title),
                    stringResource(R.string.enable_welcome_dashboard_popup_description),
                    settingCardModifier,
                    enableWelcomeDashboardPopup,
                    enableCardViewPolicy = enableCardViewPolicy
                ) {
                    context.config.enableWelcomeDashboardPopup = !enableWelcomeDashboardPopup
                    enableWelcomeDashboardPopup = !enableWelcomeDashboardPopup
                }
            }
            item {
                var enablePhotoHighlight by remember { mutableStateOf(context.config.enablePhotoHighlight) }
                SwitchCard(
                    stringResource(R.string.enable_photo_highlight_title),
                    stringResource(R.string.enable_photo_highlight_description),
                    settingCardModifier,
                    isOn = enablePhotoHighlight,
                    enableCardViewPolicy = enableCardViewPolicy
                ) {
                    enablePhotoHighlight = enablePhotoHighlight.not()
                    context.config.enablePhotoHighlight = enablePhotoHighlight
                }
            }
            item {
                SwitchCard(
                    stringResource(R.string.enable_card_view_policy_title),
                    stringResource(R.string.enable_card_view_policy_summary),
                    settingCardModifier,
                    isOn = enableCardViewPolicy,
                    enableCardViewPolicy = enableCardViewPolicy
                ) {
                    context.config.enableCardViewPolicy = enableCardViewPolicy.not()
                    mSettingsViewModel.setEnableCardViewPolicy(context.config.enableCardViewPolicy)
                }
            }
            item {
                SwitchCard(
                    "미래일정 숨김",
                    "미래일정을 메인화면 목록에서 보이지 않도록 설정합니다.",
                    settingCardModifier,
                    disableFutureDiary,
                    enableCardViewPolicy = enableCardViewPolicy
                ) {
                    context.config.disableFutureDiary = !disableFutureDiary
                    disableFutureDiary = !disableFutureDiary
                }
            }
            if (BuildConfig.FLAVOR != "foss") {
                item {
                    val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
                    SimpleCard(
                        stringResource(id = R.string.sync_google_calendar_event_title),
                        stringResource(id = R.string.sync_google_calendar_event_summary),
                        modifier = settingCardModifier.padding(0.dp, 0.dp, 0.dp, bottomPadding.plus(70.dp)),
                        enableCardViewPolicy = enableCardViewPolicy,
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(heightDp = 1100)
//    @Preview(name = "Landscape Pixel 4 XL", device = "spec:width=1280dp,height=720dp")
    @Composable
    private fun QuickSettingsPreview() {
        mSettingsViewModel = initSettingsViewModel()

        AppTheme {
            Scaffold(
                topBar = {
                    EasyDiaryActionBar(title = "QuickSettings", subTitle = "\uD83D\uDCF1\uD83D\uDC4B Shake the device to open") {}
                },
                content = { innerPadding ->
                    QuickSettings(
                        Modifier.padding(innerPadding)
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { finishActivityWithTransition() },
                        containerColor = Color(LocalContext.current.config.primaryColor),
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
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


