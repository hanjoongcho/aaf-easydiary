package me.blog.korn123.easydiary.presentation.timeline

import android.app.Application
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.extensions.applicationScope
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.FILTER_END_DATE
import me.blog.korn123.easydiary.helper.FILTER_END_ENABLE
import me.blog.korn123.easydiary.helper.FILTER_END_MONTH
import me.blog.korn123.easydiary.helper.FILTER_END_YEAR
import me.blog.korn123.easydiary.helper.FILTER_QUERY
import me.blog.korn123.easydiary.helper.FILTER_START_DATE
import me.blog.korn123.easydiary.helper.FILTER_START_ENABLE
import me.blog.korn123.easydiary.helper.FILTER_START_MONTH
import me.blog.korn123.easydiary.helper.FILTER_START_YEAR
import me.blog.korn123.easydiary.helper.FILTER_VIEW_VISIBLE
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.viewmodels.DiaryViewModel
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class TimelineViewModel
    @Inject
    constructor(
        application: Application,
        private val diaryRepository: DiaryRepository,
    ) : AndroidViewModel(application) {
        private val calendar: Calendar = Calendar.getInstance(Locale.getDefault())

        var startYear: Int = calendar.get(Calendar.YEAR)
            private set
        var startMonth: Int = calendar.get(Calendar.MONTH)
            private set
        var startDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
            private set

        var endYear: Int = calendar.get(Calendar.YEAR)
            private set
        var endMonth: Int = calendar.get(Calendar.MONTH)
            private set
        var endDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
            private set

        private val _startDateText = MutableStateFlow("")
        val startDateText: StateFlow<String> = _startDateText.asStateFlow()

        private val _endDateText = MutableStateFlow("")
        val endDateText: StateFlow<String> = _endDateText.asStateFlow()

        private val _queryText = MutableStateFlow("")
        val queryText: StateFlow<String> = _queryText.asStateFlow()

        private val _symbolSequence = MutableStateFlow(SYMBOL_SELECT_ALL)
        val symbolSequence: StateFlow<Int> = _symbolSequence.asStateFlow()

        private val _isFilterVisible = MutableStateFlow(false)
        val isFilterVisible: StateFlow<Boolean> = _isFilterVisible.asStateFlow()

        private val _diaryList = MutableStateFlow<List<Diary>>(emptyList())
        val diaryList: StateFlow<List<Diary>> = _diaryList.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        var targetDiaryId: Long = -1L
            private set

        var isContainerLoading by mutableStateOf(true)

        private var refreshJob: Job? = null

        fun setTargetDiaryId(diaryId: Long) {
            targetDiaryId = diaryId
        }

        fun restoreState(savedInstanceState: Bundle?) {
            savedInstanceState?.let { bundle ->
                startYear = bundle.getInt(FILTER_START_YEAR, calendar.get(Calendar.YEAR))
                startMonth = bundle.getInt(FILTER_START_MONTH, calendar.get(Calendar.MONTH))
                startDay = bundle.getInt(FILTER_START_DATE, calendar.get(Calendar.DAY_OF_MONTH))
                if (bundle.getBoolean(FILTER_START_ENABLE, false)) {
                    val startMillis = EasyDiaryUtils.datePickerToTimeMillis(startDay, startMonth, startYear)
                    _startDateText.value = DateUtils.getDateStringFromTimeMillis(startMillis)
                }

                endYear = bundle.getInt(FILTER_END_YEAR, calendar.get(Calendar.YEAR))
                endMonth = bundle.getInt(FILTER_END_MONTH, calendar.get(Calendar.MONTH))
                endDay = bundle.getInt(FILTER_END_DATE, calendar.get(Calendar.DAY_OF_MONTH))
                if (bundle.getBoolean(FILTER_END_ENABLE, false)) {
                    val endMillis = EasyDiaryUtils.datePickerToTimeMillis(endDay, endMonth, endYear)
                    _endDateText.value = DateUtils.getDateStringFromTimeMillis(endMillis)
                }

                if (bundle.getBoolean(FILTER_VIEW_VISIBLE, false)) {
                    _isFilterVisible.value = true
                }

                _queryText.value = bundle.getString(FILTER_QUERY, "") ?: ""
                targetDiaryId = bundle.getLong(DIARY_SEQUENCE, -1L)
            }
        }

        fun saveState(outState: Bundle) {
            if (_diaryList.value.isNotEmpty()) {
                outState.putLong(DIARY_SEQUENCE, targetDiaryId)
            }

            if (_startDateText.value.isNotEmpty()) {
                outState.putBoolean(FILTER_START_ENABLE, true)
                outState.putInt(FILTER_START_YEAR, startYear)
                outState.putInt(FILTER_START_MONTH, startMonth)
                outState.putInt(FILTER_START_DATE, startDay)
            }

            if (_endDateText.value.isNotEmpty()) {
                outState.putBoolean(FILTER_END_ENABLE, true)
                outState.putInt(FILTER_END_YEAR, endYear)
                outState.putInt(FILTER_END_MONTH, endMonth)
                outState.putInt(FILTER_END_DATE, endDay)
            }

            if (_isFilterVisible.value) {
                outState.putBoolean(FILTER_VIEW_VISIBLE, true)
            }

            outState.putString(FILTER_QUERY, _queryText.value)
        }

        fun toggleFilterVisible() {
            _isFilterVisible.value = !_isFilterVisible.value
        }

        fun closeFilter() {
            _isFilterVisible.value = false
        }

        fun setStartDate(
            year: Int,
            month: Int,
            dayOfMonth: Int,
        ) {
            startYear = year
            startMonth = month
            startDay = dayOfMonth
            val startMillis = EasyDiaryUtils.datePickerToTimeMillis(startDay, startMonth, startYear)
            _startDateText.value = DateUtils.getDateStringFromTimeMillis(startMillis)
            startRefreshJob()
        }

        fun setEndDate(
            year: Int,
            month: Int,
            dayOfMonth: Int,
        ) {
            endYear = year
            endMonth = month
            endDay = dayOfMonth
            val endMillis = EasyDiaryUtils.datePickerToTimeMillis(endDay, endMonth, endYear)
            _endDateText.value = DateUtils.getDateStringFromTimeMillis(endMillis)
            startRefreshJob()
        }

        fun setQueryText(
            query: String,
            debounce: Long = 300L,
        ) {
            _queryText.value = query
            startRefreshJob(debounce)
        }

        fun setSymbolSequence(symbolSeq: Int) {
            _symbolSequence.value = symbolSeq
            startRefreshJob()
        }

        fun clearFilter() {
            _startDateText.value = ""
            _endDateText.value = ""
            _queryText.value = ""
            _symbolSequence.value = SYMBOL_SELECT_ALL
            startYear = calendar.get(Calendar.YEAR)
            startMonth = calendar.get(Calendar.MONTH)
            startDay = calendar.get(Calendar.DAY_OF_MONTH)
            endYear = calendar.get(Calendar.YEAR)
            endMonth = calendar.get(Calendar.MONTH)
            endDay = calendar.get(Calendar.DAY_OF_MONTH)
            startRefreshJob()
        }

        fun startRefreshJob(debounce: Long = 0L) {
            refreshJob?.cancel()
            refreshJob =
                viewModelScope.launch {
                    if (debounce > 0) delay(debounce)
                    refreshList()
                }
        }

        private suspend fun refreshList() {
            _isLoading.value = true
            var startMillis = 0L
            var endMillis = 0L

            if (_startDateText.value.isNotEmpty()) {
                startMillis = EasyDiaryUtils.datePickerToTimeMillis(startDay, startMonth, startYear)
            }
            if (_endDateText.value.isNotEmpty()) {
                endMillis = EasyDiaryUtils.datePickerToTimeMillis(endDay, endMonth, endYear, true)
            }

            val results =
                findDiary(
                    _queryText.value,
                    getApplication<Application>().config.diarySearchQueryCaseSensitive,
                    startMillis,
                    endMillis,
                    _symbolSequence.value,
                    true,
                )

            _diaryList.value = results.reversed()
            _isLoading.value = false

            application.applicationScope.launch {
                delay(200L.milliseconds)
                isContainerLoading = false
            }
        }

        suspend fun findDiary(
            query: String?,
            isSensitive: Boolean = false,
            startTimeMillis: Long = 0,
            endTimeMillis: Long = 0,
            symbolSequence: Int = 0,
            checkFutureDiaryOption: Boolean = false,
        ): List<Diary> =
            EasyDiaryUtils.resolveDiaryFilter(
                diaryRepository
                    .getDiariesWithPhotos(
                        query = query,
                        isSensitive = isSensitive,
                        startTimeMillis = startTimeMillis,
                        endTimeMillis = endTimeMillis,
                        symbolSequence = symbolSequence,
                    ),
                application,
                startTimeMillis,
                endTimeMillis,
                symbolSequence,
                checkFutureDiaryOption,
            )
    }
