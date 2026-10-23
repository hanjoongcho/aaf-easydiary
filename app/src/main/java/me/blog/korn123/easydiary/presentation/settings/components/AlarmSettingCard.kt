package me.blog.korn123.easydiary.presentation.settings.components

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getFormattedTime
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.presentation.components.SimpleText

/***************************************************************************************************
 *   Alarm Setting Card
 *
 ***************************************************************************************************/

@Composable
fun AlarmCard(
    alarmTime: Int = 0,
    alarmDays: String,
    alarmDescription: String,
    alarmTag: String = "test",
    modifier: Modifier,
    isOn: Boolean,
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
    checkedChangeCallback: () -> Unit,
    callback: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier =
            (
                if (enableCardViewPolicy) {
                    modifier.padding(
                        HORIZONTAL_PADDING.dp,
                        VERTICAL_PADDING.dp,
                    )
                } else {
                    modifier
                        .padding(1.dp, 1.dp)
                        .clickable {
                            callback.invoke()
                        }
                }
            ).shadow(3.dp, RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp)),
        onClick = {
            callback.invoke()
        },
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier =
                        Modifier
                            .shadow(3.dp, shape = RoundedCornerShape(3.dp))
                            .padding(2.dp),
                ) {
                    Text(
                        text = alarmTag,
                        style =
                            TextStyle(
                                fontFamily =
                                    if (LocalInspectionMode.current) {
                                        null
                                    } else {
                                        FontUtils.getComposeFontFamily(
                                            LocalContext.current,
                                        )
                                    },
                                color = Color(LocalContext.current.config.primaryColor),
                                fontSize = TextUnit(11F, TextUnitType.Sp),
                            ),
                        modifier =
                            Modifier
                                .background(
                                    Color.White,
                                    shape = RoundedCornerShape(3.dp),
                                )
                                .border(
                                    1.dp,
                                    Color(LocalContext.current.config.primaryColor).copy(1.0f),
                                    shape = RoundedCornerShape(3.dp),
                                ).padding(5.dp),
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = modifier,
                    text =
                        LocalContext.current
                            .getFormattedTime(alarmTime.times(60), false, true)
                            .toAnnotatedString(),
                    style =
                        TextStyle(
                            fontFamily =
                                if (LocalInspectionMode.current) {
                                    null
                                } else {
                                    FontUtils.getComposeFontFamily(
                                        LocalContext.current,
                                    )
                                },
                            color = Color(LocalContext.current.config.textColor),
                            fontSize = TextUnit(44F, TextUnitType.Sp),
                        ),
                )
                Switch(
                    modifier =
                        Modifier
                            .absolutePadding(left = 5.dp)
                            .height(32.dp),
                    checked = isOn,
                    onCheckedChange = {
                        checkedChangeCallback.invoke()
                    },
                    thumbContent =
                        if (isOn) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        },
                )
            }
            Row(
                modifier = Modifier.padding(top = 5.dp),
            ) {
                SimpleText(
                    text = alarmDays,
                    alpha = 0.7f,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                )
            }
            Row(
                modifier = Modifier.padding(top = 5.dp),
            ) {
                SimpleText(
                    text = alarmDescription,
                    alpha = 0.7f,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                )
            }
        }
    }
}

fun SpannableString.toAnnotatedString(): AnnotatedString {
    val builder = AnnotatedString.Builder(this.toString())

    getSpans(0, length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)

        when (span) {
            is StyleSpan -> {
                if (span.style == Typeface.BOLD) {
                    builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                }
            }

            is ForegroundColorSpan -> {
                builder.addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
            }
        }
    }

    return builder.toAnnotatedString()
}
