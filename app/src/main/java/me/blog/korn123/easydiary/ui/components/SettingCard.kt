package me.blog.korn123.easydiary.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.viewmodels.BaseDevViewModel

const val verticalPadding = 4F
const val horizontalPadding = 3F

@Composable
fun CardContainer(
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = if (enableCardViewPolicy) Modifier
            .padding(6.dp, 0.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState)
        else Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)

    ) {
        content()
    }
}

@Composable
fun CategoryTitleCard(
    title: String,
) {
    val pixelValue = LocalContext.current.config.settingFontSize
    val density = LocalDensity.current
    val textUnit = with (density) {
        val temp = pixelValue.toDp()
        temp.toSp()
    }
    val modifier = Modifier.fillMaxWidth()
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.primaryColor)),
        modifier = (if (LocalContext.current.config.enableCardViewPolicy) modifier.padding(
            3.dp,
            3.dp
        ) else modifier.padding(1.dp, 1.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp, 5.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                    fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                    color = Color.White,
                    fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                ),
            )
        }
    }
}

@Composable
fun SimpleCard(
    title: String,
    description: String?,
    modifier: Modifier,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    callback: () -> Unit = {}
) {
    val pixelValue = LocalContext.current.config.settingFontSize
    val density = LocalDensity.current
    val textUnit = with (density) {
        val temp = pixelValue.toDp()
        temp.toSp()
    }

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = (if (enableCardViewPolicy) modifier.padding(horizontalPadding.dp, verticalPadding.dp) else modifier
            .padding(1.dp, 1.dp))
            .clickable {
                callback.invoke()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                        fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                        color = Color(LocalContext.current.config.textColor),
                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                    ),
                )
            }
            description?.let {
                Text(
                    modifier = Modifier
                        .padding(0.dp, 5.dp, 0.dp, 0.dp),
                    text = description,
                    style = TextStyle(
                        fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                        color = Color(LocalContext.current.config.textColor),
                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                    ),
                )
            }
        }
    }
}

@Composable
fun SwitchCard(
    title: String,
    description: String?,
    modifier: Modifier,
    isOn: Boolean,
    callback: () -> Unit
) {
    val pixelValue = LocalContext.current.config.settingFontSize
    val density = LocalDensity.current
    val textUnit = with (density) {
        val temp = pixelValue.toDp()
        temp.toSp()
    }
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = if (LocalContext.current.config.enableCardViewPolicy) modifier.padding(horizontalPadding.dp, verticalPadding.dp) else modifier
            .padding(1.dp, 1.dp)
            .clickable {
                callback.invoke()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
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
                        fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                        fontWeight = FontWeight.Bold,
                        color = Color(LocalContext.current.config.textColor),
                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                    ),
                )
                Switch(
                    modifier = Modifier
                        .absolutePadding(left = 5.dp)
                        .height(32.dp)
//                        .background(Color.Yellow)
                    ,

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
            description?.let {
                Row(
                    modifier = Modifier.padding(top = 5.dp)
                ) {
                    Text(
                        text = description,
                        style = TextStyle(
                            fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                            color = Color(LocalContext.current.config.textColor),
                            fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollableCard(
    title: String,
    description: String?,
    modifier: Modifier,
    scrollState: ScrollState
) {
    val pixelValue = LocalContext.current.config.settingFontSize
    val density = LocalDensity.current
    val textUnit = with (density) {
        val temp = pixelValue.toDp()
        temp.toSp()
    }

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = (if (LocalContext.current.config.enableCardViewPolicy) modifier.padding(
            3.dp,
            3.dp
        ) else modifier.padding(1.dp, 1.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(15.dp)
                .height(200.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                    fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                    color = Color(LocalContext.current.config.textColor),
                    fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                ),
            )
            description?.let {
                Text(
                    modifier = Modifier
                        .padding(0.dp, 5.dp, 0.dp, 0.dp),
                    text = description,
                    style = TextStyle(
                        fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                        color = Color(LocalContext.current.config.textColor),
                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                    ),
                )
            }
        }
    }
}

@Composable
fun SymbolCard(
    modifier: Modifier,
    viewModel: BaseDevViewModel,
    callback: () -> Unit
) {
    val pixelValue = LocalContext.current.config.settingFontSize
    val density = LocalDensity.current
    val textUnit = with (density) {
        val temp = pixelValue.toDp()
        temp.toSp()
    }
    val symbol by viewModel.symbol.observeAsState(1)
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = (if (LocalContext.current.config.enableCardViewPolicy) modifier.padding(
            3.dp,
            3.dp
        ) else modifier.padding(1.dp, 1.dp)).clickable {
            callback.invoke()
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                text = symbol.toString(),
                style = TextStyle(
                    fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(LocalContext.current),
                    fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                    color = Color(LocalContext.current.config.textColor),
                    fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                ),
            )
            Image(
                painter = painterResource(id = FlavorUtils.sequenceToSymbolResourceId(symbol)),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

