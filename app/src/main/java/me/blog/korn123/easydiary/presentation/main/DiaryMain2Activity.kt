package me.blog.korn123.easydiary.presentation.main

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
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain
import me.blog.korn123.easydiary.extensions.applyFullScreenStatusBarTheme
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getThemeId
import me.blog.korn123.easydiary.extensions.updateNavigationBarAppearance
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.main.components.CompactLayout2
import me.blog.korn123.easydiary.presentation.theme.AppTheme

@AndroidEntryPoint
class DiaryMain2Activity : EasyDiaryComposeBaseActivity() {
    private val viewModel: DiaryMainViewModel by viewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @OptIn(
        ExperimentalLayoutApi::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeId())
        super.onCreate(savedInstanceState)

        setContent {
            val currentQuery: String by viewModel.currentQuery.collectAsState()
            val items: List<DiaryDomain> by diaryViewModel.diaries.collectAsStateWithLifecycle()
            val modifier: Modifier = Modifier

            val focusManager = LocalFocusManager.current
            val isKeyboardVisible = WindowInsets.isImeVisible

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

                        fun itemClickCallback(diary: DiaryDomain) {
                            toast("itemClickCallback: ${diary.title}")
                        }

                        fun itemLongClickCallback() {
                            toast("itemLongClickCallback")
                        }

                        CompactLayout2(
                            innerPadding = innerPadding,
                            modifier = modifier,
                            listState = listState,
                            isDraggingThumb = isDraggingThumb,
                            thumbVisible = thumbVisible,
                            hideJob = hideJob,
                            currentQuery = currentQuery,
                            delayTimeMillis = delayTimeMillis,
                            durationMillis = durationMillis,
                            enableCardViewPolicy = enableCardViewPolicy,
                            items = items,
                            thumbVisibleCallback = { value -> thumbVisible = value },
                            isDraggingThumbCallback = { value -> isDraggingThumb = value },
                            hideJobCallback = { value -> hideJob = value },
                            onQueryChange = { query -> diaryViewModel.query.value = query },
                            itemClickCallback = { itemClickCallback(it) },
                            itemLongClickCallback = { itemLongClickCallback() },
                        )
                    },
                )
            }
        }
    }
}
