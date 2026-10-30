package me.blog.korn123.easydiary.presentation.main.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.presentation.components.FastScroll
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

@Composable
fun CompactLayout(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    isDraggingThumb: Boolean,
    thumbVisible: Boolean,
    hideJob: Job?,
    currentQuery: String,
    delayTimeMillis: Long,
    enableCardViewPolicy: Boolean,
    items: List<DiaryDomain>,
    symbolSequence: Int = 0,
    thumbVisibleCallback: (thumbVisible: Boolean) -> Unit,
    isDraggingThumbCallback: (isDraggingThumb: Boolean) -> Unit,
    hideJobCallback: (job: Job) -> Unit,
    onQueryChange: (query: String) -> Unit = {},
    clearFilter: () -> Unit = {},
    itemClickCallback: (diary: DiaryDomain) -> Unit = {},
    itemLongClickCallback: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()
    val speechLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                    if (results.isNotEmpty()) {
                        onQueryChange(results[0])
                    }
                }
            }
        }
    val onMicrophoneClick: () -> Unit = {
        try {
            val intent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                }
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            activity?.makeSnackBar(activity.getString(R.string.recognizer_intent_not_found_message))
        }
    }
    var enablePhotoHighlight by remember { mutableStateOf(context.config.enablePhotoHighlightInDiaryMain) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        enablePhotoHighlight = context.config.enablePhotoHighlightInDiaryMain
    }

    var bottomSheetHeight by remember { mutableStateOf(0.dp) }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val photoHighlightMaxHeight = 160.dp + statusBarTop
    val photoHighlightScrollBehavior = rememberPhotoHighlightScrollBehavior(photoHighlightMaxHeight)
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier =
            modifier
                .padding(innerPadding)
                .fillMaxSize()
                .nestedScroll(photoHighlightScrollBehavior.nestedScrollConnection),
    ) {
        if (enablePhotoHighlight) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(photoHighlightScrollBehavior.currentHeight)
                        .graphicsLayer {
                            alpha = photoHighlightScrollBehavior.alpha
                        },
            ) {
                val currentTopPadding =
                    if (photoHighlightMaxHeight > 0.dp) {
                        statusBarTop * (photoHighlightScrollBehavior.currentHeight / photoHighlightMaxHeight)
                    } else {
                        0.dp
                    }
                PhotoHighlightCard(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = currentTopPadding),
                    height = 160.dp,
                    isVisible = enablePhotoHighlight,
                    onVisibilityChanged = { isVisible ->
                        enablePhotoHighlight = isVisible
                    },
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        top = if (enablePhotoHighlight) 0.dp else statusBarTop,
                        bottom = bottomSheetHeight,
                    ),
            ) {
                itemsIndexed(items) { _, diary ->
                    Card(
                        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
                        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
                        modifier =
                            (
                                if (enableCardViewPolicy) {
                                    Modifier.padding(
                                        HORIZONTAL_PADDING.dp,
                                        VERTICAL_PADDING.dp,
                                    )
                                } else {
                                    Modifier
                                        .padding(1.dp, 1.dp)
                                }
                            ).combinedClickable(
                                onClick = { itemClickCallback(diary) },
                                onLongClick = { itemLongClickCallback() },
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
                isDraggingThumb = isDraggingThumb,
                thumbVisible = thumbVisible,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = if (enablePhotoHighlight) 0.dp else statusBarTop,
                            bottom = bottomBarPadding,
                        ),
                showDebugCard = false,
                updateThumbVisible = { thumbVisibleCallback(it) },
                updateDraggingThumb = { isDraggingThumbCallback(it) },
                dragEndCallback = {
                    hideJob?.cancel()
                    coroutineScope.launch {
                        val job =
                            launch {
                                delay(delayTimeMillis.milliseconds)
                                if (!isDraggingThumb) {
                                    thumbVisibleCallback(false)
                                }
                            }
                        hideJobCallback(job)
                    }
                },
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = !thumbVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                MainBottomSheet(
                    title = "[Total: ${items.size}] ${stringResource(id = R.string.guide_message_2)}",
                    currentQuery = currentQuery,
                    symbolSequence = symbolSequence,
                    enableCardViewPolicy = enableCardViewPolicy,
                    onMicrophoneClick = onMicrophoneClick,
                    clearFilter = clearFilter,
                    onMenuClick = onMenuClick,
                    onMoveTop = {
                        coroutineScope.launch {
                            if (items.isNotEmpty()) {
                                listState.scrollToItem(0)
                            }
                        }
                    },
                    onMoveBottom = {
                        coroutineScope.launch {
                            if (items.isNotEmpty()) {
                                listState.scrollToItem(items.size - 1)
                            }
                        }
                    },
                    onMoveToday = {
                        activity?.let { act ->
                            val tomorrowTimeMillis =
                                EasyDiaryUtils.getCalendarInstance(false, java.util.Calendar.DAY_OF_MONTH, 1).timeInMillis
                            val filteredDiary =
                                items.filter { diary -> diary.currentTimeMillis < tomorrowTimeMillis }
                            val target = filteredDiary.maxByOrNull { diary -> diary.currentTimeMillis }
                            target?.let {
                                val position = items.indexOfFirst { item -> item.diaryId == target.diaryId }
                                if (position != -1) {
                                    coroutineScope.launch {
                                        listState.scrollToItem(position)
                                    }
                                }
                            }
                        }
                    },
                    callbackQuery = { query ->
                        onQueryChange(query)
                    },
                    onHeightChanged = { bottomSheetHeight = it },
                )
            }
        }
    }
}

@Composable
fun CompactLayout2(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    isDraggingThumb: Boolean,
    thumbVisible: Boolean,
    hideJob: Job?,
    currentQuery: String,
    delayTimeMillis: Long,
    durationMillis: Int,
    enableCardViewPolicy: Boolean,
    items: List<DiaryDomain>,
    symbolSequence: Int = 0,
    thumbVisibleCallback: (thumbVisible: Boolean) -> Unit,
    isDraggingThumbCallback: (isDraggingThumb: Boolean) -> Unit,
    hideJobCallback: (job: Job) -> Unit,
    onQueryChange: (query: String) -> Unit = {},
    clearFilter: () -> Unit = {},
    itemClickCallback: (diary: DiaryDomain) -> Unit = {},
    itemLongClickCallback: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()
    val speechLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                    if (results.isNotEmpty()) {
                        onQueryChange(results[0])
                    }
                }
            }
        }
    val onMicrophoneClick: () -> Unit = {
        try {
            val intent =
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                }
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            activity?.makeSnackBar(activity.getString(R.string.recognizer_intent_not_found_message))
        }
    }
    var enablePhotoHighlight by remember { mutableStateOf(context.config.enablePhotoHighlightInDiaryMain) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        enablePhotoHighlight = context.config.enablePhotoHighlightInDiaryMain
    }

    var bottomSheetHeight by remember { mutableStateOf(0.dp) }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val photoHighlightMaxHeight = 160.dp + statusBarTop
    val photoHighlightScrollBehavior = rememberPhotoHighlightScrollBehavior(photoHighlightMaxHeight)
    val bottomBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier =
            modifier
                .padding(innerPadding)
                .fillMaxSize()
                .nestedScroll(photoHighlightScrollBehavior.nestedScrollConnection),
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        top = if (enablePhotoHighlight) 0.dp else statusBarTop,
                        bottom = bottomSheetHeight,
                    ),
            ) {
                itemsIndexed(items) { index, diary ->
                    if (enablePhotoHighlight && index == 0) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                        ) {
                            val currentTopPadding =
                                if (photoHighlightMaxHeight > 0.dp) {
                                    statusBarTop * (photoHighlightScrollBehavior.currentHeight / photoHighlightMaxHeight)
                                } else {
                                    0.dp
                                }
                            PhotoHighlightCard(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(top = currentTopPadding),
                                height = 160.dp,
                                isVisible = enablePhotoHighlight,
                                onVisibilityChanged = { isVisible ->
                                    enablePhotoHighlight = isVisible
                                },
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
                        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
                        modifier =
                            (
                                if (enableCardViewPolicy) {
                                    Modifier.padding(
                                        HORIZONTAL_PADDING.dp,
                                        VERTICAL_PADDING.dp,
                                    )
                                } else {
                                    Modifier
                                        .padding(1.dp, 1.dp)
                                }
                            ).combinedClickable(
                                onClick = { itemClickCallback(diary) },
                                onLongClick = { itemLongClickCallback() },
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
                isDraggingThumb = isDraggingThumb,
                thumbVisible = thumbVisible,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = if (enablePhotoHighlight) 0.dp else statusBarTop,
                            bottom = bottomBarPadding,
                        ),
                showDebugCard = false,
                updateThumbVisible = { thumbVisibleCallback(it) },
                updateDraggingThumb = { isDraggingThumbCallback(it) },
                dragEndCallback = {
                    hideJob?.cancel()
                    coroutineScope.launch {
                        val job =
                            launch {
                                delay(delayTimeMillis.milliseconds)
                                if (!isDraggingThumb) {
                                    thumbVisibleCallback(false)
                                }
                            }
                        hideJobCallback(job)
                    }
                },
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = !thumbVisible,
                enter = fadeIn(animationSpec = tween(durationMillis)),
                exit = fadeOut(animationSpec = tween(durationMillis)),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                MainBottomSheet(
                    title = "[Total: ${items.size}] ${stringResource(id = R.string.guide_message_2)}",
                    currentQuery = currentQuery,
                    symbolSequence = symbolSequence,
                    enableCardViewPolicy = enableCardViewPolicy,
                    onMicrophoneClick = onMicrophoneClick,
                    clearFilter = clearFilter,
                    onMenuClick = onMenuClick,
                    onMoveTop = {
                        coroutineScope.launch {
                            if (items.isNotEmpty()) {
                                listState.scrollToItem(0)
                            }
                        }
                    },
                    onMoveBottom = {
                        coroutineScope.launch {
                            if (items.isNotEmpty()) {
                                listState.scrollToItem(items.size - 1)
                            }
                        }
                    },
                    callbackQuery = { query ->
                        onQueryChange(query)
                    },
                    onHeightChanged = { bottomSheetHeight = it },
                )
            }
        }
    }
}
