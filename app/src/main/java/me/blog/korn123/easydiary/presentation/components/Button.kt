package me.blog.korn123.easydiary.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tbuonomo.viewpagerdotsindicator.dpToPx
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.SYMBOL_USER_CUSTOM_START
import me.blog.korn123.easydiary.utils.EasyDiaryUtils

@Composable
fun CustomElevatedButton(
    text: String? = null,
    iconResourceId: Int? = null,
    iconSize: Dp = 24.dp,
    fontColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    ElevatedButton(
        onClick = onClick,
        colors =
            ButtonDefaults.elevatedButtonColors(
                containerColor = Color(LocalContext.current.config.primaryColor), // 배경색
                contentColor = fontColor, // 텍스트/아이콘 색
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, // 비활성화 배경색
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant, // 비활성화 텍스트색
            ),
        contentPadding = PaddingValues(12.dp, 8.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
    ) {
        if (iconResourceId != null) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(id = iconResourceId),
                contentDescription = text,
            )
        }
        if (iconResourceId != null && text != null) Spacer(modifier = Modifier.width(6.dp))
        if (text != null) {
            SimpleText(text = text, fontColor = fontColor)
        }
    }
}

@Composable
fun CustomElevatedSquareButton(
    text: String,
    iconResourceId: Int,
    iconSize: Dp = 16.dp,
    iconPadding: Dp = 0.dp,
    fontColor: Color = Color.White,
    enabled: Boolean = true,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    ElevatedButton(
        modifier = Modifier.defaultMinSize(76.dp),
        onClick = onClick,
        colors =
            ButtonDefaults.elevatedButtonColors(
                containerColor = Color(LocalContext.current.config.primaryColor), // 배경색
                contentColor = fontColor, // 텍스트/아이콘 색
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, // 비활성화 배경색
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant, // 비활성화 텍스트색
            ),
        contentPadding = PaddingValues(12.dp, 12.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                modifier = Modifier.size(iconSize).padding(iconPadding),
                painter = painterResource(id = iconResourceId),
                contentDescription = text,
                tint = tint,
            )
            Spacer(modifier = Modifier.height(5.dp))
            SimpleText(text = text, fontColor = fontColor, fontSize = 10.dp.dpToPx())
        }
    }
}

@Composable
fun CustomCircleIconButton(
    iconResourceId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    buttonSize: Dp = 32.dp,
    iconSize: Dp = 20.dp,
    iconPadding: Dp = 0.dp,
    containerColor: Color = Color(LocalContext.current.config.primaryColor),
    tint: Color = Color.White,
    rippleColor: Color = Color.White,
    elevation: Dp = 0.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(buttonSize)
                .shadow(elevation = elevation, shape = CircleShape)
                .clip(CircleShape)
                .background(containerColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = rippleColor, bounded = true),
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(iconSize).padding(iconPadding),
            painter = painterResource(id = iconResourceId),
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}

@Composable
fun SymbolIconButton(
    symbolSequence: Int,
    modifier: Modifier = Modifier,
    symbolSize: Dp = 32.dp,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .size(symbolSize)
                .clip(CircleShape),
    ) {
        if (symbolSequence < SYMBOL_USER_CUSTOM_START) {
            val symbolResourceId = FlavorUtils.sequenceToSymbolResourceId(symbolSequence)
            val symbolPainter = if (symbolResourceId > 0) painterResource(id = symbolResourceId) else painterResource(id = R.drawable.ic_select_symbol)
            Image(
                painter = symbolPainter,
                contentDescription = "Symbol",
                modifier = Modifier.size(symbolSize),
            )
        } else {
            val targetIndex = symbolSequence - SYMBOL_USER_CUSTOM_START
            val filePath =
                if (context.config.customSymbolPaths.size > targetIndex) {
                    context.config.customSymbolPaths[targetIndex].getFilePath()
                } else {
                    ""
                }
            val fullPath = EasyDiaryUtils.getApplicationDataDirectory(context) + filePath
            val bitmap =
                remember(symbolSequence, fullPath) {
                    BitmapFactory.decodeFile(fullPath)
                }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Custom Symbol",
                    modifier = Modifier.size(symbolSize),
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_select_symbol),
                    contentDescription = "Symbol Fallback",
                    modifier = Modifier.size(symbolSize),
                )
            }
        }
    }
}
