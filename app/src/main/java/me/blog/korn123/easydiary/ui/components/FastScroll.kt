package me.blog.korn123.easydiary.ui.components

import androidx.annotation.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.extensions.config

@Composable
fun FastScroll(
    items: List<String>,
    listState: LazyListState,
    containerHeightPx: Float,
    isDraggingThumb: Boolean,
    containerSize: IntSize,
    thumbVisible: Boolean,
    showDebugCard: Boolean = false,
    modifier: Modifier,
    updateThumbVisible: (thumbVisible: Boolean) -> Unit,
    updateDraggingThumb: (isDraggingThumb: Boolean) -> Unit,
    dragEndCallback: () -> Unit,
) {

    // --- Fast Scroll 트랙 + 썸 + 버블 ---
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var thumbY by remember { mutableFloatStateOf(0f) } // 썸의 y-offset (픽셀)
    var dragY by remember { mutableFloatStateOf(0f) }
    var proportion by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableFloatStateOf(0f) }
    var bubbleText by remember { mutableStateOf<String?>(null) } // 버블 텍스트 (옵션)

    val layoutInfo = remember { derivedStateOf { listState.layoutInfo } }
    val totalItems = layoutInfo.value.totalItemsCount.coerceAtLeast(1)

    // 보이는 첫 아이템 높이로 평균 높이 추정
    val itemHeight = layoutInfo.value.visibleItemsInfo.firstOrNull()?.size ?: 1

    val firstIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val firstOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

    val totalContentHeightPx = (totalItems * itemHeight).toFloat()
    val scrollablePx = (totalContentHeightPx - containerHeightPx).coerceAtLeast(1f)

    val scrolledPx = (firstIndex.value.times(itemHeight) + firstOffset.value).toFloat()
    val progress = (scrolledPx / scrollablePx).coerceIn(0f, 1f)

    val thumbHeightPx = with(density) { 30.dp.toPx() }
    val baseThumbY = progress * (containerHeightPx - thumbHeightPx)
    thumbY = if (isDraggingThumb) thumbY else baseThumbY
    val drawThumbY =
        if (isDraggingThumb) thumbY.coerceIn(0f, containerHeightPx - thumbHeightPx) else baseThumbY

    // --- Fast Scroll 트랙 + 썸 ---
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(30.dp) // 트랙 + 터치 영역
            .padding(end = 8.dp)
            .pointerInput(totalItems) {
                detectDragGestures(
                    onDragStart = {
                        updateThumbVisible(true)
                        updateDraggingThumb(true)
                        bubbleText =
                            items.getOrNull(firstIndex.value) ?: ""
                    },
                    onDrag = { change, drag ->
                        dragY = drag.y
                        change.consume()
                        thumbY = (thumbY + drag.y).coerceIn(
                            0f,
                            containerHeightPx - thumbHeightPx
                        )
                        proportion =
                            thumbY / (containerHeightPx - thumbHeightPx)
                        val target =
                            ((scrollablePx * proportion) / itemHeight).toInt()
                                .coerceIn(0, totalItems - 1)
                        offset = (scrollablePx * proportion) % itemHeight
                        coroutineScope.launch {
                            listState.scrollToItem(
                                target.coerceAtLeast(0), offset.toInt()
                            )
                        }
                        bubbleText = items.getOrNull(target) ?: ""
                    },
                    onDragEnd = {
                        updateDraggingThumb(false)
                        bubbleText = null
                        dragEndCallback()
                    },
                    onDragCancel = {
                        updateDraggingThumb(false)
                        bubbleText = null
                        dragEndCallback()
                    }
                )
            }
    ) {
        if (thumbVisible) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .padding(end = 4.dp)
                    .align(Alignment.CenterEnd)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.1f
                        )
                    )
            )

            Box(
                Modifier
                    .offset { IntOffset(0, drawThumbY.toInt()) }
                    .width(12.dp)
                    .align(Alignment.TopEnd)
                    .height(with(density) { thumbHeightPx.toDp() })
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }

    Card(
        shape = RoundedCornerShape(roundedCornerShapeSize.dp),
        colors = CardDefaults.cardColors(
            Color(LocalContext.current.config.backgroundColor).copy(
                alpha = 0.8f
            )
        ),

        ) {
        SimpleText(
            text = "" +
                    "firstIndex: ${firstIndex.value}\n" +
                    "firstOffset: ${firstOffset.value}\n" +
                    "offset: $offset\n" +
                    "proportion: $proportion\n" +
                    "scrollablePx: $scrollablePx\n" +
                    "scrolledPx: $scrolledPx\n" +
                    "progress: $progress\n" +
                    "baseThumbY: $baseThumbY\n" +
                    "drawThumbY: $drawThumbY\n" +
                    "dragY: $dragY\n" +
                    "thumbY: $thumbY\n" +
                    "",
//                                    alpha = 0.8f,
            modifier = Modifier
//                                        .align(Alignment.TopStart)
                .padding(16.dp),
        )
    }

    // --- 버블: ***왼쪽 방향*** ---
    if (isDraggingThumb && bubbleText != null) {
        Box(
            modifier = modifier
                .offset {
                    val bubbleY = (drawThumbY - 24f).toInt()
                        .coerceIn(0, containerSize.height - 48)
                    IntOffset(0, bubbleY)
                }
        ) {
            Card(
                shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp),
                colors = CardDefaults.cardColors(
                    Color(LocalContext.current.config.primaryColor).copy(
                        alpha = 1.0f
                    )
                ),
                modifier = Modifier.padding(end = 30.dp),
            ) {
                SimpleText(
                    text = bubbleText ?: "",
                    fontColor = Color.White,
                    modifier = Modifier
                        .padding(16.dp, 8.dp),
                )
            }
        }
    }
}