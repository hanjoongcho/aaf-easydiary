package me.blog.korn123.easydiary.presentation.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
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

            Row(
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val ctx = LocalContext.current
                Slider(
                    value = lineSpacingScaleFactor,
                    onValueChange = { progressFloat ->
                        val rounded = kotlin.math.round(progressFloat * 10) / 10.0f
                        ctx.config.lineSpacingScaleFactor = rounded
                        callback(rounded)
                    },
                    valueRange = 0.2f..1.8f,
                    steps = 15,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        SliderDefaults.colors(
                            thumbColor = Color(ctx.config.primaryColor),
                            activeTrackColor = Color(ctx.config.primaryColor),
                            inactiveTrackColor = Color(ctx.config.textColor).copy(alpha = 0.3f),
                        ),
                )
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
