package me.blog.korn123.easydiary.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomToolBarContainer(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
//        modifier = modifier.padding(bottom = bottomPadding.plus(5.dp))
        modifier = modifier
            .navigationBarsPadding() // 내부적으로 Modifier.windowInsetsPadding(WindowInsets.navigationBars) 호출
            .imePadding() // navigationBarsPadding() 보다 우선 순위가 높음
            .padding(bottom = 5.dp) // 최소 5dp 패딩 유지
    ) {
        val scrollState = rememberScrollState()
        Row (
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),  // 우측정렬 + 간격
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd) // Box 내부에서 우측 하단 배치
                .horizontalScroll(scrollState) // 가로 스크롤 적용
        ) {

            Spacer(modifier = Modifier.width(5.dp))

            content()

            Spacer(modifier = Modifier.width(5.dp))
        }
    }
}

