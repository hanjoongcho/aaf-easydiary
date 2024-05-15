package me.blog.korn123.easydiary.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.extensions.config

@Composable
fun SimpleCard(
    context: Context,
    textUnit: TextUnit,
    isPreview: Boolean = false,
    title: String,
    description: String,
    modifier: Modifier,
    callback: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(context.config.backgroundColor)),
        modifier = (if (context.config.enableCardViewPolicy) modifier.padding(
            3.dp,
            3.dp
        ) else modifier.padding(1.dp, 1.dp)).clickable {
            callback.invoke()
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(context),
                    fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                    color = Color(context.config.textColor),
                    fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                ),
            )
            Text(
                modifier = Modifier
                    .padding(0.dp, 5.dp, 0.dp, 0.dp),
                text = description,
                style = TextStyle(
                    fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(context),
                    color = Color(context.config.textColor),
                    fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                ),
            )
        }
    }
}

@Composable
fun SwitchCard(
    context: Context,
    textUnit: TextUnit,
    isPreview: Boolean = false,
    title: String,
    description: String,
    modifier: Modifier,
    isOn: Boolean,
    callback: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(context.config.backgroundColor)),
        modifier = if (context.config.enableCardViewPolicy) modifier.padding(3.dp, 3.dp) else modifier.padding(1.dp, 1.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            callback.invoke()
        }
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = TextStyle(
                        fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(context),
                        fontWeight = FontWeight.Bold,
                        color = Color(context.config.textColor),
                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                    ),
                )
                Switch(
                    modifier = Modifier.absolutePadding(0.dp),
                    checked = isOn,
                    onCheckedChange = {
                        callback.invoke()
                    },
                    thumbContent = if (isOn) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    }
                )
            }
            Row(
            ) {
                Text(
                    text = description,
                    style = TextStyle(
                        fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(context),
                        color = Color(context.config.textColor),
                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                    ),
                )
            }
        }
    }
}