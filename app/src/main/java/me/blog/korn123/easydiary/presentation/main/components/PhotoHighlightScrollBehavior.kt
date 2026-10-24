package me.blog.korn123.easydiary.presentation.main.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class PhotoHighlightScrollBehavior(
    val maxHeight: Dp,
    val heightOffset: MutableFloatState,
    val nestedScrollConnection: NestedScrollConnection,
    val density: Density,
) {
    val maxHeightPx: Float
        get() = with(density) { maxHeight.toPx() }

    val currentHeight: Dp
        get() {
            val currentPx = (maxHeightPx + heightOffset.floatValue).coerceIn(0f, maxHeightPx)
            return with(density) { currentPx.toDp() }
        }

    val translationY: Float
        get() = heightOffset.floatValue

    val alpha: Float
        get() {
            if (maxHeightPx <= 0f) return 1f
            return (maxHeightPx + heightOffset.floatValue) / maxHeightPx
        }
}

@Composable
fun rememberPhotoHighlightScrollBehavior(
    maxHeight: Dp = 160.dp,
): PhotoHighlightScrollBehavior {
    val density = LocalDensity.current
    val maxHeightPx = with(density) { maxHeight.toPx() }
    val heightOffset = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection =
        remember(maxHeightPx) {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    val delta = available.y
                    val currentOffset = heightOffset.floatValue

                    // 상단 이동 (델타가 음수: 스크롤을 아래로 내려서 컨텐츠를 위로 올릴 때) -> 헤더 축소
                    if (delta < 0f && currentOffset > -maxHeightPx) {
                        val newOffset = (currentOffset + delta).coerceIn(-maxHeightPx, 0f)
                        heightOffset.floatValue = newOffset
                        val consumed = newOffset - currentOffset
                        return Offset(0f, consumed)
                    }

                    // 하단 이동 (델타가 양수: 스크롤을 위로 올려서 컨텐츠를 아래로 내릴 때) -> 헤더 즉시 확장
                    if (delta > 0f && currentOffset < 0f) {
                        val newOffset = (currentOffset + delta).coerceIn(-maxHeightPx, 0f)
                        heightOffset.floatValue = newOffset
                        val consumed = newOffset - currentOffset
                        return Offset(0f, consumed)
                    }

                    return Offset.Zero
                }
            }
        }

    return remember(maxHeightPx, density) {
        PhotoHighlightScrollBehavior(
            maxHeight = maxHeight,
            heightOffset = heightOffset,
            nestedScrollConnection = nestedScrollConnection,
            density = density,
        )
    }
}
