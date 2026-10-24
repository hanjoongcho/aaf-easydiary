package me.blog.korn123.easydiary.presentation.main.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.presentation.components.FastScroll

@Composable
fun MediumLayout(
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
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier =
            Modifier
                .fillMaxWidth(),
    ) {
        Column(
            modifier =
                modifier
                    .weight(0.4f)
                    .padding(innerPadding)
                    .fillMaxSize(),
        ) {
            PhotoHighlightCard(onVisibilityChanged = { _ ->
            })
        }
        Column(modifier = Modifier.weight(0.6f)) {
            Box(
                modifier = Modifier.weight(1f),
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
                            bottom =
                                WindowInsets.navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding(),
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
                                WindowInsets.navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding()
                                    .toPx()
                            },
                        ),
                    isDraggingThumb = isDraggingThumb,
                    thumbVisible = thumbVisible,
                    containerSize = containerSize,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(
                                bottom =
                                    WindowInsets.navigationBars
                                        .asPaddingValues()
                                        .calculateBottomPadding(),
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

                androidx.compose.animation.AnimatedVisibility(
                    visible = !thumbVisible,
                    enter = fadeIn(animationSpec = tween(durationMillis)),
                    exit = fadeOut(animationSpec = tween(durationMillis)),
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter),
                ) {
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
}
