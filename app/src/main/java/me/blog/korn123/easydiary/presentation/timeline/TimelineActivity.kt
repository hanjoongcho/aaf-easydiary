package me.blog.korn123.easydiary.presentation.timeline

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.PREVIOUS_ACTIVITY_CREATE
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.SELECTED_SYMBOL_SEQUENCE
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.presentation.timeline.components.TimelineScreen

/**
 * Created by hanjoong on 2017-07-16.
 * Migrated to Jetpack Compose.
 */
@AndroidEntryPoint
class TimelineActivity : EasyDiaryComposeBaseActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private val timelineViewModel: TimelineViewModel by viewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timelineViewModel.restoreState(savedInstanceState)

        setContent {
            AppTheme {
                val startDateText by timelineViewModel.startDateText.collectAsStateWithLifecycle()
                val endDateText by timelineViewModel.endDateText.collectAsStateWithLifecycle()
                val queryText by timelineViewModel.queryText.collectAsStateWithLifecycle()
                val symbolSequence by timelineViewModel.symbolSequence.collectAsStateWithLifecycle()
                val isFilterVisible by timelineViewModel.isFilterVisible.collectAsStateWithLifecycle()
                val diaryList by timelineViewModel.diaryList.collectAsStateWithLifecycle()
                val isLoading by timelineViewModel.isLoading.collectAsStateWithLifecycle()

                TimelineScreen(
                    diaryList = diaryList,
                    isContainerLoading = timelineViewModel.isContainerLoading,
                    isLoading = isLoading,
                    loadingMessage = diaryViewModel.loadingMessage ?: "Initializing...",
                    isFilterVisible = isFilterVisible,
                    startDateText = startDateText,
                    endDateText = endDateText,
                    queryText = queryText,
                    symbolSequence = symbolSequence,
                    onToggleFilter = { timelineViewModel.toggleFilterVisible() },
                    onCloseFilter = { timelineViewModel.closeFilter() },
                    onStartDateClick = { showStartDatePicker() },
                    onEndDateClick = { showEndDatePicker() },
                    onQueryChange = { query -> timelineViewModel.setQueryText(query) },
                    onSymbolClick = { showSymbolDialog() },
                    onClearFilter = { timelineViewModel.clearFilter() },
                    onDiaryClick = { diary -> openDiaryReading(diary) },
                    onWriteDiaryClick = { openDiaryWriting() },
                    onBackClick = { finishActivityWithPauseLock() },
                )
            }
        }

        if (savedInstanceState == null) {
            timelineViewModel.startRefreshJob()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        timelineViewModel.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (config.previousActivity == PREVIOUS_ACTIVITY_CREATE) {
            timelineViewModel.startRefreshJob()
            config.previousActivity = -1
        } else {
            timelineViewModel.startRefreshJob()
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun showStartDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                timelineViewModel.setStartDate(year, month, dayOfMonth)
            },
            timelineViewModel.startYear,
            timelineViewModel.startMonth,
            timelineViewModel.startDay,
        ).show()
    }

    private fun showEndDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                timelineViewModel.setEndDate(year, month, dayOfMonth)
            },
            timelineViewModel.endYear,
            timelineViewModel.endMonth,
            timelineViewModel.endDay,
        ).show()
    }

    private fun showSymbolDialog() {
        lifecycleScope.launch {
            openFeelingSymbolDialog(
                getString(R.string.diary_symbol_search_message),
                selectedSymbolSequence = 0,
                diaryViewModel.getSymbolUsedCountMap(true),
            ) { symbolSequence ->
                timelineViewModel.setSymbolSequence(if (symbolSequence == 0) SYMBOL_SELECT_ALL else symbolSequence)
            }
        }
    }

    private fun openDiaryReading(diary: DiaryDomain) {
        val detailIntent =
            Intent(this, DiaryReadingActivity::class.java).apply {
                putExtra(DIARY_SEQUENCE, diary.diaryId)
                putExtra(SELECTED_SEARCH_QUERY, timelineViewModel.queryText.value)
                putExtra(SELECTED_SYMBOL_SEQUENCE, timelineViewModel.symbolSequence.value)
            }
        TransitionHelper.startActivityWithTransition(this, detailIntent)
    }

    private fun openDiaryWriting() {
        val createDiary = Intent(this, DiaryWritingActivity::class.java)
        TransitionHelper.startActivityWithTransition(this, createDiary)
    }
}
