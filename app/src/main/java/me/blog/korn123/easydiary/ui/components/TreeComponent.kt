package me.blog.korn123.easydiary.ui.components

import android.content.Intent
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.syncMarkDown
import me.blog.korn123.easydiary.helper.ColorConstants.HIGHLIGHT_COLOR
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.TransitionHelper.Companion.finishActivityWithTransition
import java.util.Calendar

@Composable
fun TreeContent(
    innerPadding: PaddingValues,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    showDebugCard: Boolean = false,
    total: Int,
    treeData: List<Pair<FileNode, Int>>,
    currentQuery: String,
    isResultAPI: Boolean = false,
    fetchDiary: () -> Unit,
    updateQuery: (String) -> Unit,
    toggleWholeTree: (Boolean) -> Unit,
    folderOnClick: (FileNode) -> Unit,
    resultAPICallback: (Int) -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val density = LocalDensity.current
    val config = context.config

    val bottomPadding =
        if (isVanillaIceCreamPlus()) {
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        } else {
            0.dp
        }

    var showOptionDialog by remember { mutableStateOf(false) }
    var visibleSubTitle by remember { mutableStateOf(true) }
    var stretchCard by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    var bottomToolbarHeight by remember { mutableStateOf(0.dp) }
    var topToolbarHeight by remember { mutableStateOf(0.dp) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val filteredTreeData =
        remember(treeData) {
            treeData.filter { it.first.isParentFolderOpen && it.first.isShow }
        }

    val moveToTodayEntryAction = {
        scrollToToday(treeData, listState, toggleWholeTree, topToolbarHeight, density, coroutineScope)
    }

    LaunchedEffect(topToolbarHeight) {
        if (topToolbarHeight > 0.dp) {
            scrollToToday(treeData, listState, toggleWholeTree, topToolbarHeight, density, coroutineScope)
        }
    }

    var thumbVisible by remember { mutableStateOf(false) }
    var isDraggingThumb by remember { mutableStateOf(false) }
    var hideJob: Job? by remember { mutableStateOf(null) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { isScrolling ->
            if (isScrolling) {
                hideJob?.cancel()
                thumbVisible = true
            } else if (!isDraggingThumb) {
                hideJob?.cancel()
                hideJob =
                    launch {
                        delay(300L)
                        thumbVisible = false
                    }
            }
        }
    }

    OptionDialog(
        showDialog = showOptionDialog,
        visibleSubTitle = visibleSubTitle,
        visibleSubTitleChaneCallback = { visibleSubTitle = it },
        stretchCard = stretchCard,
        stretchCardChaneCallback = { stretchCard = it },
        onDismiss = { showOptionDialog = false },
    )

    Box(
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .onSizeChanged { containerSize = it },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(config.screenBackgroundColor)),
            ) {
                item { Spacer(modifier = Modifier.height(topToolbarHeight)) }

                items(
                    items = filteredTreeData,
                    key = { "${it.first.sequence}-${it.first.fullPath}" },
                ) { (node, level) ->
                    TreeCard(
                        sequence = node.sequence,
                        weather = node.weather,
                        title = node.name,
                        subTitle = node.fullPath,
                        level = level,
                        currentTimeMillis = node.currentTimeMillis,
                        isFile = node.isFile,
                        currentQuery = currentQuery,
                        isFolderOpen = node.isFolderOpen,
                        visibleSubTitle = visibleSubTitle,
                        stretchCard = stretchCard,
                        onClick = {
                            handleNodeClick(
                                node,
                                isResultAPI,
                                context,
                                activity,
                                currentQuery,
                                resultAPICallback,
                                folderOnClick,
                            )
                        },
                        onLongClick = {
                            if (!node.isFile) {
                                isLoading = true
                                (activity as ComponentActivity).syncMarkDown(null, node.fullPath) {
                                    isLoading = false
                                    fetchDiary()
                                }
                            }
                        },
                    )
                }

                item { Spacer(modifier = Modifier.height(bottomToolbarHeight + 5.dp)) }
            }
        }

        AnimatedVisibility(
            visible = !thumbVisible,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(1000)),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                TreeToolbar(
                    title = "[Total: $total] category or title",
                    currentQuery = currentQuery,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .zIndex(1f)
                            .align(Alignment.TopCenter)
                            .onGloballyPositioned {
                                topToolbarHeight =
                                    with(density) {
                                        it.size.height
                                            .toDp()
                                            .plus(20.dp)
                                    }
                            },
                    callback = { query ->
                        updateQuery(query)
                        fetchDiary()
                    },
                )

                BottomToolBar(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .onGloballyPositioned {
                                bottomToolbarHeight = with(density) { it.size.height.toDp() }
                            },
                    bottomPadding = bottomPadding,
                    showOptionDialog = { showOptionDialog = true },
                    closeCallback = { finishActivityWithTransition(activity) },
                    writeDiaryCallback = {
                        TransitionHelper.startActivityWithTransition(activity, Intent(context, DiaryWritingActivity::class.java))
                    },
                    expandTreeCallback = { toggleWholeTree(true) },
                    collapseTreeCallback = { toggleWholeTree(false) },
                    scrollTop = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                    scrollEnd = { coroutineScope.launch { listState.animateScrollToItem(filteredTreeData.size.minus(1)) } },
                    moveToTodayEntry = { moveToTodayEntryAction() },
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        FastScroll(
            items = filteredTreeData,
            listState = listState,
            containerHeightPx = containerSize.height.toFloat(),
            isDraggingThumb = isDraggingThumb,
            thumbVisible = thumbVisible,
            containerSize = containerSize,
            modifier = Modifier.align(Alignment.TopEnd),
            showDebugCard = showDebugCard,
            updateThumbVisible = { thumbVisible = it },
            updateDraggingThumb = { isDraggingThumb = it },
            dragEndCallback = {
                hideJob?.cancel()
                coroutineScope.launch {
                    hideJob =
                        launch {
                            delay(300L)
                            if (!isDraggingThumb) thumbVisible = false
                        }
                }
            },
        )
    }
}

@Composable
fun TreeToolbar(
    title: String,
    currentQuery: String = "",
    modifier: Modifier,
    callback: (String) -> Unit,
) {
    val context = LocalContext.current
    val config = context.config
    var isFocused by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(currentQuery) }
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val textUnit = with(density) { config.settingFontSize.toDp().toSp() }
    val fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(context)

    Box(
        modifier =
            modifier
                .shadow(elevation = 15.dp, shape = RoundedCornerShape(15.dp))
                .background(
                    color = if (isFocused) Color(config.primaryColor) else Color(config.backgroundColor),
                    shape = RoundedCornerShape(15.dp),
                ),
    ) {
        TextField(
            value = text,
            onValueChange = {
                text = it
                callback(it)
            },
            label = {
                Text(
                    text = title,
                    style =
                        TextStyle(
                            fontFamily = fontFamily,
                            color = if (isFocused) Color.White else Color(config.textColor),
                            fontSize = textUnit,
                        ),
                )
            },
            colors =
                TextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            textStyle =
                TextStyle(
                    fontFamily = fontFamily,
                    color = if (isFocused) Color.White else Color(config.textColor),
                    fontSize = textUnit,
                ),
            singleLine = true,
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = {
                        text = ""
                        callback("")
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                    }
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TreeCard(
    sequence: Int,
    weather: Int,
    title: String,
    subTitle: String,
    level: Int,
    currentTimeMillis: Long,
    isFile: Boolean,
    currentQuery: String,
    isFolderOpen: Boolean,
    visibleSubTitle: Boolean,
    stretchCard: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val config = context.config
    val fontSize = config.settingFontSize
    val fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(context)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val indentSpacing = (level.minus(1) * 20).dp

        if (!isFile) {
            IconButton(
                onClick = onClick,
                modifier =
                    Modifier
                        .padding(start = indentSpacing)
                        .size(32.dp),
            ) {
                Icon(
                    painter = painterResource(if (isFolderOpen) R.drawable.arrow_drop_down_24px else R.drawable.arrow_right_24px),
                    contentDescription = null,
                    tint = Color(config.primaryColor),
                )
            }
        }

        val cardModifier =
            Modifier
                .padding(1.dp)
                .then(if (isFile) Modifier.padding(start = indentSpacing + 32.dp) else Modifier)
                .then(if (stretchCard) Modifier.fillMaxWidth() else Modifier)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)

        Card(
            shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
            colors = CardDefaults.cardColors(containerColor = Color(config.backgroundColor)),
            modifier = cardModifier,
            elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
        ) {
            Column(modifier = Modifier.padding(10.dp, 7.dp)) {
                NodeHeaderRow(
                    isFile = isFile,
                    sequence = sequence,
                    title = title,
                    weather = weather,
                    currentQuery = currentQuery,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                )

                if (visibleSubTitle) {
                    val displaySubTitle = if (config.enableDebugOptionVisibleDiarySequence) "[level: $level] $subTitle" else subTitle
                    SimpleText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = displaySubTitle,
                        fontSize = fontSize * 0.8f,
                        fontFamily = fontFamily,
                        maxLines = 1,
                    )
                }

                if (config.enableDebugOptionVisibleTreeStatus) {
                    SimpleText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = "[isFolderOpen: $isFolderOpen][level: $level][time: $currentTimeMillis]",
                        fontSize = fontSize * 0.8f,
                        fontFamily = fontFamily,
                    )
                }
            }
        }
    }
}

@Composable
private fun NodeHeaderRow(
    isFile: Boolean,
    sequence: Int,
    title: String,
    weather: Int,
    currentQuery: String,
    fontSize: Float,
    fontFamily: FontFamily?,
) {
    val config = LocalContext.current.config
    Row(verticalAlignment = Alignment.CenterVertically) {
        val displayTitle =
            buildString {
                if (config.enableDebugOptionVisibleDiarySequence) append("[$sequence] ")
                if (!isFile) append("📂 ")
                append(title)
            }

        val annotatedText =
            remember(displayTitle, currentQuery) {
                getAnnotatedTitle(displayTitle, currentQuery, HIGHLIGHT_COLOR)
            }

        if (isFile) {
            AndroidView(
                modifier = Modifier.size(20.dp),
                factory = { ctx -> ImageView(ctx) },
                update = { FlavorUtils.initWeatherView(it.context, it, weather) },
            )
            Spacer(modifier = Modifier.width(5.dp))
        }

        SimpleText(
            text = annotatedText,
            fontSize = fontSize,
            fontFamily = fontFamily,
            maxLines = 1,
        )
    }
}

@Composable
fun OptionDialog(
    showDialog: Boolean,
    visibleSubTitle: Boolean,
    visibleSubTitleChaneCallback: (Boolean) -> Unit,
    stretchCard: Boolean,
    stretchCardChaneCallback: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!showDialog) return

    val config = LocalContext.current.config
    AlertDialog(
        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
        containerColor = Color(config.backgroundColor),
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                SimpleText(text = "확인")
            }
        },
        icon = {
            Icon(painterResource(R.drawable.ic_easydiary), contentDescription = null, tint = Color(config.textColor))
        },
        title = {
            SimpleText(text = "트리뷰 옵션설정", fontWeight = FontWeight.Bold, fontSize = config.settingFontSize * 1.3f)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OptionSwitchRow("Node 전체경로 표시", visibleSubTitle, visibleSubTitleChaneCallback)
                OptionSwitchRow("아이템 카드 스트레치", stretchCard, stretchCardChaneCallback)
            }
        },
    )
}

@Composable
private fun OptionSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SimpleText(modifier = Modifier.weight(1f), text = label)
        Switch(
            modifier = Modifier.padding(start = 10.dp),
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent =
                if (checked) {
                    { Icon(Icons.Filled.Check, null, Modifier.size(SwitchDefaults.IconSize)) }
                } else {
                    null
                },
        )
    }
}

@Composable
fun BottomToolBar(
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    showOptionDialog: (Boolean) -> Unit,
    closeCallback: () -> Unit,
    writeDiaryCallback: () -> Unit,
    expandTreeCallback: () -> Unit,
    collapseTreeCallback: () -> Unit,
    scrollTop: () -> Unit,
    scrollEnd: () -> Unit,
    moveToTodayEntry: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 5.dp),
    ) {
        val focusManager = LocalFocusManager.current
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(5.dp))
            CustomElevatedSquareButton("Close", R.drawable.ic_cross) { closeCallback() }
            CustomElevatedSquareButton("New Entry", R.drawable.ic_edit) { writeDiaryCallback() }
            CustomElevatedSquareButton("TODAY", R.drawable.ic_time_8_w) { moveToTodayEntry() }
            CustomElevatedSquareButton("Expand All", R.drawable.ic_expand) { expandTreeCallback() }
            CustomElevatedSquareButton("Collapse All", R.drawable.ic_collapse) { collapseTreeCallback() }
            CustomElevatedSquareButton("Top", R.drawable.ic_arrow_top) { scrollTop() }
            CustomElevatedSquareButton("Bottom", R.drawable.ic_arrow_bottom) { scrollEnd() }
            CustomElevatedSquareButton("Clear Focus", R.drawable.ic_update) { focusManager.clearFocus() }
            CustomElevatedSquareButton("Settings", R.drawable.ic_settings_7) { showOptionDialog(true) }
            Spacer(modifier = Modifier.width(5.dp))
        }
    }
}

private fun scrollToToday(
    treeData: List<Pair<FileNode, Int>>,
    listState: LazyListState,
    toggleWholeTree: (Boolean) -> Unit,
    topToolbarHeight: Dp,
    density: Density,
    scope: CoroutineScope,
) {
    val tomorrowTimeMillis = EasyDiaryUtils.getCalendarInstance(false, Calendar.DAY_OF_MONTH, 1).timeInMillis
    val target =
        treeData
            .filter { it.first.currentTimeMillis < tomorrowTimeMillis }
            .maxByOrNull { it.first.currentTimeMillis }

    target?.let {
        val position = getIndexBySequence(treeData, it.first.sequence)
        if (position != -1) {
            toggleWholeTree(true)
            scope.launch {
                listState.scrollToItem(
                    position + 1,
                    with(density) { -topToolbarHeight.toPx().toInt() },
                )
            }
        }
    }
}

private fun handleNodeClick(
    node: FileNode,
    isResultAPI: Boolean,
    context: android.content.Context,
    activity: android.app.Activity?,
    currentQuery: String,
    resultAPICallback: (Int) -> Unit,
    folderOnClick: (FileNode) -> Unit,
) {
    if (node.isFile) {
        if (isResultAPI) {
            resultAPICallback(node.sequence)
        } else {
            val detailIntent =
                Intent(context, DiaryReadingActivity::class.java).apply {
                    putExtra(DIARY_SEQUENCE, node.sequence)
                    putExtra(SELECTED_SEARCH_QUERY, currentQuery)
                }
            TransitionHelper.startActivityWithTransition(activity, detailIntent)
        }
    } else {
        folderOnClick(node)
    }
}

private fun getAnnotatedTitle(
    title: String,
    query: String,
    highlightColor: Int,
): AnnotatedString =
    buildAnnotatedString {
        append(title)
        if (query.isNotBlank()) {
            var startIndex = title.indexOf(query, 0, ignoreCase = true)
            while (startIndex >= 0) {
                addStyle(
                    style = SpanStyle(background = Color(highlightColor), color = Color.Black),
                    start = startIndex,
                    end = startIndex + query.length,
                )
                startIndex = title.indexOf(query, startIndex + query.length, ignoreCase = true)
            }
        }
    }

private fun getIndexBySequence(
    treeData: List<Pair<FileNode, Int>>,
    sequence: Int,
): Int = treeData.indexOfFirst { it.first.sequence == sequence }
