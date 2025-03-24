package me.blog.korn123.easydiary.ui.components

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.xw.repo.BubbleSeekBar
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.PartialBubbleSeekBarBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getFormattedTime
import me.blog.korn123.easydiary.viewmodels.BaseDevViewModel

const val verticalPadding = 4F
const val horizontalPadding = 3F

/***************************************************************************************************
 *   Base Composable
 *
 ***************************************************************************************************/

@Composable
fun CardContainer(
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = if (enableCardViewPolicy) Modifier
//            .padding(6.dp, 0.dp)
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
fun SimpleText(
    modifier: Modifier = Modifier,
    text: String,
    alpha: Float = 1.0f,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontColor: Color = Color(LocalContext.current.config.textColor),
    fontFamily: FontFamily? = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(
        LocalContext.current
    ),
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
) {
    val density = LocalDensity.current
    val textUnit = with(density) {
        val temp = fontSize.toDp()
        temp.toSp()
    }

    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontFamily = fontFamily,
            fontWeight = fontWeight,
//                        fontStyle = FontStyle.Italic,
            color = fontColor.copy(alpha),
            fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
        ),
        lineHeight = textUnit.value.times(lineSpacingScaleFactor.sp)
    )
}

@Composable
fun CategoryTitleCard(
    title: String,
    marginTop: Int = 6,
    fontFamily: FontFamily? = null,
) {
    val modifier = Modifier.fillMaxWidth()
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.primaryColor)),
        modifier = (if (LocalContext.current.config.enableCardViewPolicy) modifier.padding(
            start = 3.dp, top = marginTop.plus(3).dp,
            end = 3.dp,
            bottom = 3.dp
        ) else modifier.padding(
            start = 1.dp,
            top = marginTop.plus(1).dp,
            end = 1.dp,
            bottom = 1.dp
        )),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp, 5.dp)
        ) {
            SimpleText(
                text = title,
                fontWeight = FontWeight.Bold,
                fontColor = Color.White,
                fontFamily = fontFamily
            )
        }
    }
}


/***************************************************************************************************
 *   Simple Setting Card
 *
 ***************************************************************************************************/
@Composable
fun SimpleCard(
    title: String,
    description: String?,
    subDescription: String? = null,
    modifier: Modifier,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontFamily: FontFamily? = null,
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    callback: () -> Unit = {}
) {

    SimpleCardWithImage(
        title = title,
        description = description,
        subDescription = subDescription,
        imageResourceId = null,
        modifier = modifier,
        enableCardViewPolicy = enableCardViewPolicy,
        fontSize = fontSize,
        fontFamily = fontFamily,
        lineSpacingScaleFactor = lineSpacingScaleFactor,
        callback = callback
    )
}

@Composable
fun SimpleCardWithImage(
    title: String,
    description: String?,
    subDescription: String? = null,
    imageResourceId: Int?,
    modifier: Modifier,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontFamily: FontFamily? = null,
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    callback: () -> Unit = {}
) {

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
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.Top) {
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
                verticalAlignment = Alignment.CenterVertically) {
                imageResourceId?.let {
                    Image(
                        painter = painterResource(id = it),
                        contentDescription = "Google Calendar",
                        contentScale = ContentScale.Fit,
                        modifier =  Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                description?.run {
                    SimpleText(
                        text = description,
                        alpha = 0.7f,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        lineSpacingScaleFactor = lineSpacingScaleFactor,
                    )
                }
            }

            subDescription?.run {
                Row(
                    modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SimpleText(
                        text = subDescription,
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
    fontFamily: FontFamily? = null,
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    callback: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = if (enableCardViewPolicy) modifier.padding(
            horizontalPadding.dp,
            verticalPadding.dp
        ) else modifier
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
                SimpleText(
                    modifier = Modifier.weight(1f),
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingScaleFactor = lineSpacingScaleFactor,
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
    fontFamily: FontFamily? = null,
    modifier: Modifier,
    isOn: Boolean,
    callback: () -> Unit
) {

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
                SimpleText(
                    modifier = Modifier.weight(1f),
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
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
            Row(
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = imageResourceId),
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier =  Modifier.size(32.dp)
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
    fontFamily: FontFamily? = null,
    callback: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = if (enableCardViewPolicy) modifier.padding(horizontalPadding.dp, verticalPadding.dp) else modifier
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
                    modifier =  Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_doing),
                    contentDescription = "Doing",
                    contentScale = ContentScale.Fit,
                    modifier =  Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
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
            Row(
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp)
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
    fontFamily: FontFamily? = null,
    callback: (key: Int) -> Unit
) {

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = if (LocalContext.current.config.enableCardViewPolicy) modifier.padding(
            horizontalPadding.dp,
            verticalPadding.dp
        ) else modifier
            .padding(1.dp, 1.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
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
                    modifier = Modifier.padding(top = 5.dp)
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
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(LocalContext.current.config.primaryColor),
                            unselectedColor = Color(LocalContext.current.config.textColor),
                            disabledSelectedColor = Color.LightGray,   // 비활성화된 선택 색상
                            disabledUnselectedColor = Color.DarkGray   // 비활성화된 미선택 색상
                        )
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


/***************************************************************************************************
 *   Custom Setting Card
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
    fontFamily: FontFamily? = null,
) {

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = (if (enableCardViewPolicy) modifier.padding(
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
            SimpleText(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                fontFamily = fontFamily,
                lineSpacingScaleFactor = lineSpacingScaleFactor,
            )
            description?.let {
                SimpleText(
                    modifier = Modifier
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
    fontFamily: FontFamily? = null,
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    callback: (progressFloat: Float) -> Unit = {}
) {

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = (if (enableCardViewPolicy) modifier.padding(horizontalPadding.dp, verticalPadding.dp) else modifier
            .padding(1.dp, 1.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
//                modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                modifier = Modifier,
                verticalAlignment = Alignment.Top) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                AndroidView(
                    modifier = modifier,
                    factory = { ctx ->
                        val binding = PartialBubbleSeekBarBinding.inflate(LayoutInflater.from(ctx)).apply {
                            fontLineSpacing.configBuilder
                                .min(0.2F)
                                .max(1.8F)
                                .progress(lineSpacingScaleFactor)
                                .floatType()
                                .secondTrackColor(ctx.config.textColor)
                                .trackColor(ctx.config.textColor)
                                .sectionCount(16)
                                .sectionTextInterval(2)
                                .showSectionText()
                                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                                .autoAdjustSectionMark()
                                .build()
                            val bubbleSeekBarListener = object : BubbleSeekBar.OnProgressChangedListener {
                                override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
                                    ctx.config.lineSpacingScaleFactor = progressFloat
//                                    setFontsStyle()
                                    callback(progressFloat)
                                }
                                override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {}
                                override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {}
                            }
                            fontLineSpacing.onProgressChangedListener = bubbleSeekBarListener
                        }

                        binding.root
                    },
                    update = {
                            view ->
                        val binding = PartialBubbleSeekBarBinding.bind(view)
                        binding.fontLineSpacing.run {
                            setProgress(lineSpacingScaleFactor)
                            invalidate()
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically
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
    fontFamily: FontFamily? = null,
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    callbackMinus: () -> Unit = {},
    callbackPlus: () -> Unit = {}
) {

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = (if (enableCardViewPolicy) modifier.padding(
            horizontalPadding.dp,
            verticalPadding.dp
        ) else modifier
            .padding(1.dp, 1.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
//                modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                modifier = Modifier,
                verticalAlignment = Alignment.Top
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
                verticalAlignment = Alignment.CenterVertically
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
                    modifier = Modifier
                        .size(32.dp)
                        .padding(3.dp)
                        .clickable {
//                            LocalContext.current.config.settingFontSize.minus(6)
                            callbackMinus.invoke()
                        },
                    colorFilter = ColorFilter.tint(Color(LocalContext.current.config.textColor)),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_plus_6),
                    contentDescription = "Google Calendar",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
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

@Composable
fun SymbolCard(
    modifier: Modifier,
    viewModel: BaseDevViewModel,
    fontFamily: FontFamily? = null,
    callback: () -> Unit
) {
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
        Row(
            modifier = Modifier.padding(15.dp)
        ) {
            SimpleText(
                text = symbol.toString(),
                fontFamily = fontFamily,
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
    fontFamily: FontFamily? = null,
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    checkedChangeCallback: () -> Unit,
    callback: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = if (enableCardViewPolicy) modifier.padding(
            horizontalPadding.dp,
            verticalPadding.dp
        ) else modifier
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .shadow(3.dp, shape = RoundedCornerShape(3.dp))
                        .padding(2.dp)
                ) {
                    Text(
                        text = alarmTag,
                        style = TextStyle(
                            fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(
                                LocalContext.current
                            ),
//                        fontWeight = fontWeight,
//                        fontStyle = FontStyle.Italic,
//                        color = Color(LocalContext.current.config.textColor).copy(alpha),
                            color = Color(LocalContext.current.config.primaryColor),
                            fontSize = TextUnit(11F, TextUnitType.Sp),
                        ),
                        modifier = Modifier
                            .background(
                                Color.White
                                , shape = RoundedCornerShape(3.dp)
                            )
//                            .shadow(
//                                8.dp
//                                , shape = RoundedCornerShape(3.dp)
//                            )
                            .border(
                                1.dp
                                , Color(LocalContext.current.config.primaryColor).copy(1.0f)
                                , shape = RoundedCornerShape(3.dp)
                            )
                            .padding(5.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = modifier,
                    text = LocalContext.current.getFormattedTime(alarmTime.times(60), false, true).toAnnotatedString(),
                    style = TextStyle(
                        fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(
                            LocalContext.current
                        ),
//                        fontWeight = fontWeight,
//                        fontStyle = FontStyle.Italic,
//                        color = Color(LocalContext.current.config.textColor).copy(alpha),
                        color = Color(LocalContext.current.config.textColor),
                        fontSize = TextUnit(44F, TextUnitType.Sp),
                    ),
//                    lineHeight = textUnit.value.times(lineSpacingScaleFactor.sp)
                )
                Switch(
                    modifier = Modifier
                        .absolutePadding(left = 5.dp)
                        .height(32.dp)
//                        .background(Color.Yellow)
                    ,
                    checked = isOn,
                    onCheckedChange = {
                        checkedChangeCallback.invoke()
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
                modifier = Modifier.padding(top = 5.dp)
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
                modifier = Modifier.padding(top = 5.dp)
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