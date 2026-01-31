package me.blog.korn123.easydiary.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.extensions.config

@Composable
fun SimpleText(
    modifier: Modifier = Modifier,
    text: String,
    alpha: Float = 1.0f,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontColor: Color = Color(LocalContext.current.config.textColor),
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    maxLines: Int = Int.MAX_VALUE,
) {
    val density = LocalDensity.current
    val textUnit =
        with(density) {
            val temp = fontSize.toDp()
            temp.toSp()
        }

    Text(
        modifier = modifier,
        text = text,
        style =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = fontWeight,
//                        fontStyle = FontStyle.Italic,
                color = fontColor.copy(alpha),
                fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
            ),
        lineHeight = textUnit.value.times(lineSpacingScaleFactor.sp),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun SimpleText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    alpha: Float = 1.0f,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontColor: Color = Color(LocalContext.current.config.textColor),
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    maxLines: Int = Int.MAX_VALUE,
) {
    val density = LocalDensity.current
    val textUnit =
        with(density) {
            val temp = fontSize.toDp()
            temp.toSp()
        }

    Text(
        modifier = modifier,
        text = text,
        style =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = fontWeight,
//                        fontStyle = FontStyle.Italic,
                color = fontColor.copy(alpha),
                fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
            ),
        lineHeight = textUnit.value.times(lineSpacingScaleFactor.sp),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
