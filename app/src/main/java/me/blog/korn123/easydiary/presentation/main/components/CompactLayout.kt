package me.blog.korn123.easydiary.presentation.main.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.presentation.components.FastScroll
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

@Composable
fun CompactLayout(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    containerSize: IntSize,
    isDraggingThumb: Boolean,
    thumbVisible: Boolean,
    hideJob: Job?,
    currentQuery: String,
    delayTimeMillis: Long,
    durationMillis: Int,
    enableCardViewPolicy: Boolean,
    items: List<DiaryDomain>,
    thumbVisibleCallback: (thumbVisible: Boolean) -> Unit,
    isDraggingThumbCallback: (isDraggingThumb: Boolean) -> Unit,
    hideJobCallback: (job: Job) -> Unit,
    containerSizeCallback: (containerSize: IntSize) -> Unit,
    onQueryChange: (query: String) -> Unit = {},
    itemClickCallback: (diary: DiaryDomain) -> Unit = {},
    itemLongClickCallback: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var enablePhotoHighlight by remember { mutableStateOf(context.config.enablePhotoHighlight) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        enablePhotoHighlight = context.config.enablePhotoHighlight
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val photoHighlightMaxHeight = 160.dp + statusBarTop
    val photoHighlightScrollBehavior = rememberPhotoHighlightScrollBehavior(photoHighlightMaxHeight)
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier =
            modifier
                .padding(innerPadding)
                .fillMaxSize()
                .nestedScroll(photoHighlightScrollBehavior.nestedScrollConnection),
    ) {
        if (enablePhotoHighlight) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(photoHighlightScrollBehavior.currentHeight)
                        .graphicsLayer {
                            alpha = photoHighlightScrollBehavior.alpha
                        },
            ) {
                val currentTopPadding =
                    if (photoHighlightMaxHeight > 0.dp) {
                        statusBarTop * (photoHighlightScrollBehavior.currentHeight / photoHighlightMaxHeight)
                    } else {
                        0.dp
                    }
                PhotoHighlightCard(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = currentTopPadding),
                    height = 160.dp,
                    isVisible = enablePhotoHighlight,
                    onVisibilityChanged = { isVisible ->
                        enablePhotoHighlight = isVisible
                    },
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .onSizeChanged {
                            containerSizeCallback(it)
                        },
                contentPadding =
                    PaddingValues(
                        top = if (enablePhotoHighlight) 0.dp else statusBarTop,
                        bottom = bottomBarPadding,
                    ),
            ) {
                itemsIndexed(items) { _, diary ->
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
                containerHeightPx =
                    containerSize.height.toFloat().minus(
                        with(
                            LocalDensity.current,
                        ) {
                            bottomBarPadding.toPx().plus(
                                if (enablePhotoHighlight) 0f else statusBarTop.toPx(),
                            )
                        },
                    ),
                isDraggingThumb = isDraggingThumb,
                thumbVisible = thumbVisible,
                containerSize = containerSize,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = if (enablePhotoHighlight) 0.dp else statusBarTop,
                            bottom = bottomBarPadding,
                        ),
                showDebugCard = false,
                updateThumbVisible = { thumbVisibleCallback(it) },
                updateDraggingThumb = { isDraggingThumbCallback(it) },
                dragEndCallback = {
                    hideJob?.cancel()
                    coroutineScope.launch {
                        val job =
                            launch {
                                delay(delayTimeMillis)
                                if (!isDraggingThumb) {
                                    thumbVisibleCallback(false)
                                }
                            }
                        hideJobCallback(job)
                    }
                },
            )

            Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                MainBottomSheet(
                    title = "[Total: ${items.size}] category or title",
                    currentQuery = currentQuery,
                    enableCardViewPolicy = enableCardViewPolicy,
                ) { query ->
                    onQueryChange(query)
                }
            }
        }
    }
}

@Composable
fun CompactLayout2(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    containerSize: IntSize,
    isDraggingThumb: Boolean,
    thumbVisible: Boolean,
    hideJob: Job?,
    currentQuery: String,
    delayTimeMillis: Long,
    durationMillis: Int,
    enableCardViewPolicy: Boolean,
    items: List<DiaryDomain>,
    thumbVisibleCallback: (thumbVisible: Boolean) -> Unit,
    isDraggingThumbCallback: (isDraggingThumb: Boolean) -> Unit,
    hideJobCallback: (job: Job) -> Unit,
    containerSizeCallback: (containerSize: IntSize) -> Unit,
    onQueryChange: (query: String) -> Unit = {},
    itemClickCallback: (diary: DiaryDomain) -> Unit = {},
    itemLongClickCallback: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var enablePhotoHighlight by remember { mutableStateOf(context.config.enablePhotoHighlight) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        enablePhotoHighlight = context.config.enablePhotoHighlight
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val photoHighlightMaxHeight = 160.dp + statusBarTop
    val photoHighlightScrollBehavior = rememberPhotoHighlightScrollBehavior(photoHighlightMaxHeight)
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier =
            modifier
                .padding(innerPadding)
                .fillMaxSize()
                .nestedScroll(photoHighlightScrollBehavior.nestedScrollConnection),
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .onSizeChanged {
                            containerSizeCallback(it)
                        },
                contentPadding =
                    PaddingValues(
                        top = if (enablePhotoHighlight) 0.dp else statusBarTop,
                        bottom = bottomBarPadding,
                    ),
            ) {
                itemsIndexed(items) { index, diary ->
                    if (enablePhotoHighlight && index == 0) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(photoHighlightScrollBehavior.currentHeight)
                                    .graphicsLayer {
                                        alpha = photoHighlightScrollBehavior.alpha
                                    },
                        ) {
                            val currentTopPadding =
                                if (photoHighlightMaxHeight > 0.dp) {
                                    statusBarTop * (photoHighlightScrollBehavior.currentHeight / photoHighlightMaxHeight)
                                } else {
                                    0.dp
                                }
                            PhotoHighlightCard(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(top = currentTopPadding),
                                height = 160.dp,
                                isVisible = enablePhotoHighlight,
                                onVisibilityChanged = { isVisible ->
                                    enablePhotoHighlight = isVisible
                                },
                            )
                        }
                    }

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
                containerHeightPx =
                    containerSize.height.toFloat().minus(
                        with(
                            LocalDensity.current,
                        ) {
                            bottomBarPadding.toPx().plus(
                                if (enablePhotoHighlight) 0f else statusBarTop.toPx(),
                            )
                        },
                    ),
                isDraggingThumb = isDraggingThumb,
                thumbVisible = thumbVisible,
                containerSize = containerSize,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = if (enablePhotoHighlight) 0.dp else statusBarTop,
                            bottom = bottomBarPadding,
                        ),
                showDebugCard = false,
                updateThumbVisible = { thumbVisibleCallback(it) },
                updateDraggingThumb = { isDraggingThumbCallback(it) },
                dragEndCallback = {
                    hideJob?.cancel()
                    coroutineScope.launch {
                        val job =
                            launch {
                                delay(delayTimeMillis)
                                if (!isDraggingThumb) {
                                    thumbVisibleCallback(false)
                                }
                            }
                        hideJobCallback(job)
                    }
                },
            )

            Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                MainBottomSheet(
                    title = "[Total: ${items.size}] category or title",
                    currentQuery = currentQuery,
                    enableCardViewPolicy = enableCardViewPolicy,
                ) { query ->
                    onQueryChange(query)
                }
            }
        }
    }
}
