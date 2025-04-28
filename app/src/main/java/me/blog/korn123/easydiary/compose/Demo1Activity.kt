package me.blog.korn123.easydiary.compose

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isColorLight
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.updateSystemStatusBarColor
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
        val mode = intent.getIntExtra("mode" ,1)
        setContent {
            mSettingsViewModel = initSettingsViewModel()
            val topAppBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
            val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp

            when (mode) {
                1 -> {
                    AppTheme {
                        Scaffold(
//                    contentWindowInsets = WindowInsets(0, 0, 0, 0), // 기본 inset 제거
                            contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                            topBar = {
                                EasyDiaryActionBar(
                                    title = "QuickSettings",
                                    scrollBehavior = scrollBehavior
                                ) {
                                    finishActivityWithTransition()
                                }
                            },
                            containerColor = Color(config.screenBackgroundColor),
                            content = { innerPadding ->
                                QuickSettings(
                                    Modifier
                                        .padding(innerPadding)
                                        .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                2 -> {
                    FullScreen()
                }
                3 -> {
                    CollapsingTopAppBarFullScreen()
                }
                4 -> {
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    CollapsingTopAppBarFullScreenTransparentStatusBar()
                }
            }
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
    @Suppress("DEPRECATION")
    fun FullScreen() {
        // enableEdgeToEdge()

        // Control directly without using enableEdgeToEdge function
        WindowCompat.setDecorFitsSystemWindows(window, false) // 시스템 창(상태바, 내비게이션바) 위로 그리기
        LocalActivity.current?.updateSystemStatusBarColor()

        mSettingsViewModel = initSettingsViewModel()
        val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp

        AppTheme {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
                containerColor = Color(config.screenBackgroundColor),
                content = { innerPadding ->
                    val context = LocalContext.current
                    val settingCardModifier = Modifier.fillMaxWidth()
                    val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
                        context.config.enableCardViewPolicy
                    )
                    val maxItemsInEachRow = when {
                        LocalInspectionMode.current -> 1
                        isLandScape() -> 2
                        else -> 1
                    }
                    val topPadding =
                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    val itemSize = 20
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(maxItemsInEachRow),
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()
                            .background(Color(context.config.screenBackgroundColor)),
                        state = rememberLazyGridState(),
                    ) {
                        items(itemSize) { index ->
                            SimpleCard(
                                stringResource(id = R.string.sync_google_calendar_event_title),
                                stringResource(id = R.string.sync_google_calendar_event_summary),
                                modifier = settingCardModifier.padding(
                                    0.dp,
                                    if (index < maxItemsInEachRow) topPadding else 0.dp,
                                    0.dp,
                                    if (index >= itemSize.minus(maxItemsInEachRow)) 72.dp.plus(
                                        bottomPadding
                                    ) else 0.dp
                                ),
                                enableCardViewPolicy = enableCardViewPolicy,
                            ) {}
                        }
                    }
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CollapsingTopAppBarFullScreenTransparentStatusBar() {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val topBarState = scrollBehavior.state
        val topBarHeight = remember { mutableStateOf(0.dp) }
        val a = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = { Text("투명 상태 바") },
                    navigationIcon = {
                        IconButton(onClick = { /* 뒤로 가기 액션 */ }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로 가기")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.onSizeChanged { size ->
                        topBarHeight.value = size.height.dp
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, // 앱 바 배경을 투명하게
                        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer, // 스크롤 시 배경색 변경 가능
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
//                    elevation = if (topBarState.overlappedFraction > 0f) 4.dp else 0.dp // 스크롤 시 그림자 표시
                )
            },
            content = { paddingValues ->
                val contentPaddingTop by remember {
                    derivedStateOf {
                        if (topBarState.overlappedFraction > 0f) {
                           a // 상태 바 높이만큼 패딩
                        } else {
                            paddingValues.calculateTopPadding() // 앱 바가 완전히 보일 때 Scaffold 기본 패딩
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = paddingValues.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
                        .padding(bottom = paddingValues.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = contentPaddingTop)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
                    }
                    items(100) { index ->
                        Text(text = "아이템 $index", modifier = Modifier.padding(16.dp))
                        Divider()
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CollapsingTopAppBarFullScreen() {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val topBarState = scrollBehavior.state
        val topBarHeight = remember { mutableStateOf(0.dp) }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = { Text("전체 화면 확장 앱 바") },
                    navigationIcon = {
                        IconButton(onClick = { /* 뒤로 가기 액션 */ }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로 가기")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.onSizeChanged { size ->
                        topBarHeight.value = size.height.dp
                    }
                )
            },
            content = { paddingValues ->
                val contentPaddingTop by remember {
                    derivedStateOf {
                        if (topBarState.overlappedFraction > 0f) {
                            0.dp // 앱 바가 일부라도 보이면 패딩 없음
                        } else {
                            paddingValues.calculateTopPadding() // 앱 바가 완전히 숨겨지면 Scaffold 기본 패딩 적용
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = paddingValues.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr))
                        .padding(bottom = paddingValues.calculateBottomPadding()),
                    contentPadding = PaddingValues(top = contentPaddingTop)
                ) {
                    items(100) { index ->
                        Text(text = "아이템 $index", modifier = Modifier.padding(16.dp))
                        Divider()
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CollapsingTopAppBarLazyColumn() {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val listState = rememberScrollState()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = { Text("스크롤하여 숨겨지는 앱 바") },
                    navigationIcon = {
                        IconButton(onClick = { /* 뒤로 가기 액션 */ }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로 가기")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            content = { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(top = 0.dp) // TopAppBar 영역까지 콘텐츠 확장
                ) {
                    items(100) { index ->
                        Text(text = "아이템 $index", modifier = Modifier.padding(16.dp))
                        Divider()
                    }
                }
            }
        )
    }


}


