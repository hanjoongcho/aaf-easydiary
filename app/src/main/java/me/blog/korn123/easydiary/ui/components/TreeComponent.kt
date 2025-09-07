package me.blog.korn123.easydiary.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.simplemobiletools.commons.extensions.toast
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.syncMarkDown
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.TransitionHelper.Companion.finishActivityWithTransition

/***************************************************************************************************
 *
 *   SelfDevelopmentRepo Compose Components
 *
 ***************************************************************************************************/

@Composable
fun TreeContent(
    innerPadding: PaddingValues,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    total: Int,
    treeData: List<Pair<FileNode, Int>>,
    currentQuery: String,
    fetchDiary: () -> Unit,
    updateQuery: (String) -> Unit,
    toggleWholeTree: (Boolean) -> Unit,
    folderOnClick: (FileNode) -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val bottomPadding = if (context.isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
    val statusBarPadding = if (context.isVanillaIceCreamPlus()) WindowInsets.statusBars.asPaddingValues().calculateTopPadding() else 0.dp
    var showOptionDialog by remember { mutableStateOf(false) }
    var visibleSubTitle by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val settingCardModifier = Modifier.fillMaxWidth()
    var bottomToolbarHeight by remember { mutableStateOf(0.dp) }
    var topToolbarHeight by remember { mutableStateOf(0.dp) }

    OptionDialog (
        showDialog = showOptionDialog,
        optionEnabled = visibleSubTitle,
        onOptionChangeVisibleSubTitle = { visibleSubTitle = it },
        onDismiss = { showOptionDialog = false }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(context.config.screenBackgroundColor)),
            ) {
                item {
                    Spacer(modifier = Modifier.height(topToolbarHeight.plus(statusBarPadding)))
                }
                items(items = treeData.filter { data -> data.first.isRootShow && data.first.isShow }, key = { "${it.first.sequence}-${it.first.fullPath}"}) { (node, level) ->
                    TreeCard(
                        title = node.name,
                        subTitle = node.fullPath,
                        level = level,
                        isFile = node.isFile,
                        currentQuery = currentQuery,
                        isRootShow = node.isRootShow,
                        isShow = node.isShow,
                        isFolderOpen = node.isFolderOpen,
                        visibleSubTitle = visibleSubTitle,
                        modifier = Modifier.padding(
                            0.dp,
                            0.dp,
                            0.dp,
                            0.dp
                        ),
                        onClick = {
                            if (node.isFile) {
                                // 파일인 경우, 해당 다이어리 읽기 화면으로 이동
                                val detailIntent = Intent(
                                    context,
                                    DiaryReadingActivity::class.java
                                )
                                detailIntent.putExtra(
                                    DIARY_SEQUENCE,
                                    node.sequence
                                )
                                detailIntent.putExtra(
                                    SELECTED_SEARCH_QUERY,
                                    currentQuery
                                )
//                                                detailIntent.putExtra(
//                                                    SELECTED_SYMBOL_SEQUENCE,
//                                                    DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS
//                                                )
                                TransitionHelper.startActivityWithTransition(
                                    activity,
                                    detailIntent
                                )
                            } else {
                                folderOnClick(node)
                            }
                        }
                    ) {
                        if (!node.isFile) {
//                                        makeToast(node.fullPath)
                            isLoading = true
                            activity?.syncMarkDown(null, node.fullPath) {
                                isLoading = false
                                fetchDiary()
                            }
                        }
                    }
                }
                item {
//                    Spacer(modifier = Modifier.height(bottomPadding.plus(bottomToolbarHeight)))
                    Spacer(modifier = Modifier.height(bottomToolbarHeight.plus(5.dp)))
                }
            }
        }

        val density = LocalDensity.current
        TreeToolbar(
            title = "[Total: $total] category or title",
            modifier = settingCardModifier
                .padding(
                    0.dp,
                    statusBarPadding,
                    0.dp,
                    0.dp
                )
                .align(Alignment.TopCenter)
                .onGloballyPositioned {
                    topToolbarHeight = with(density) { it.size.height.toDp() }
                },
            enableCardViewPolicy = enableCardViewPolicy,
        ) { query ->
            updateQuery(query.trim())
            fetchDiary()
        }

        BottomToolBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onGloballyPositioned {
                    bottomToolbarHeight = with(density) { it.size.height.toDp() }
                },
            bottomPadding = bottomPadding,
            showOptionDialog = { showOptionDialog = true },
            closeCallback = { finishActivityWithTransition(activity) },
            writeDiaryCallback = {
                TransitionHelper.startActivityWithTransition(
                    activity,
                    Intent(
                        context,
                        DiaryWritingActivity::class.java
                    )
                )
            },
            expandTreeCallback = {
                toggleWholeTree(true)
            },
            collapseTreeCallback = {
                toggleWholeTree(false)
            },
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                                    .background(Color(0x88624747))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

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
        modifier = modifier.padding(0.dp, 0.dp, 0.dp, verticalPadding.dp).alpha(0.5f)
        ,
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
    modifier: Modifier = Modifier,
    showOptionDialog: (isShow: Boolean) -> Unit,
    closeCallback: () -> Unit = {},
    writeDiaryCallback: () -> Unit = {},
    expandTreeCallback: () -> Unit = {},
    collapseTreeCallback: () -> Unit = {},
) {
    Box(modifier = modifier.padding(bottom = bottomPadding.plus(5.dp))) {
        val scrollState = rememberScrollState()
        Row (
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),  // 우측정렬 + 간격
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd) // Box 내부에서 우측 하단 배치
                .horizontalScroll(scrollState) // 가로 스크롤 적용
        ) {

            Spacer(modifier = Modifier.width(5.dp))

            FloatingActionButton(
                onClick = { closeCallback() },
                containerColor = Color(LocalContext.current.config.primaryColor),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cross),
                    contentDescription = "Finish Activity"
                )
            }

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
            ElevatedButtonWrapper(text = "길어지면 스크롤") {  }

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

            Spacer(modifier = Modifier.width(5.dp))
        }
    }
}
