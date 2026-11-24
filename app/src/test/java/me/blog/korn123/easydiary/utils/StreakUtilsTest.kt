package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.helper.DiaryEditingConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

/**
 * Unit tests for the writing streak of the dashboard (issue #88).
 *
 * The reporter asked for "Don't Break the Chain" and its looser form, the "Two Day Rule": the first ends a
 * chain of daily entries after one day without an entry, the second allows one skipped day and ends the chain
 * only when two days in a row pass without one. The tests pin the four states the card can show, the day
 * boundary at midnight, an empty history, and the difference the rule selection makes on the same entries.
 *
 * The heatmap squares carry the entries of their day so a tap on one opens what was written that day, and the
 * months above the columns name the month of the days below them; the tests pin what a square holds and where
 * a month is named.
 */
class StreakUtilsTest {
    private val today = LocalDate.of(2026, 11, 25)

    @Test
    fun `an entry of today counts today and the days before it`() {
        val summary = StreakUtils.calculate(diaries(today, today.minusDays(1), today.minusDays(2)), today)

        assertEquals(StreakStatus.WRITTEN_TODAY, summary.status)
        assertEquals(3, summary.currentStreak)
        assertEquals(3, summary.streakIfWrittenToday)
    }

    @Test
    fun `a chain whose last entry is yesterday still stands and asks for today`() {
        val summary = StreakUtils.calculate(diaries(today.minusDays(1), today.minusDays(2)), today)

        assertEquals(StreakStatus.DUE_TODAY, summary.status)
        assertEquals(2, summary.currentStreak)
        assertEquals(3, summary.streakIfWrittenToday)
    }

    @Test
    fun `one missed day breaks the daily chain`() {
        // The last entry is the day before yesterday, so yesterday was missed.
        val summary = StreakUtils.calculate(diaries(today.minusDays(2), today.minusDays(3)), today)

        assertEquals(StreakStatus.BROKEN, summary.status)
        assertEquals(0, summary.currentStreak)
        assertEquals(1, summary.streakIfWrittenToday)
    }

    @Test
    fun `the two day rule keeps a chain whose yesterday was skipped`() {
        val entries = diaries(today.minusDays(2), today.minusDays(3), today.minusDays(4))

        val daily = StreakUtils.calculate(entries, today)
        assertEquals(StreakStatus.BROKEN, daily.status)
        assertEquals(0, daily.currentStreak)

        val twoDay = StreakUtils.calculate(entries, today, isTwoDayRule = true)
        assertEquals(StreakStatus.AT_RISK_TWO_DAY, twoDay.status)
        // Three entries, and the skipped day the rule allowed, at once.
        assertEquals(3, twoDay.currentStreak)
        assertEquals(4, twoDay.streakIfWrittenToday)
        assertEquals(3, twoDay.longestStreak)
    }

    @Test
    fun `two missed days break the two day rule as well`() {
        val summary = StreakUtils.calculate(diaries(today.minusDays(3), today.minusDays(4)), today, isTwoDayRule = true)

        assertEquals(StreakStatus.BROKEN, summary.status)
        assertEquals(0, summary.currentStreak)
    }

    @Test
    fun `the two day rule keeps one chain across the day it allows to skip`() {
        // 21st, 22nd, then the 24th and the 25th: the 23rd is the one day the rule allows to skip.
        val entries = diaries(today.minusDays(4), today.minusDays(3), today.minusDays(1), today)

        assertEquals(2, StreakUtils.calculate(entries, today).currentStreak)
        val twoDay = StreakUtils.calculate(entries, today, isTwoDayRule = true)
        assertEquals(StreakStatus.WRITTEN_TODAY, twoDay.status)
        assertEquals(4, twoDay.currentStreak)
        assertEquals(4, twoDay.streakIfWrittenToday)
    }

    @Test
    fun `an empty history starts a new chain today`() {
        val summary = StreakUtils.calculate(emptyList(), today)

        assertEquals(StreakStatus.BROKEN, summary.status)
        assertEquals(0, summary.currentStreak)
        assertEquals(0, summary.longestStreak)
        assertEquals(1, summary.streakIfWrittenToday)
        assertTrue(summary.heatmap.flatten().all { it.count == 0 })
    }

    @Test
    fun `the longest chain is remembered after the chain broke`() {
        val entries =
            diaries(
                today.minusDays(20),
                today.minusDays(19),
                today.minusDays(18),
                today.minusDays(17),
                today.minusDays(10),
            )

        val summary = StreakUtils.calculate(entries, today)

        assertEquals(4, summary.longestStreak)
        assertEquals(StreakStatus.BROKEN, summary.status)
        assertEquals(0, summary.currentStreak)
    }

    @Test
    fun `the longest chain is counted the way the selected rule counts one`() {
        // 16th, 18th, 20th, then the 24th and the 25th.
        val entries = diaries(today.minusDays(9), today.minusDays(7), today.minusDays(5), today.minusDays(1), today)

        assertEquals(2, StreakUtils.calculate(entries, today).longestStreak)
        assertEquals(3, StreakUtils.calculate(entries, today, isTwoDayRule = true).longestStreak)
    }

    @Test
    fun `several entries of one day count as one day`() {
        val morning = diaryAt(today, hour = 8)
        val evening = diaryAt(today, hour = 21)
        val yesterday = diaryAt(today.minusDays(1))

        val summary = StreakUtils.calculate(listOf(morning, evening, yesterday), today)

        assertEquals(2, summary.currentStreak)
        assertEquals(2, summary.heatmap.flatten().first { it.date == today }.count)
    }

    @Test
    fun `an entry just after midnight belongs to the new day`() {
        val justAfterMidnight = diaryAt(today, hour = 0, minute = 0)
        val beforeMidnight = diaryAt(today.minusDays(1), hour = 23, minute = 59)

        val summary = StreakUtils.calculate(listOf(justAfterMidnight, beforeMidnight), today)

        assertEquals(StreakStatus.WRITTEN_TODAY, summary.status)
        assertEquals(2, summary.currentStreak)

        // Only the entry before midnight is left, so the chain waits for today.
        val yesterdayOnly = StreakUtils.calculate(listOf(beforeMidnight), today)
        assertEquals(StreakStatus.DUE_TODAY, yesterdayOnly.status)
        assertEquals(1, yesterdayOnly.currentStreak)
    }

    @Test
    fun `the day turning over takes the promise of yesterday away`() {
        val entries = diaries(today.minusDays(1), today.minusDays(2))

        assertEquals(StreakStatus.DUE_TODAY, StreakUtils.calculate(entries, today).status)
        assertEquals(StreakStatus.BROKEN, StreakUtils.calculate(entries, today.plusDays(1)).status)
        assertEquals(
            StreakStatus.AT_RISK_TWO_DAY,
            StreakUtils.calculate(entries, today.plusDays(1), isTwoDayRule = true).status,
        )
        assertEquals(
            StreakStatus.BROKEN,
            StreakUtils.calculate(entries, today.plusDays(2), isTwoDayRule = true).status,
        )
    }

    @Test
    fun `an entry dated after today does not extend the chain`() {
        val summary = StreakUtils.calculate(diaries(today.plusDays(1)), today)

        assertEquals(StreakStatus.BROKEN, summary.status)
        assertEquals(0, summary.currentStreak)
        assertEquals(0, summary.heatmap.flatten().first { it.date == today.plusDays(1) }.count)
    }

    @Test
    fun `an all day entry is counted on its stored date`() {
        // Written while the device was a day ahead, so the instant alone would fall on today.
        val allDay = diaryAt(today, isAllDay = true, dateString = today.minusDays(1).toString())
        val yesterday = diaryAt(today.minusDays(1))
        val twoDaysAgo = diaryAt(today.minusDays(2))

        val summary = StreakUtils.calculate(listOf(allDay, yesterday, twoDaysAgo), today)

        assertEquals(StreakStatus.DUE_TODAY, summary.status)
        assertEquals(2, summary.currentStreak)
        assertEquals(0, summary.heatmap.flatten().first { it.date == today }.count)
    }

    @Test
    fun `the heatmap ends with the week of today`() {
        val summary = StreakUtils.calculate(diaries(today), today)

        assertEquals(StreakUtils.HEATMAP_WEEKS, summary.heatmap.size)
        assertTrue(summary.heatmap.all { week -> week.size == 7 })

        val lastWeek = summary.heatmap.last()
        assertEquals(DayOfWeek.SUNDAY, lastWeek.first().date.dayOfWeek)
        assertTrue(lastWeek.first().date <= today && lastWeek.last().date >= today)
        assertEquals(1, lastWeek.first { it.date == today }.count)
        assertEquals(lastWeek.first().date.minusWeeks((StreakUtils.HEATMAP_WEEKS - 1).toLong()), summary.heatmap.first().first().date)
    }

    @Test
    fun `the heatmap leaves the days that have not come yet out`() {
        val summary = StreakUtils.calculate(diaries(today), today)
        val lastWeek = summary.heatmap.last()

        assertTrue(lastWeek.filter { it.date.isAfter(today) }.all { it.isFuture })
        assertFalse(lastWeek.any { !it.date.isAfter(today) && it.isFuture })
        assertTrue(lastWeek.filter { it.date.isAfter(today) }.all { it.count == 0 })
    }

    @Test
    fun `a square carries the entries of its day, newest first`() {
        val morning = diaryAt(today, hour = 8)
        val evening = diaryAt(today, hour = 21)

        val summary = StreakUtils.calculate(listOf(morning, evening, diaryAt(today.minusDays(1))), today)

        val day = summary.heatmap.flatten().first { it.date == today }
        assertEquals(2, day.count)
        assertEquals(listOf(evening.diaryId, morning.diaryId), day.diaries.map { it.diaryId })

        val empty = summary.heatmap.flatten().first { it.date == today.minusDays(3) }
        assertEquals(0, empty.count)
        assertTrue(empty.diaries.isEmpty())

        // A day that has not come yet carries nothing, so a tap on it leads to no entry.
        assertTrue(summary.heatmap.flatten().first { it.date == today.plusDays(2) }.diaries.isEmpty())
    }

    @Test
    fun `the draft of an entry being written is counted by neither the chain nor the square`() {
        val saved = diaryAt(today)
        val draft =
            saved.copy(
                diaryId = DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY,
                originDiaryId = saved.diaryId,
            )
        val draftOfAnEarlierDay =
            diaryAt(today.minusDays(1)).copy(
                diaryId = DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY,
                originDiaryId = diaryAt(today.minusDays(1)).diaryId,
            )

        val summary = StreakUtils.calculate(listOf(saved, draft, draftOfAnEarlierDay), today)

        assertEquals(1, summary.currentStreak)
        assertEquals(1, summary.heatmap.flatten().first { it.date == today }.count)
        assertEquals(0, summary.heatmap.flatten().first { it.date == today.minusDays(1) }.count)
    }

    @Test
    fun `a month is named on the first column and wherever it changes`() {
        val summary = StreakUtils.calculate(diaries(today), today)

        assertEquals(StreakUtils.HEATMAP_WEEKS, summary.heatmapMonths.size)
        assertEquals(
            listOf(
                0 to YearMonth.of(2026, 8),
                4 to YearMonth.of(2026, 9),
                8 to YearMonth.of(2026, 10),
                12 to YearMonth.of(2026, 11),
            ),
            summary.heatmapMonths.mapIndexedNotNull { index, month -> month?.let { index to it } },
        )
    }

    @Test
    fun `an empty history names the months of the grid as well`() {
        val summary = StreakUtils.calculate(emptyList(), today)

        assertEquals(4, summary.heatmapMonths.count { month -> month != null })
        assertEquals(YearMonth.of(2026, 8), summary.heatmapMonths.first())
        assertEquals(YearMonth.of(2026, 11), summary.heatmapMonths.last { month -> month != null })
    }

    @Test
    fun `a month is named in the short form of the device language`() {
        assertEquals("11월", StreakUtils.monthLabel(YearMonth.of(2026, 11), Locale.KOREAN))
        assertEquals("Nov", StreakUtils.monthLabel(YearMonth.of(2026, 11), Locale.ENGLISH))
    }

    @Test
    fun `the current chain never outgrows the best chain`() {
        val histories =
            listOf(
                diaries(today, today.minusDays(1), today.minusDays(2)),
                diaries(today.minusDays(1), today.minusDays(2)),
                diaries(today.minusDays(2), today.minusDays(3), today.minusDays(4)),
                diaries(today.minusDays(3), today.minusDays(4)),
                diaries(today.minusDays(30), today.minusDays(29), today),
            )

        histories.forEach { entries ->
            listOf(false, true).forEach { isTwoDayRule ->
                val summary = StreakUtils.calculate(entries, today, isTwoDayRule)

                assertTrue(summary.currentStreak <= summary.longestStreak)
                assertTrue(summary.streakIfWrittenToday >= summary.currentStreak)
            }
        }
    }

    private fun diaries(vararg dates: LocalDate): List<Diary> = dates.map { date -> diaryAt(date) }

    private fun diaryAt(
        date: LocalDate,
        hour: Int = 9,
        minute: Int = 0,
        isAllDay: Boolean = false,
        dateString: String? = null,
    ): Diary =
        Diary(
            diaryId = date.toEpochDay() * 100 + hour * 10 + minute / 10,
            currentTimeMillis =
                date
                    .atTime(hour, minute)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
            title = date.toString(),
            isAllDay = isAllDay,
            dateString = dateString,
        )
}
