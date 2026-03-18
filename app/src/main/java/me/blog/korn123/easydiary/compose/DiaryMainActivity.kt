package me.blog.korn123.easydiary.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.simplemobiletools.commons.extensions.toast
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DevActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.extensions.applyFullScreenStatusBarTheme
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getThemeId
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.showBetaFeatureMessage
import me.blog.korn123.easydiary.extensions.updateNavigationBarAppearance
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.ui.components.BottomToolBarContainer
import me.blog.korn123.easydiary.ui.components.CustomElevatedButton
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.components.FastScroll
import me.blog.korn123.easydiary.ui.components.LegacyDiaryItemCard
import me.blog.korn123.easydiary.ui.components.PhotoHighlightCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

class DiaryMainActivity : EasyDiaryComposeBaseActivity() {
    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeId())
        super.onCreate(savedInstanceState)

        setContent {
            val topAppBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
            val bottomPadding =
                if (isVanillaIceCreamPlus()) {
                    WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                } else {
                    0.dp
                }

            val items: List<Diary> = EasyDiaryDbHelper.findDiary(null)
            val modifier: Modifier = Modifier
            AppTheme {
                applyFullScreenStatusBarTheme()
                updateNavigationBarAppearance()
                Scaffold(
                    contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                    containerColor = Color(config.screenBackgroundColor),
                    content = { innerPadding ->
                        val context = LocalContext.current
                        val activity = LocalActivity.current
                        val coroutineScope = rememberCoroutineScope()
                        val settingCardModifier = Modifier.fillMaxWidth()
                        val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
                            context.config.enableCardViewPolicy,
                        )
                        var topToolbarHeight by remember { mutableStateOf(0.dp) }
                        val density = LocalDensity.current
                        val listState = rememberLazyListState()
                        var thumbVisible by remember { mutableStateOf(false) }
                        var containerSize by remember { mutableStateOf(IntSize.Zero) } // 컨테이너 높이(픽셀)
                        var isDraggingThumb by remember { mutableStateOf(false) } // 토글: 썸을 누르고 있는지
                        var hideJob: Job? by remember { mutableStateOf(null) }
                        val delayTimeMillis = 500L
                        val durationMillis = 1200

                        // 스크롤 이벤트 감지
                        LaunchedEffect(listState) {
                            snapshotFlow { listState.isScrollInProgress }
                                .collect { isScrolling ->
                                    if (isScrolling) {
                                        hideJob?.cancel()
                                        thumbVisible = true
                                    } else {
                                        hideJob?.cancel()
                                        hideJob =
                                            launch {
                                                delay(delayTimeMillis)
                                                if (!isDraggingThumb) thumbVisible = false
                                            }
                                    }
                                }
                        }

                        fun itemClickCallback(diary: Diary) {
                            activity?.toast("itemClickCallback: ${diary.title}")
                        }

                        fun itemLongClickCallback() {
                            activity?.toast("itemLongClickCallback")
                        }

                        Column(
                            modifier =
                                modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                        ) {
                            PhotoHighlightCard()
                            Box(
                                modifier = Modifier.weight(1f),
                            ) {
                                LazyColumn(
                                    state = listState,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .onSizeChanged { containerSize = it },
                                    contentPadding =
                                        PaddingValues(
                                            bottom =
                                                WindowInsets.navigationBars
                                                    .asPaddingValues()
                                                    .calculateBottomPadding(),
                                        ),
                                ) {
                                    itemsIndexed(items) { index, diary ->
                                        Card(
                                            shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
                                            colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
                                            modifier = (
                                                if (enableCardViewPolicy) {
                                                    modifier.padding(
                                                        HORIZONTAL_PADDING.dp,
                                                        VERTICAL_PADDING.dp,
                                                    )
                                                } else {
                                                    modifier
                                                        .padding(1.dp, 1.dp)
                                                }
                                            ),
                                            elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
                                        ) {
                                            LegacyDiaryItemCard(
                                                diary = diary,
                                                itemClickCallback = { itemClickCallback(it) },
                                                itemLongClickCallback = { itemLongClickCallback() },
                                            )
                                        }
                                    }
                                }

                                FastScroll(
                                    items = items,
                                    listState = listState,
                                    containerHeightPx =
                                        containerSize.height.toFloat().minus(
                                            with(
                                                LocalDensity.current,
                                            ) {
                                                WindowInsets.navigationBars
                                                    .asPaddingValues()
                                                    .calculateBottomPadding()
                                                    .toPx()
//                                                    .plus(
//                                                        WindowInsets.statusBars
//                                                            .asPaddingValues()
//                                                            .calculateTopPadding()
//                                                            .toPx(),
//                                                    )
                                            },
                                        ),
                                    isDraggingThumb = isDraggingThumb,
                                    thumbVisible = thumbVisible,
                                    containerSize = containerSize,
                                    modifier =
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(
//                                                top =
//                                                    WindowInsets.statusBars
//                                                        .asPaddingValues()
//                                                        .calculateTopPadding(),
                                                bottom =
                                                    WindowInsets.navigationBars
                                                        .asPaddingValues()
                                                        .calculateBottomPadding(),
                                            ),
                                    showDebugCard = false,
                                    updateThumbVisible = { thumbVisible = it },
                                    updateDraggingThumb = { isDraggingThumb = it },
                                    dragEndCallback = {
                                        hideJob?.cancel()
                                        coroutineScope.launch {
                                            hideJob =
                                                launch {
                                                    delay(delayTimeMillis)
                                                    if (!isDraggingThumb) thumbVisible = false
                                                }
                                        }
                                    },
                                )

                                this@Column.AnimatedVisibility(
                                    visible = !thumbVisible,
                                    enter = fadeIn(animationSpec = tween(durationMillis)),
                                    exit = fadeOut(animationSpec = tween(durationMillis)),
                                    modifier =
                                        Modifier
                                            .align(Alignment.BottomCenter),
                                ) {
                                    Column {
                                        BottomToolBarContainer(
                                            isAutoPadding = false,
                                        ) {
                                            CustomElevatedButton(
                                                text = getString(R.string.button_new_entry),
                                                iconResourceId = R.drawable.ic_edit,
                                                iconSize = 16.dp,
                                            ) {
                                                val createDiary =
                                                    Intent(this@DiaryMainActivity, DiaryWritingActivity::class.java)
                                                TransitionHelper.startActivityWithTransition(
                                                    this@DiaryMainActivity,
                                                    createDiary,
                                                )
                                            }
                                            CustomElevatedButton(
                                                text = getString(R.string.button_tree_view),
                                                iconResourceId = R.drawable.ic_tree_structure,
                                                iconSize = 16.dp,
                                            ) {
                                                TransitionHelper.startActivityWithTransition(
                                                    this@DiaryMainActivity,
                                                    Intent(this@DiaryMainActivity, TreeTimelineActivity::class.java),
                                                )
                                            }
                                            CustomElevatedButton(text = "TODAY", iconResourceId = R.drawable.ic_time_8_w, iconSize = 16.dp) {
                                                //                                        moveToday()
                                                makeSnackBar("moveToday()")
                                            }

                                            if (config.enableDebugMode) {
                                                CustomElevatedButton(
                                                    text = getString(R.string.button_quick_settings),
                                                    iconResourceId = R.drawable.ic_running,
                                                    iconSize = 16.dp,
                                                ) {
                                                    TransitionHelper.startActivityWithTransition(
                                                        this@DiaryMainActivity,
                                                        Intent(this@DiaryMainActivity, QuickSettingsActivity::class.java),
                                                    )
                                                }
                                                CustomElevatedButton(iconResourceId = R.drawable.ic_bug_2, iconSize = 16.dp) {
                                                    TransitionHelper.startActivityWithTransition(
                                                        this@DiaryMainActivity,
                                                        Intent(this@DiaryMainActivity, DevActivity::class.java),
                                                    )
                                                }
                                                CustomElevatedButton(
                                                    text = "MENU",
                                                    iconResourceId = R.drawable.ic_options_three_dots,
                                                    iconSize = 16.dp,
                                                ) {
                                                    //                                            openCustomOptionMenu()
                                                    makeSnackBar("openCustomOptionMenu()")
                                                }
                                            }
                                        }
                                        MainToolbar(
                                            title = "category or title",
                                            currentQuery = "",
                                            enableCardViewPolicy = enableCardViewPolicy,
                                        ) { query ->
                                        }
                                    }
                                }
                            }
                        }
                    },
                )
            }
        }
    }

    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @Composable
    fun MainToolbar(
        title: String,
        currentQuery: String = "",
        enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
        fontSize: Float = LocalContext.current.config.settingFontSize,
        fontColor: Color = Color(LocalContext.current.config.textColor),
        alpha: Float = 1.0f,
        fontWeight: FontWeight = FontWeight.Normal,
        fontFamily: FontFamily? =
            if (LocalInspectionMode.current) {
                null
            } else {
                FontUtils.getComposeFontFamily(
                    LocalContext.current,
                )
            },
        lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
        callback: (query: String) -> Unit = {},
    ) {
        var isFocused by remember { mutableStateOf(false) }
        Box(
//        shape = RoundedCornerShape(bottomStart = roundedCornerShapeSize.dp, bottomEnd = roundedCornerShapeSize.dp),
//        shape = RoundedCornerShape(15.dp),
//        colors = CardDefaults.cardColors(Color(LocalContext.current.config.primaryColor)),
            modifier =
                Modifier
                    .imePadding() // navigationBarsPadding() 보다 우선 순위가 높음
//                    .padding(0.dp, 10.dp)
                    .shadow(
                        elevation = 15.dp,
                        shape = RoundedCornerShape(15.dp),
                        clip = false, // 기본값
                    ).background(
                        color =
                            if (isFocused) {
                                Color(LocalContext.current.config.primaryColor)
                            } else {
                                Color(
                                    LocalContext.current.config.backgroundColor,
                                )
                            },
                        shape = RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp),
                    ),
            //            .alpha(0.8f)
//        modifier = (if (enableCardViewPolicy) modifier.padding(horizontalPadding.dp, verticalPadding.dp) else modifier
//            .padding(5.dp, 5.dp)),
//        elevation = CardDefaults.cardElevation(defaultElevation = roundedCornerShapeSize.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(5.dp)
                        .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var text by remember { mutableStateOf(currentQuery) }
                    val density = LocalDensity.current
                    val textUnit =
                        with(density) {
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
                        label = {
                            Text(
                                text = title,
                                style =
                                    TextStyle(
                                        fontFamily = fontFamily,
                                        fontWeight = fontWeight,
//                        fontStyle = FontStyle.Italic,
//                        color = fontColor.copy(alpha),
                                        color = if (isFocused) Color.White else Color(LocalContext.current.config.textColor),
                                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                                    ),
                            )
                        },
                        colors =
                            TextFieldDefaults.colors(
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent, // 포커스 시 배경
                                unfocusedContainerColor = Color.Transparent, // 포커스 없을 때 배경
                            ),
                        textStyle =
                            TextStyle(
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
//                        fontStyle = FontStyle.Italic,
//                        color = fontColor.copy(alpha),
                                color = if (isFocused) Color.White else Color(LocalContext.current.config.textColor),
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
                                        contentDescription = "Clear text",
                                        tint = Color.White,
                                    )
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(0.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                },
                    )
                }
            }
        }
    }
}
