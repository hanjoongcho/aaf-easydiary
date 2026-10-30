package me.blog.korn123.easydiary.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplemobiletools.commons.extensions.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.enums.DiaryMode
import me.blog.korn123.easydiary.extensions.applyFullScreenStatusBarTheme
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getThemeId
import me.blog.korn123.easydiary.extensions.updateNavigationBarAppearance
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.SELECTED_SYMBOL_SEQUENCE
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.main.components.CompactLayout
import me.blog.korn123.easydiary.presentation.main.components.MediumLayout
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import kotlin.time.Duration.Companion.milliseconds
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

@AndroidEntryPoint
class DiaryMainActivity : EasyDiaryComposeBaseActivity() {
    private val viewModel: DiaryMainViewModel by viewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @OptIn(
        ExperimentalLayoutApi::class,
        ExperimentalMaterial3WindowSizeClassApi::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeId())
        super.onCreate(savedInstanceState)

        setContent {
            val currentQuery: String by diaryViewModel.query.collectAsState()
            val items: List<DiaryDomain> by diaryViewModel.diaries.collectAsStateWithLifecycle()
            val currentSymbol: Int by viewModel.symbol.collectAsStateWithLifecycle()
            val modifier: Modifier = Modifier

            val focusManager = LocalFocusManager.current
            val isKeyboardVisible = WindowInsets.isImeVisible

            val windowSizeClass = calculateWindowSizeClass(this)

            LaunchedEffect(isKeyboardVisible) {
                if (!isKeyboardVisible) {
                    focusManager.clearFocus()
                }
            }

            AppTheme {
                enableEdgeToEdge()
                applyFullScreenStatusBarTheme()
                updateNavigationBarAppearance()
                Scaffold(
                    contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
                    containerColor = Color(config.screenBackgroundColor),
                    content = { innerPadding ->
                        val enableCardViewPolicy: Boolean by settingsViewModel.enableCardViewPolicy.collectAsState()
                        val listState = rememberLazyListState()
                        var thumbVisible by remember { mutableStateOf(false) }
                        var isDraggingThumb by remember { mutableStateOf(false) } // 토글: 썸을 누르고 있는지
                        var hideJob: Job? by remember { mutableStateOf(null) }
                        val delayTimeMillis = 500L

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
                                                delay(delayTimeMillis.milliseconds)
                                                if (!isDraggingThumb) thumbVisible = false
                                            }
                                    }
                                }
                        }

                        fun itemClickCallback(diary: DiaryDomain) {
                            val detailIntent = Intent(this@DiaryMainActivity, DiaryReadingActivity::class.java)
                            detailIntent.putExtra(DIARY_SEQUENCE, diary.diaryId)
                            detailIntent.putExtra(SELECTED_SEARCH_QUERY, diaryViewModel.query.value)
                            detailIntent.putExtra(SELECTED_SYMBOL_SEQUENCE, viewModel.symbol.value)
                            TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, detailIntent)
                        }

                        fun itemLongClickCallback() {
                            toast("itemLongClickCallback")
                        }

                        when (windowSizeClass.widthSizeClass) {
                            WindowWidthSizeClass.Compact -> {
                                // 폰: 리스트 하나만 꽉 채워서 보여줌
                                CompactLayout(
                                    innerPadding = innerPadding,
                                    modifier = modifier,
                                    listState = listState,
                                    isDraggingThumb = isDraggingThumb,
                                    thumbVisible = thumbVisible,
                                    hideJob = hideJob,
                                    currentQuery = currentQuery,
                                    delayTimeMillis = delayTimeMillis,
                                    enableCardViewPolicy = enableCardViewPolicy,
                                    items = items,
                                    symbolSequence = currentSymbol,
                                    thumbVisibleCallback = { value -> thumbVisible = value },
                                    isDraggingThumbCallback = { value -> isDraggingThumb = value },
                                    hideJobCallback = { value -> hideJob = value },
                                    onQueryChange = { query -> diaryViewModel.query.value = query },
                                    clearFilter = {
                                        viewModel.updateSymbolSequence(SYMBOL_SELECT_ALL)
                                        diaryViewModel.symbolSequence.value = SYMBOL_SELECT_ALL
                                        diaryViewModel.query.value = ""
                                    },
                                    itemClickCallback = { itemClickCallback(it) },
                                    itemLongClickCallback = { itemLongClickCallback() },
                                )
                            }

                            WindowWidthSizeClass.Medium -> {
                                // 폴더블: 리스트를 보여주고 옆에 작은 툴바를 띄움
                                MediumLayout(
                                    innerPadding = innerPadding,
                                    modifier = modifier,
                                    listState = listState,
                                    isDraggingThumb = isDraggingThumb,
                                    thumbVisible = thumbVisible,
                                    hideJob = hideJob,
                                    currentQuery = currentQuery,
                                    delayTimeMillis = delayTimeMillis,
                                    enableCardViewPolicy = enableCardViewPolicy,
                                    items = items,
                                    symbolSequence = currentSymbol,
                                    thumbVisibleCallback = { value -> thumbVisible = value },
                                    isDraggingThumbCallback = { value -> isDraggingThumb = value },
                                    hideJobCallback = { value -> hideJob = value },
                                    onQueryChange = { query -> diaryViewModel.query.value = query },
                                    clearFilter = {
                                        viewModel.updateSymbolSequence(SYMBOL_SELECT_ALL)
                                        diaryViewModel.symbolSequence.value = SYMBOL_SELECT_ALL
                                        diaryViewModel.query.value = ""
                                    },
                                    itemClickCallback = { itemClickCallback(it) },
                                    itemLongClickCallback = { itemLongClickCallback() },
                                )
                            }

                            WindowWidthSizeClass.Expanded -> {
                                // 태블릿: 좌측엔 리스트, 우측엔 상세화면 (2단 분리)
                            }
                        }
                    },
                )
            }
        }
    }
}
