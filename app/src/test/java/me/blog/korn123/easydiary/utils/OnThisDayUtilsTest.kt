package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.domain.model.Diary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit tests for the 'On This Day' section of the dashboard (issue #140).
 *
 * The reporter's mother keeps a paper diary with ten spaces for different years on every day page, and
 * asked for the same view: what was written on this day in the years before. The section keeps the
 * diaries of the same month and day in earlier years, the nearest past year first, and shows nothing on
 * a day that has no such diary.
 */
class OnThisDayUtilsTest {
    private val today = LocalDate.of(2026, 11, 22)

    @Test
    fun `keeps the same day of earlier years only`() {
        val thisYear = diaryAt(LocalDate.of(2026, 11, 22))
        val lastYear = diaryAt(LocalDate.of(2025, 11, 22))
        val twoThousandNineteen = diaryAt(LocalDate.of(2019, 11, 22))
        val previousDay = diaryAt(LocalDate.of(2025, 11, 21))
        val otherMonth = diaryAt(LocalDate.of(2025, 10, 22))

        val result =
            OnThisDayUtils.diariesOfPastYearsOn(
                listOf(thisYear, previousDay, lastYear, otherMonth, twoThousandNineteen),
                today,
            )

        assertEquals(listOf(lastYear, twoThousandNineteen), result)
    }

    @Test
    fun `orders the nearest past year first`() {
        val diaries =
            listOf(
                diaryAt(LocalDate.of(2019, 11, 22)),
                diaryAt(LocalDate.of(2025, 11, 22)),
                diaryAt(LocalDate.of(2021, 11, 22)),
                diaryAt(LocalDate.of(2004, 11, 22)),
            )

        val years = OnThisDayUtils.diariesOfPastYearsOn(diaries, today).map { yearOf(it) }

        assertEquals(listOf(2025, 2021, 2019, 2004), years)
    }

    @Test
    fun `a day without an earlier diary is empty`() {
        val diaries = listOf(diaryAt(LocalDate.of(2026, 11, 22)), diaryAt(LocalDate.of(2026, 11, 21)))

        assertTrue(OnThisDayUtils.diariesOfPastYearsOn(diaries, today).isEmpty())
    }

    @Test
    fun `an all day diary is matched by its stored date`() {
        // Written while the device was a day ahead, so the instant alone would fall on 23 November.
        val allDay = diaryAt(LocalDate.of(2018, 11, 23), isAllDay = true, dateString = "2018-11-22")

        assertEquals(listOf(allDay), OnThisDayUtils.diariesOfPastYearsOn(listOf(allDay), today))
    }

    @Test
    fun `an all day diary without a readable date falls back to its written moment`() {
        val noDateString = diaryAt(LocalDate.of(2018, 11, 22), isAllDay = true, dateString = null)
        val unreadable = diaryAt(LocalDate.of(2018, 11, 22), isAllDay = true, dateString = "")
        val otherDay = diaryAt(LocalDate.of(2018, 11, 25), isAllDay = true, dateString = "not a date")

        val result = OnThisDayUtils.diariesOfPastYearsOn(listOf(noDateString, unreadable, otherDay), today)

        assertEquals(listOf(noDateString, unreadable), result)
    }

    @Test
    fun `a diary that is not all day is matched by its written moment`() {
        // The stored date string of a timed diary is derived from the moment, so the moment is used.
        val timed = diaryAt(LocalDate.of(2019, 11, 22), isAllDay = false, dateString = "2019-01-01")

        assertEquals(listOf(timed), OnThisDayUtils.diariesOfPastYearsOn(listOf(timed), today))
    }

    @Test
    fun `the leap day is only on the leap day`() {
        val onLeapDay = LocalDate.of(2024, 2, 29)

        // 'Today' is 29 February 2028, the next leap day, so 29 February 2024 belongs there.
        assertEquals(
            listOf(diaryAt(onLeapDay)),
            OnThisDayUtils.diariesOfPastYearsOn(listOf(diaryAt(onLeapDay)), LocalDate.of(2028, 2, 29)),
        )

        // 'Today' is 28 February 2029, so a diary of 29 February 2024 is not the same day.
        assertTrue(
            OnThisDayUtils
                .diariesOfPastYearsOn(listOf(diaryAt(onLeapDay)), LocalDate.of(2029, 2, 28))
                .isEmpty(),
        )
    }

    @Test
    fun `two diaries of the same day are both kept`() {
        val morning = diaryAt(LocalDate.of(2023, 11, 22), hour = 8)
        val evening = diaryAt(LocalDate.of(2023, 11, 22), hour = 21)
        val nextYear = diaryAt(LocalDate.of(2024, 11, 22))

        val result = OnThisDayUtils.diariesOfPastYearsOn(listOf(morning, nextYear, evening), today)

        assertEquals(listOf(nextYear, evening, morning), result)
    }

    @Test
    fun `today comes from the given date`() {
        val diary = diaryAt(LocalDate.of(2025, 3, 7))

        assertEquals(listOf(diary), OnThisDayUtils.diariesOfPastYearsOn(listOf(diary), LocalDate.of(2026, 3, 7)))
        assertTrue(OnThisDayUtils.diariesOfPastYearsOn(listOf(diary), LocalDate.of(2026, 3, 8)).isEmpty())
    }

    private fun diaryAt(
        date: LocalDate,
        isAllDay: Boolean = false,
        dateString: String? = null,
        hour: Int = 9,
    ): Diary =
        Diary(
            diaryId = date.toEpochDay() * 10 + hour,
            currentTimeMillis =
                date
                    .atTime(hour, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
            title = date.toString(),
            isAllDay = isAllDay,
            dateString = dateString,
        )

    private fun yearOf(diary: Diary): Int =
        Instant
            .ofEpochMilli(diary.currentTimeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .year
}
