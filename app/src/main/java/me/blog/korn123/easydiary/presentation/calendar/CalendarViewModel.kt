package me.blog.korn123.easydiary.presentation.calendar

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel
    @Inject
    constructor(
        application: Application,
        private val diaryRepository: DiaryRepository,
    ) : AndroidViewModel(application) {
        val currentMonth: YearMonth = YearMonth.now()
        var startMonth by mutableStateOf(currentMonth.minusYears(2))
            private set
        var endMonth by mutableStateOf(currentMonth.plusYears(2))
            private set
        var selection by mutableStateOf<LocalDate?>(LocalDate.now())
            private set
        var eventsMap by mutableStateOf<Map<LocalDate, List<Diary>>>(emptyMap())
            private set
        var isLoading by mutableStateOf(false)

        private var currentObservingMonth: YearMonth? = null

        fun setSelectedDate(date: LocalDate?) {
            selection = date
        }

        fun observeEventsForMonth(visibleMonth: YearMonth) {
            if (currentObservingMonth == visibleMonth) return
            currentObservingMonth = visibleMonth

            if (!visibleMonth.isAfter(startMonth.plusMonths(3))) {
                startMonth = startMonth.minusYears(2)
            }
            if (!visibleMonth.isBefore(endMonth.minusMonths(3))) {
                endMonth = endMonth.plusYears(2)
            }

            isLoading = true
            val targetMonth = visibleMonth
            val startOfMonth = targetMonth.atDay(1)
            val startDate = startOfMonth.minusWeeks(7)
            val endDate = startOfMonth.plusWeeks(7)
            val sortAsc = getApplication<Application>().config.calendarSorting == CALENDAR_SORTING_ASC

            val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 86400000L

            viewModelScope.launch {
                try {
                    diaryRepository
                        .observeDiariesWithPhotos(
                            startTimeMillis = startMillis,
                            endTimeMillis = endMillis,
                        ).map { allDiariesInRange ->
                            val groupedMap =
                                if (sortAsc) {
                                    allDiariesInRange.sortedBy { it.currentTimeMillis }
                                } else {
                                    allDiariesInRange.sortedByDescending { it.currentTimeMillis }
                                }.groupBy { it.dateString ?: "" }

                            val resultMap = mutableMapOf<LocalDate, List<Diary>>()
                            for ((dateStr, diaries) in groupedMap) {
                                if (diaries.isNotEmpty()) {
                                    try {
                                        val localDate = LocalDate.parse(dateStr)
                                        resultMap[localDate] = diaries
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                            resultMap
                        }.collectLatest { newMap ->
                            eventsMap = newMap
                            isLoading = false
                        }
                } catch (_: Exception) {
                    isLoading = false
                }
            }
        }
    }
