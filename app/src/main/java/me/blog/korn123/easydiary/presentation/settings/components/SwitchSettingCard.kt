package me.blog.korn123.easydiary.presentation.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.presentation.components.SimpleText

/***************************************************************************************************
 *   Switch Setting Card
 *
 ***************************************************************************************************/

@Composable
fun SwitchCard(
    title: String,
    description: String?,
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
    callback: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier =
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
            },
        elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
        onClick = {
            callback.invoke()
        },
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SimpleText(
                    modifier = Modifier.weight(1f),
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                )
                Switch(
                    modifier =
                        Modifier
                            .absolutePadding(left = 5.dp)
                            .height(32.dp),
                    checked = isOn,
                    onCheckedChange = {
                        callback.invoke()
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
            description?.let {
                Row(
                    modifier = Modifier.padding(top = 5.dp),
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
}

@Composable
fun SwitchCardWithImage(
    title: String,
    imageResourceId: Int,
    description: String,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    modifier: Modifier,
    isOn: Boolean,
    callback: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier =
            if (LocalContext.current.config.enableCardViewPolicy) {
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
            },
        elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
        onClick = {
            callback.invoke()
        },
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SimpleText(
                    modifier = Modifier.weight(1f),
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                )
                Switch(
                    modifier =
                        Modifier
                            .absolutePadding(left = 5.dp)
                            .height(32.dp),
                    checked = isOn,
                    onCheckedChange = {
                        callback.invoke()
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
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = imageResourceId),
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                SimpleText(
                    text = description,
                    alpha = 0.7f,
                    fontFamily = fontFamily,
                )
            }
        }
    }
}

@Composable
fun SwitchCardTodo(
    title: String,
    description: String,
    modifier: Modifier,
    isOn: Boolean,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    callback: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier =
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
            },
        elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
        onClick = {
            callback.invoke()
        },
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SimpleText(
                    modifier = Modifier.weight(1f),
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_todo),
                    contentDescription = "Todo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_doing),
                    contentDescription = "Doing",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Switch(
                    modifier =
                        Modifier
                            .absolutePadding(left = 5.dp)
                            .height(32.dp),
                    checked = isOn,
                    onCheckedChange = {
                        callback.invoke()
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
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
            ) {
                SimpleText(
                    text = description,
                    alpha = 0.7f,
                    fontFamily = fontFamily,
                )
            }
        }
    }
}
