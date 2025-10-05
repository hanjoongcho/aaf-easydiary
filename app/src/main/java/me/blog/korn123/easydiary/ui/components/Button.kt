package me.blog.korn123.easydiary.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.extensions.config

@Composable
fun CustomElevatedButton(
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