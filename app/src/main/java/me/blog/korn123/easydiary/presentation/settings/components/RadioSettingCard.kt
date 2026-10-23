package me.blog.korn123.easydiary.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.utils.FontUtils

/***************************************************************************************************
 *   Radio Setting Card
 *
 ***************************************************************************************************/

@Composable
fun RadioGroupCard(
    title: String,
    description: String?,
    modifier: Modifier,
    options: List<Map<String, Any>>,
    selectedKey: Int,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    callback: (key: Int) -> Unit,
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
            },
        elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
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
            }
            description?.let {
                Row(
                    modifier = Modifier.padding(top = 5.dp),
                ) {
                    SimpleText(
                        text = description,
                        alpha = 0.7f,
                        fontFamily = fontFamily,
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                options.forEach { option ->
                    RadioButton(
                        selected = (selectedKey == option["key"]),
                        onClick = {
                            callback.invoke(option["key"] as Int)
                        },
                        modifier = Modifier.size(20.dp),
                        colors =
                            RadioButtonDefaults.colors(
                                selectedColor = Color(LocalContext.current.config.primaryColor),
                                unselectedColor = Color(LocalContext.current.config.textColor),
                                disabledSelectedColor = Color.LightGray,
                                disabledUnselectedColor = Color.DarkGray,
                            ),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    SimpleText(
                        text = option["title"] as String,
                        alpha = 0.7f,
                        fontFamily = fontFamily,
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                }
            }
        }
    }
}
