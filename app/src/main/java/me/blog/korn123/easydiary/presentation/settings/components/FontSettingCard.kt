package me.blog.korn123.easydiary.presentation.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.utils.FontUtils

/***************************************************************************************************
 *   Font / Display Setting Card
 *
 ***************************************************************************************************/

@Composable
fun ScrollableCard(
    title: String,
    description: String?,
    modifier: Modifier,
    scrollState: ScrollState,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
) {
    Card(
        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = (
            if (enableCardViewPolicy) {
                modifier.padding(
                    3.dp,
                    3.dp,
                )
            } else {
                modifier.padding(1.dp, 1.dp)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(15.dp)
                    .height(200.dp)
                    .verticalScroll(scrollState),
        ) {
            SimpleText(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                fontFamily = fontFamily,
                lineSpacingScaleFactor = lineSpacingScaleFactor,
            )
            description?.let {
                SimpleText(
                    modifier =
                        Modifier
                            .padding(0.dp, 5.dp, 0.dp, 0.dp),
                    text = description,
                    alpha = 0.7f,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineSpacing(
    title: String,
    description: String,
    modifier: Modifier,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    callback: (progressFloat: Float) -> Unit = {},
) {
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
        Column(
            modifier = Modifier.padding(15.dp),
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.Top,
            ) {
                SimpleText(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                )
            }

            val ctx = LocalContext.current
            val interactionSource = remember { MutableInteractionSource() }

            // 1. IconButton의 기본 48.dp 최소 크기 제약을 32.dp로 축소
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 32.dp) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            val newValue = (lineSpacingScaleFactor - 0.1f).coerceIn(0.2f, 1.8f)
                            val rounded = kotlin.math.round(newValue * 10) / 10.0f
                            ctx.config.lineSpacingScaleFactor = rounded
                            callback(rounded)
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease line spacing",
                            tint = Color(ctx.config.textColor),
                        )
                    }

                    Slider(
                        value = lineSpacingScaleFactor,
                        onValueChange = { progressFloat ->
                            val rounded = kotlin.math.round(progressFloat * 10) / 10.0f
                            ctx.config.lineSpacingScaleFactor = rounded
                            callback(rounded)
                        },
                        valueRange = 0.2f..1.8f,
                        steps = 15,
                        // 3. Thumb과 동일한 interactionSource를 Slider에도 연결해야 드래그 시 핸들 애니메이션 동작
                        interactionSource = interactionSource,
                        // 2. Slider 자체에 내장 여백이 있으므로 horizontal padding 제거
                        modifier = Modifier.weight(1f),
                        colors =
                            SliderDefaults.colors(
                                thumbColor = Color(ctx.config.primaryColor),
                                activeTrackColor = Color(ctx.config.primaryColor),
                                inactiveTrackColor = Color(ctx.config.textColor).copy(alpha = 0.3f),
                            ),
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = interactionSource,
                                thumbSize = DpSize(width = 4.dp, height = 24.dp),
                            )
                        },
                    )

                    IconButton(
                        onClick = {
                            val newValue = (lineSpacingScaleFactor + 0.1f).coerceIn(0.2f, 1.8f)
                            val rounded = kotlin.math.round(newValue * 10) / 10.0f
                            ctx.config.lineSpacingScaleFactor = rounded
                            callback(rounded)
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase line spacing",
                            tint = Color(ctx.config.textColor),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SimpleText(
                    text = description,
                    alpha = 0.7f,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                )
            }
        }
    }
}

@Composable
fun FontSize(
    title: String,
    description: String,
    modifier: Modifier,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    callbackMinus: () -> Unit = {},
    callbackPlus: () -> Unit = {},
) {
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
        Column(
            modifier = Modifier.padding(15.dp),
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.Top,
            ) {
                SimpleText(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                )
            }

            Row(
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SimpleText(
                    modifier = Modifier.weight(1f),
                    text = description,
                    alpha = 0.7f,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_minus_6),
                    contentDescription = "Google Calendar",
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .size(32.dp)
                            .padding(3.dp)
                            .clickable {
                                callbackMinus.invoke()
                            },
                    colorFilter = ColorFilter.tint(Color(LocalContext.current.config.textColor)),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_plus_6),
                    contentDescription = "Google Calendar",
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .size(32.dp)
                            .padding(3.dp)
                            .clickable {
                                callbackPlus.invoke()
                            },
                    colorFilter = ColorFilter.tint(Color(LocalContext.current.config.textColor)),
                )
            }
        }
    }
}
