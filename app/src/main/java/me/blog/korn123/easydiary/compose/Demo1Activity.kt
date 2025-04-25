package me.blog.korn123.easydiary.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

class Demo1Activity : EasyDiaryComposeBaseActivity() {


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
//            CollapsingAppBarFullScreen()
        }
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
            items(20) {
                SimpleCard(
                    stringResource(id = R.string.sync_google_calendar_event_title),
                    stringResource(id = R.string.sync_google_calendar_event_summary),
                    modifier = settingCardModifier.padding(0.dp, 0.dp, 0.dp, 0.dp),
                    enableCardViewPolicy = enableCardViewPolicy,
                ) {}
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CollapsingAppBarFullScreen() {
        val toolbarHeight = 56.dp
        val toolbarHeightPx = with(LocalDensity.current) { toolbarHeight.toPx() }

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val scrollState = rememberLazyListState()

        val animatedOffset by animateFloatAsState(
            targetValue = scrollBehavior.state.heightOffset.coerceIn(-toolbarHeightPx, 0f),
            animationSpec = tween(300),
            label = "AppBarOffset"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            // LazyColumn: 상태바까지 확장
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(top = 0.dp)
            ) {
                items(100) {
                    Text(
                        "Item #$it",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            // 앱바: Box 위에 오버레이
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, animatedOffset.toInt()) }
                    .fillMaxWidth()
                    .height(toolbarHeight + WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            ) {
                TopAppBar(
                    title = { Text("스크롤 앱바") },
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Blue)
                )
            }
        }
    }
}


