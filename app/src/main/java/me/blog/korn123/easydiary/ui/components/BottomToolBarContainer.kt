package me.blog.korn123.easydiary.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config

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

@Composable
fun BottomToolBarButton(
    text: String? = null,
    iconResourceId: Int? = null,
    fontColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {

    ElevatedButton(
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = Color(LocalContext.current.config.primaryColor),   // 배경색
            contentColor = fontColor,   // 텍스트/아이콘 색
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, // 비활성화 배경색
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant // 비활성화 텍스트색
        ),
        contentPadding = PaddingValues(8.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        if (iconResourceId != null) {
            Icon(
                painter = painterResource(id = iconResourceId),
                contentDescription = text
            )
        }
        if (iconResourceId != null && text != null) Spacer(modifier = Modifier.width(4.dp))
        if (text != null) {
            SimpleText(text = text, fontColor = fontColor)
        }
    }
}