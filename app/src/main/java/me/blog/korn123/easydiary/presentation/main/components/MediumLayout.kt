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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeSnackBar

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.presentation.components.FastScroll
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

@Composable
fun MediumLayout(
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
    thumbVisibleCallback: (thumbVisible: Boolean) -> Unit,
    isDraggingThumbCallback: (isDraggingThumb: Boolean) -> Unit,
    hideJobCallback: (job: Job) -> Unit,
    onQueryChange: (query: String) -> Unit = {},
    itemClickCallback: (diary: DiaryDomain) -> Unit = {},
    itemLongClickCallback: () -> Unit = {},
) {
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

    Row(
        modifier =
            Modifier
                .fillMaxWidth(),
    ) {
        Column(
            modifier =
                modifier
                    .weight(0.4f)
                    .padding(innerPadding)
                    .fillMaxSize(),
        ) {
            PhotoHighlightCard(onVisibilityChanged = { _ ->
            })
        }
        Column(modifier = Modifier.weight(0.6f)) {
            Box(
                modifier = Modifier.weight(1f),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            bottom =
                                WindowInsets.navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding(),
                        ),
                ) {
                    itemsIndexed(items) { _, diary ->
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
                    isDraggingThumb = isDraggingThumb,
                    thumbVisible = thumbVisible,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(
                                bottom =
                                    WindowInsets.navigationBars
                                        .asPaddingValues()
                                        .calculateBottomPadding(),
                            ),
                    showDebugCard = false,
                    updateThumbVisible = { thumbVisibleCallback(it) },
                    updateDraggingThumb = { isDraggingThumbCallback(it) },
                    dragEndCallback = {
                        hideJob?.cancel()
                        coroutineScope.launch {
                            val job =
                                launch {
                                    delay(delayTimeMillis)
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
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter),
                ) {
                    MainBottomSheet(
                        title = "[Total: ${items.size}] category or title",
                        currentQuery = currentQuery,
                        enableCardViewPolicy = enableCardViewPolicy,
                        onMicrophoneClick = onMicrophoneClick,
                    ) { query ->
                        onQueryChange(query)
                    }
                }
            }
        }
    }
}
