package me.blog.korn123.easydiary.compose

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.simplemobiletools.commons.extensions.toast
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getThemeId
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.updateSystemStatusBarColor
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.components.FastScroll
import me.blog.korn123.easydiary.ui.components.LegacyDiaryItemCard
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

class Demo1Activity : EasyDiaryComposeBaseActivity() {
    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeId())

        super.onCreate(savedInstanceState)
        val mode = intent.getIntExtra("mode", 1)
        setContent {
            mSettingsViewModel = initSettingsViewModel()
            when (mode) {
                1 -> {
                    NestedScrollConnection()
                }

                2 -> {
                    FullScreen()
                }

                3 -> {
                    NestedScrollConnectionWithAutoInsets()
                }

                4 -> {
                    val items = List(50) { "Item #$it" }
                    FastScrollLazyColumnSample(items)
                }

                5 -> {
                    val items = EasyDiaryDbHelper.findDiary(null)
                    FastScrollLazyColumnSample2(items)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setTheme(getThemeId())
    }

    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NestedScrollConnection() {
        mSettingsViewModel = initSettingsViewModel()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val bottomPadding =
            if (isVanillaIceCreamPlus()) {
                WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            } else {
                0.dp
            }

        AppTheme {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                topBar = {
                    EasyDiaryActionBar(
                        title = "QuickSettings",
                        scrollBehavior = scrollBehavior,
                    ) {
                        finishActivityWithTransition()
                    }
                },
                containerColor = Color(config.screenBackgroundColor),
                content = { innerPadding ->
                    val context = LocalContext.current
                    val settingCardModifier = Modifier.fillMaxWidth()
                    val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
                        context.config.enableCardViewPolicy,
                    )
                    val maxItemsInEachRow =
                        when {
                            LocalInspectionMode.current -> 1
                            isLandScape() -> 2
                            else -> 1
                        }
                    val itemSize = 20
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(maxItemsInEachRow),
                        modifier =
                            Modifier
                                .padding(innerPadding)
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                                .fillMaxWidth()
                                .background(Color(context.config.screenBackgroundColor)),
                        state = rememberLazyGridState(),
                    ) {
                        items(itemSize) { index ->
                            SimpleCard(
                                stringResource(id = R.string.sync_google_calendar_event_title),
                                stringResource(id = R.string.sync_google_calendar_event_summary),
                                modifier =
                                    settingCardModifier.padding(
                                        0.dp,
                                        0.dp,
                                        0.dp,
                                        if (index >= itemSize.minus(maxItemsInEachRow)) {
                                            72.dp.plus(
                                                bottomPadding,
                                            )
                                        } else {
                                            0.dp
                                        },
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
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cross),
                                contentDescription = "Finish Activity",
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
    fun NestedScrollConnectionWithAutoInsets() {
        mSettingsViewModel = initSettingsViewModel()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

        AppTheme {
            Scaffold(
                topBar = {
                    EasyDiaryActionBar(
                        title = "QuickSettings",
                        scrollBehavior = scrollBehavior,
                    ) {
                        finishActivityWithTransition()
                    }
                },
                containerColor = Color(config.screenBackgroundColor),
                content = { innerPadding ->
                    val context = LocalContext.current
                    val settingCardModifier = Modifier.fillMaxWidth()
                    val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
                        context.config.enableCardViewPolicy,
                    )
                    val maxItemsInEachRow =
                        when {
                            LocalInspectionMode.current -> 1
                            isLandScape() -> 2
                            else -> 1
                        }
                    val itemSize = 20
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(maxItemsInEachRow),
                        modifier =
                            Modifier
                                .padding(innerPadding)
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                                .fillMaxWidth()
                                .background(Color(context.config.screenBackgroundColor)),
                        state = rememberLazyGridState(),
                    ) {
                        items(itemSize) { index ->
                            SimpleCard(
                                stringResource(id = R.string.sync_google_calendar_event_title),
                                stringResource(id = R.string.sync_google_calendar_event_summary),
                                modifier =
                                    settingCardModifier.padding(
                                        0.dp,
                                        0.dp,
                                        0.dp,
                                        if (index >= itemSize.minus(maxItemsInEachRow)) 72.dp else 0.dp,
                                    ),
                                enableCardViewPolicy = enableCardViewPolicy,
                            ) {}
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { finishActivityWithTransition() },
                        containerColor = Color(config.primaryColor),
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "Finish Activity",
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.Center,
            )
        }
    }

    @Composable
    fun FullScreen() {
        // enableEdgeToEdge()

        // Control directly without using enableEdgeToEdge function
        WindowCompat.setDecorFitsSystemWindows(window, false) // 시스템 창(상태바, 내비게이션바) 위로 그리기
        LocalActivity.current?.updateSystemStatusBarColor()

        mSettingsViewModel = initSettingsViewModel()
        val bottomPadding =
            if (isVanillaIceCreamPlus()) {
                WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            } else {
                0.dp
            }

        AppTheme {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
                containerColor = Color(config.screenBackgroundColor),
                content = { innerPadding ->
                    val context = LocalContext.current
                    val settingCardModifier = Modifier.fillMaxWidth()
                    val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
                        context.config.enableCardViewPolicy,
                    )
                    val maxItemsInEachRow =
                        when {
                            LocalInspectionMode.current -> 1
                            isLandScape() -> 2
                            else -> 1
                        }
                    val topPadding =
                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    val itemSize = 20
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(maxItemsInEachRow),
                        modifier =
                            Modifier
                                .padding(innerPadding)
                                .fillMaxWidth()
                                .background(Color(context.config.screenBackgroundColor)),
                        state = rememberLazyGridState(),
                    ) {
                        items(itemSize) { index ->
                            SimpleCard(
                                stringResource(id = R.string.sync_google_calendar_event_title),
                                stringResource(id = R.string.sync_google_calendar_event_summary),
                                modifier =
                                    settingCardModifier.padding(
                                        0.dp,
                                        if (index < maxItemsInEachRow) topPadding else 0.dp,
                                        0.dp,
                                        if (index >= itemSize.minus(maxItemsInEachRow)) {
                                            72.dp.plus(
                                                bottomPadding,
                                            )
                                        } else {
                                            0.dp
                                        },
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
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cross),
                                contentDescription = "Finish Activity",
                            )
                        }
                    }
                },
                floatingActionButtonPosition = FabPosition.Center,
            )
        }
    }

    @Composable
    fun FastScrollLazyColumnSample(
        items: List<String>,
        modifier: Modifier = Modifier,
    ) {
        LocalActivity.current?.updateSystemStatusBarColor()
        AppTheme {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical),
                containerColor = Color(config.screenBackgroundColor),
                content = { innerPadding ->
                    val context = LocalContext.current
                    val coroutineScope = rememberCoroutineScope()
                    val settingCardModifier = Modifier.fillMaxWidth()
                    val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
                        context.config.enableCardViewPolicy,
                    )
                    val listState = rememberLazyListState()
                    var thumbVisible by remember { mutableStateOf(false) }
                    var containerSize by remember { mutableStateOf(IntSize.Zero) } // 컨테이너 높이(픽셀)
                    var isDraggingThumb by remember { mutableStateOf(false) } // 토글: 썸을 누르고 있는지
                    var hideJob: Job? by remember { mutableStateOf(null) }
                    val delayTimeMillis = 1500L

                    // 스크롤 이벤트 감지
                    LaunchedEffect(listState) {
                        snapshotFlow { listState.isScrollInProgress }
                            .collect { isScrolling ->
                                if (isScrolling) {
                                    hideJob?.cancel()
                                    thumbVisible = true
                                } else {
                                    hideJob?.cancel()
                                    hideJob =
                                        launch {
                                            delay(delayTimeMillis)
                                            if (!isDraggingThumb) thumbVisible = false
                                        }
                                }
                            }
                    }

                    Box(
                        modifier =
                            modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .onSizeChanged { containerSize = it },
                        ) {
                            itemsIndexed(items) { index, item ->
                                SimpleCard(
                                    "$index: $item",
                                    stringResource(id = R.string.sync_google_calendar_event_summary),
                                    modifier =
                                        settingCardModifier.padding(
                                            0.dp,
                                            0.dp,
                                            0.dp,
                                            0.dp,
                                        ),
                                    enableCardViewPolicy = enableCardViewPolicy,
                                ) {}
                            }
                        }

                        FastScroll(
                            items = items,
                            listState = listState,
                            containerHeightPx = containerSize.height.toFloat(),
                            isDraggingThumb = isDraggingThumb,
                            thumbVisible = thumbVisible,
                            containerSize = containerSize,
                            modifier =
                                Modifier
                                    .align(Alignment.TopEnd),
                            showDebugCard = true,
                            updateThumbVisible = { thumbVisible = it },
                            updateDraggingThumb = { isDraggingThumb = it },
                            dragEndCallback = {
                                hideJob?.cancel()
                                coroutineScope.launch {
                                    hideJob =
                                        launch {
                                            delay(delayTimeMillis)
                                            if (!isDraggingThumb) thumbVisible = false
                                        }
                                }
                            },
                        )
                    }
                },
            )
        }
    }

    @Composable
    fun FastScrollLazyColumnSample2(
        items: List<Diary>,
        modifier: Modifier = Modifier,
    ) {
        LocalActivity.current?.updateSystemStatusBarColor()
        AppTheme {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical),
                containerColor = Color(config.screenBackgroundColor),
                content = { innerPadding ->
                    val context = LocalContext.current
                    val activity = LocalActivity.current
                    val coroutineScope = rememberCoroutineScope()
                    val settingCardModifier = Modifier.fillMaxWidth()
                    val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
                        context.config.enableCardViewPolicy,
                    )
                    val listState = rememberLazyListState()
                    var thumbVisible by remember { mutableStateOf(false) }
                    var containerSize by remember { mutableStateOf(IntSize.Zero) } // 컨테이너 높이(픽셀)
                    var isDraggingThumb by remember { mutableStateOf(false) } // 토글: 썸을 누르고 있는지
                    var hideJob: Job? by remember { mutableStateOf(null) }
                    val delayTimeMillis = 1500L

                    // 스크롤 이벤트 감지
                    LaunchedEffect(listState) {
                        snapshotFlow { listState.isScrollInProgress }
                            .collect { isScrolling ->
                                if (isScrolling) {
                                    hideJob?.cancel()
                                    thumbVisible = true
                                } else {
                                    hideJob?.cancel()
                                    hideJob =
                                        launch {
                                            delay(delayTimeMillis)
                                            if (!isDraggingThumb) thumbVisible = false
                                        }
                                }
                            }
                    }

                    fun itemClickCallback(diary: Diary) {
                        activity?.toast("itemClickCallback: ${diary.title}")
                    }

                    fun itemLongClickCallback() {
                        activity?.toast("itemLongClickCallback")
                    }

                    Box(
                        modifier =
                            modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .onSizeChanged { containerSize = it },
                        ) {
                            itemsIndexed(items) { index, diary ->
                                Card(
                                    shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
                                    colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
                                    modifier = (
                                        if (enableCardViewPolicy) {
                                            modifier.padding(
                                                HORIZONTAL_PADDING.dp,
                                                VERTICAL_PADDING.dp,
                                            )
                                        } else {
                                            modifier
                                                .padding(1.dp, 1.dp)
                                        }
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
                                ) {
                                    LegacyDiaryItemCard(
                                        diary = diary,
                                        itemClickCallback = { itemClickCallback(it) },
                                        itemLongClickCallback = { itemLongClickCallback() },
                                    )
                                }
                            }
                        }

                        FastScroll(
                            items = items,
                            listState = listState,
                            containerHeightPx = containerSize.height.toFloat(),
                            isDraggingThumb = isDraggingThumb,
                            thumbVisible = thumbVisible,
                            containerSize = containerSize,
                            modifier =
                                Modifier
                                    .align(Alignment.TopEnd),
                            showDebugCard = false,
                            updateThumbVisible = { thumbVisible = it },
                            updateDraggingThumb = { isDraggingThumb = it },
                            dragEndCallback = {
                                hideJob?.cancel()
                                coroutineScope.launch {
                                    hideJob =
                                        launch {
                                            delay(delayTimeMillis)
                                            if (!isDraggingThumb) thumbVisible = false
                                        }
                                }
                            },
                        )
                    }
                },
            )
        }
    }
}
