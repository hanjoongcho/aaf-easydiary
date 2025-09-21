package me.blog.korn123.easydiary.compose

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.updateSystemStatusBarColor
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.theme.AppTheme
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp

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
                    val items = List(1000) { "Item #$it" }
                    FastScrollLazyColumnSample(items)
                }
            }
        }

    }


    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun  NestedScrollConnection() {
        mSettingsViewModel = initSettingsViewModel()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp

        AppTheme {
            Scaffold(
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
                    val itemSize = 20
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(maxItemsInEachRow),
                        modifier = Modifier
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
                                modifier = settingCardModifier.padding(
                                    0.dp,
                                    0.dp,
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
    fun NestedScrollConnectionWithAutoInsets() {
        mSettingsViewModel = initSettingsViewModel()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

        AppTheme {
            Scaffold(
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
                    val itemSize = 20
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(maxItemsInEachRow),
                        modifier = Modifier
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
                                modifier = settingCardModifier.padding(
                                    0.dp,
                                    0.dp,
                                    0.dp,
                                    if (index >= itemSize.minus(maxItemsInEachRow)) 72.dp else 0.dp
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
                        modifier = Modifier.size(40.dp)
                    ) {
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

    @Composable
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

    @Composable
    fun FastScrollLazyColumnSample(
        items: List<String>,
        modifier: Modifier = Modifier
    ) {
        LocalActivity.current?.updateSystemStatusBarColor()
        AppTheme {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical),
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


                    val listState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()

                    // 컨테이너 높이(픽셀)
                    var containerSize by remember { mutableStateOf(IntSize.Zero) }
                    val density = LocalDensity.current

                    // 토글: 썸을 누르고 있는지
                    var isDraggingThumb by remember { mutableStateOf(false) }
                    // 썸의 y-offset (픽셀)
                    var thumbY by remember { mutableStateOf(0f) }
                    // 버블 텍스트 (옵션)
                    var bubbleText by remember { mutableStateOf<String?>(null) }
                    // 썸 애니메이션/노출은 간단하게 상태로 제어 가능
                    Box(modifier = modifier.padding(innerPadding).fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .onSizeChanged { containerSize = it }
                        ) {
                            itemsIndexed(items) { index, item ->
                                // 리스트 아이템 예시
//                                Text(
//                                    text = "$index: $item",
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(16.dp)
//                                )
//                                Divider()

                                SimpleCard(
                                    "$index: $item",
                                    stringResource(id = R.string.sync_google_calendar_event_summary),
                                    modifier = settingCardModifier.padding(
                                        0.dp,
                                        0.dp,
                                        0.dp,
                                        0.dp
                                    ),
                                    enableCardViewPolicy = enableCardViewPolicy,
                                ) {}
                            }
                        }

                        // --- Fast Scroll 트랙 + 썸 + 버블 ---
                        if (containerSize.height > 0) {
                            val layoutInfo = listState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount.coerceAtLeast(1)
                            val containerHeightPx = containerSize.height.toFloat()


                            // 보이는 첫 아이템 높이로 평균 높이 추정
                            val itemHeight = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1
                            val firstIndex = listState.firstVisibleItemIndex
                            val firstOffset = listState.firstVisibleItemScrollOffset

                            val totalContentHeightPx = (totalItems * itemHeight).toFloat()
                            val scrollablePx = (totalContentHeightPx - containerHeightPx).coerceAtLeast(1f)

                            val scrolledPx = (firstIndex * itemHeight + firstOffset).toFloat()
                            val progress = (scrolledPx / scrollablePx).coerceIn(0f, 1f)

                            val visibleCount = layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
                            val minThumbHeightPx = with(density) { 24.dp.toPx() }
                            val thumbHeightPx = (containerHeightPx * (visibleCount.toFloat() / totalItems))
                                .coerceAtLeast(minThumbHeightPx)
                            val baseThumbY = progress * (containerHeightPx - thumbHeightPx)
                            val drawThumbY = if (isDraggingThumb) thumbY.coerceIn(0f, containerHeightPx - thumbHeightPx) else baseThumbY

                            // --- Fast Scroll 트랙 + 썸 ---
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(40.dp) // 트랙+터치 영역
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 4.dp)
                                    .pointerInput(totalItems) {
                                        detectDragGestures(
                                            onDragStart = {
                                                isDraggingThumb = true
                                                bubbleText = items.getOrNull(firstIndex)?.firstOrNull()?.toString()
                                            },
                                            onDrag = { change, drag ->
                                                change.consume()
                                                thumbY = (thumbY + drag.y).coerceIn(0f, containerHeightPx - thumbHeightPx)
                                                val proportion = (thumbY + thumbHeightPx / 2f) / containerHeightPx
                                                val target = (proportion * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)
                                                coroutineScope.launch { listState.scrollToItem(target) }
//                                                bubbleText = items.getOrNull(target)?.firstOrNull()?.toString() ?: ""
                                                bubbleText = items.getOrNull(target) ?: ""
                                            },
                                            onDragEnd = { isDraggingThumb = false; bubbleText = null },
                                            onDragCancel = { isDraggingThumb = false; bubbleText = null }
                                        )
                                    }
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxHeight()
                                        .width(4.dp)
                                        .align(Alignment.CenterEnd)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                )
                                Box(
                                    Modifier
                                        .offset { IntOffset(0, drawThumbY.toInt()) }
                                        .width(12.dp)
                                        .height(with(density) { thumbHeightPx.toDp() })
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }

                            // --- 버블: ***왼쪽 방향*** ---
                            if (isDraggingThumb && bubbleText != null) {
                                Box(
                                    modifier = Modifier
                                        .offset {
                                            // 썸 왼쪽 80.dp 정도 위치
                                            val bubbleX = (containerSize.width -
                                                    with(density) { 80.dp.toPx() } -
                                                    with(density) { 40.dp.toPx() }).toInt().coerceAtLeast(0)
                                            val bubbleY = (drawThumbY - 24f).toInt().coerceIn(0, containerSize.height - 48)
                                            IntOffset(bubbleX, bubbleY)
                                        }
//                                        .size(64.dp)
//                                        .clip(CircleShape)
                                        .background(Color(LocalContext.current.config.primaryColor))
//                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                                        ,
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bubbleText ?: "",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}


