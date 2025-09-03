package me.blog.korn123.easydiary.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config

/***************************************************************************************************
 *
 *   SelfDevelopmentRepo Compose Components
 *
 ***************************************************************************************************/

@Composable
fun TreeToolbar(
    title: String,
    modifier: Modifier,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontColor: Color = Color(LocalContext.current.config.textColor),
    alpha: Float = 1.0f,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily? = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(
        LocalContext.current
    ),
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    callback: (query: String) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(roundedCornerShapeSize.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier = modifier.padding(0.dp, 0.dp, 0.dp, verticalPadding.dp),
//        modifier = (if (enableCardViewPolicy) modifier.padding(horizontalPadding.dp, verticalPadding.dp) else modifier
//            .padding(5.dp, 5.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = roundedCornerShapeSize.dp),
    ) {
        Column(
            modifier = Modifier.padding(5.dp)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically) {
                var text by remember { mutableStateOf("") }
                val density = LocalDensity.current
                val textUnit = with(density) {
                    val temp = fontSize.toDp()
                    temp.toSp()
                }

                val focusRequester = remember { FocusRequester() }

                // 화면이 그려진 직후 포커스 요청
//                LaunchedEffect(Unit) {
//                    // 약간의 delay를 주면 레이아웃이 안정된 후 포커스됨
//                    delay(100)
//                    focusRequester.requestFocus()
//                }
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        callback.invoke(text)
                    },
                    label = { Text(text = title, style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
//                        fontStyle = FontStyle.Italic,
                        color = fontColor.copy(alpha),
                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                    )) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(LocalContext.current.config.backgroundColor),   // 포커스 시 배경
                        unfocusedContainerColor = Color(LocalContext.current.config.backgroundColor) // 포커스 없을 때 배경
                    ),
                    textStyle = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
//                        fontStyle = FontStyle.Italic,
                        color = fontColor.copy(alpha),
                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                    ),
                    singleLine = true,
                    trailingIcon = {
                        if (text.isNotEmpty()) {
                            IconButton(onClick = {
                                text = ""
                                callback.invoke(text)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear text"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                        .focusRequester(focusRequester)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TreeCard(
    sequence: Int = 0,
    title: String,
    subTitle: String,
    level: Int,
    isFile: Boolean,
    currentQuery: String,
    isRootShow: Boolean = true,
    isShow: Boolean = true,
    isFolderOpen: Boolean = true,
    visibleSubTitle: Boolean = true,
    modifier: Modifier,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontFamily: FontFamily? = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(
        LocalContext.current
    ),
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val color = if (isFile) Color.LightGray else Color.White
    if (isRootShow && isShow) {

        Row{
            if (!isFile) {
                Column(
                    Modifier
                        .padding(start = (level.minus(1) * 20).dp)
                ) {
                    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            painter = if (isFolderOpen) painterResource(id = R.drawable.arrow_drop_down_24px) else painterResource(id = R.drawable.arrow_right_24px),
                            contentDescription = null,

                            tint = Color(LocalContext.current.config.primaryColor),

                            )
                    }
                }
            }
            Column(
                modifier = if (isFile) Modifier
                    .padding(start = ((level.minus(1) * 20) + 32).dp)
                    .fillMaxWidth()
                    .weight(1f) // 남은 공간 모두 차지
                else Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                Card(
                    shape = RoundedCornerShape(roundedCornerShapeSize.dp),
                    colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
                    modifier = modifier
                        .padding(1.dp, 1.dp)
                        .combinedClickable(
                            onClick = { onClick.invoke() },
                            onLongClick = { onLongClick.invoke() }
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = roundedCornerShapeSize.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .padding(10.dp, 7.dp),

                        ) {
                        val nodeModifier = Modifier
//                    .background(Color.Yellow.copy(alpha = 0.2f))
                            .padding(0.dp, 0.dp)
                        Row(
                            modifier = nodeModifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var originTitle = if (isFile) "🗒️ $title" else "📂 $title";
                            originTitle =
                                if (LocalContext.current.config.enableDebugOptionVisibleDiarySequence) "[$sequence] $originTitle" else originTitle
                            val annotatedText = buildAnnotatedString {
                                append(originTitle)
                                if (currentQuery.isNotBlank()) {
                                    var startIndex =
                                        originTitle.indexOf(currentQuery, 0, ignoreCase = true)
                                    while (startIndex >= 0) {
                                        addStyle(
                                            style = SpanStyle(
//                                    background = Color.Yellow.copy(alpha = 0.5f),
                                                background = Color(HIGHLIGHT_COLOR),
                                                color = Color.Black,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            start = startIndex,
                                            end = startIndex + currentQuery.length
                                        )
                                        startIndex = originTitle.indexOf(
                                            currentQuery,
                                            startIndex + currentQuery.length,
                                            ignoreCase = true
                                        )
                                    }
                                }
                            }
                            SimpleText(
//                    text = if (isFile) "🗒️ $annotatedText" else "️📂 $annotatedText",
                                text = annotatedText,
                                fontWeight = FontWeight.Normal,
                                fontSize = fontSize,
                                fontFamily = fontFamily,
                                lineSpacingScaleFactor = lineSpacingScaleFactor,
                                maxLines = 1
                            )
                        }
                        if (visibleSubTitle) {
                            val displaySubTitle =
                                if (LocalContext.current.config.enableDebugOptionVisibleDiarySequence) "[$isRootShow][$isShow][$level] $subTitle" else subTitle
                            Row(
                                modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SimpleText(
                                    text = displaySubTitle,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = fontSize.times(0.8f),
                                    fontFamily = fontFamily,
                                    lineSpacingScaleFactor = lineSpacingScaleFactor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionDialog(
    showDialog: Boolean,
    optionEnabled: Boolean,
    fontFamily: FontFamily? = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(
        LocalContext.current
    ),
    onOptionChangeVisibleSubTitle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            containerColor = Color(LocalContext.current.config.backgroundColor),
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    SimpleText(text = "확인")
                }
            },
            title = {
                SimpleText(
                    text = "트리뷰 옵션설정",
                    fontWeight = FontWeight.Bold,
                    fontSize = LocalContext.current.config.settingFontSize.times(1.3f),
                )
            },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SimpleText(
                        modifier = Modifier.weight(1f),
                        "Node 전체경로 표시",
                    )
                    Switch(
                        modifier = Modifier.padding(start = 10.dp),
                        checked = optionEnabled,
                        onCheckedChange = onOptionChangeVisibleSubTitle,
                        thumbContent = if (optionEnabled) {
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
            }
        )
    }
}

@Composable
fun ElevatedButtonWrapper(
    text: String,
    modifier: Modifier = Modifier,
    fontColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    ElevatedButton(
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = Color(LocalContext.current.config.primaryColor),   // 배경색
            contentColor = fontColor,   // 텍스트/아이콘 색
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, // 비활성화 배경색
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant // 비활성화 텍스트색
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        enabled = enabled
    ) {
        SimpleText(text = text, fontColor = fontColor)
    }
}

@Composable
fun BottomToolBar(
    bottomPadding: Dp,
    showOptionDialog: (isShow: Boolean) -> Unit,
    writeDiaryCallback: () -> Unit = {},
    expandTreeCallback: () -> Unit = {},
    collapseTreeCallback: () -> Unit = {},
) {
    Box(modifier = Modifier.padding(bottom = bottomPadding)) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),  // 우측정렬 + 간격
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 0.dp, 10.dp, 0.dp)
                .align(Alignment.BottomEnd) // Box 내부에서 우측 하단 배치
        ) {
            FloatingActionButton(
                onClick = { writeDiaryCallback() },
                containerColor = Color(LocalContext.current.config.primaryColor),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Write Diary"
                )
            }

            ElevatedButtonWrapper(text = "펼치기") { expandTreeCallback() }
            ElevatedButtonWrapper(text = "접기") { collapseTreeCallback() }

            FloatingActionButton(
                onClick = { showOptionDialog(true) },
                containerColor = Color(LocalContext.current.config.primaryColor),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_7),
                    contentDescription = "옵션 설정"
                )
            }
        }
    }
}
