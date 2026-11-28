package me.blog.korn123.easydiary.presentation.calendar

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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

        /**
         * The month `rememberCalendarState` starts from when it builds a new calendar state.
         *
         * The calendar state is keyed on the range it receives, so every change of [startMonth] or
         * [endMonth] builds a fresh state that begins at its first visible month. Pinning that month
         * to the one on screen keeps the user where they are when the range grows; the current month
         * would throw them back to today instead.
         *
         * It deliberately stays behind on ordinary swipes: following the visible month would rebuild
         * the state on every swipe and cut the scroll in progress. Only a range change or an explicit
         * jump moves it.
         */
        var anchorMonth by mutableStateOf(currentMonth)
            private set
        var selection by mutableStateOf<LocalDate?>(LocalDate.now())
            private set
        var eventsMap by mutableStateOf<Map<LocalDate, List<Diary>>>(emptyMap())
            private set
        var isLoading by mutableStateOf(false)
        var pendingScrollMonth by mutableStateOf<YearMonth?>(null)
            private set

        private var currentObservingMonth: YearMonth? = null
        private var observeEventsJob: Job? = null

        fun setSelectedDate(date: LocalDate?) {
            selection = date
        }

        /**
         * Requests a jump to [targetMonth] and widens the calendar range when the month sits
         * outside it, so that the state can actually scroll there.
         *
         * The margins mirror [observeEventsForMonth]: a month that lands within three months of
         * either edge would expand the range again as soon as it becomes visible, and a range
         * change rebuilds the calendar state. Widening the range up front keeps the landing month
         * away from both edges, so the jump is not followed by a rebuild.
         */
        fun jumpToMonth(targetMonth: YearMonth) {
            if (!targetMonth.isAfter(startMonth.plusMonths(3))) {
                startMonth = minOf(startMonth, targetMonth.minusYears(2))
            }
            if (!targetMonth.isBefore(endMonth.minusMonths(3))) {
                endMonth = maxOf(endMonth, targetMonth.plusYears(2))
            }
            // A rebuilt state must land on the requested month, not on the month it was anchored to
            // before the jump.
            anchorMonth = targetMonth
            pendingScrollMonth = targetMonth
        }

        fun clearPendingScrollMonth() {
            pendingScrollMonth = null
        }

        fun observeEventsForMonth(visibleMonth: YearMonth) {
            if (currentObservingMonth == visibleMonth) return
            currentObservingMonth = visibleMonth

            var rangeChanged = false
            if (!visibleMonth.isAfter(startMonth.plusMonths(3))) {
                startMonth = startMonth.minusYears(2)
                rangeChanged = true
            }
            if (!visibleMonth.isBefore(endMonth.minusMonths(3))) {
                endMonth = endMonth.plusYears(2)
                rangeChanged = true
            }
            // Widening the range rebuilds the calendar state, which then starts at [anchorMonth].
            // Anchor it to the month being browsed so the rebuild does not move the calendar.
            if (rangeChanged) anchorMonth = visibleMonth

            // A collected Room flow never completes on its own, so the collector of the previous
            // month has to be cancelled before the next one starts; otherwise every month browsed
            // adds a live collector that keeps overwriting eventsMap with its own range.
            observeEventsJob?.cancel()
            isLoading = true
            val targetMonth = visibleMonth
            val startOfMonth = targetMonth.atDay(1)
            val startDate = startOfMonth.minusWeeks(7)
            val endDate = startOfMonth.plusWeeks(7)
            val sortAsc = getApplication<Application>().config.calendarSorting == CALENDAR_SORTING_ASC

            val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 86400000L

            observeEventsJob =
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
                    } catch (e: CancellationException) {
                        // The next month is already subscribed and owns isLoading now.
                        throw e
                    } catch (_: Exception) {
                        isLoading = false
                    }
                }
        }
    }
