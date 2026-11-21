package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.dateAnchorTimeMillis
import me.blog.korn123.easydiary.helper.DateUtilConstants
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

/**
 * Unit tests for the date of an 'All day' diary across a device time zone change (issue #214).
 *
 * An all day diary stores the day in `dateString` and the moment it was written at (00:00 of that day in
 * the time zone the device was in then) in `currentTimeMillis`. A flight moves the device to another time
 * zone, which moves that moment to another day: the reporter's entries of 27 May 2023 and 1 January 2023
 * were shown as 26 May 2023 and 31 December 2022 after flying from New Zealand to Nepal and from Nepal to
 * South Africa. The date has to survive that, both on screen and in the D-Day badge.
 */
class DiaryAllDayTimeZoneTest {
    // Fixed offsets instead of named zones: the tests must not depend on daylight saving rules.
    private val utcPlus12 = "GMT+12:00"
    private val utcPlus2 = "GMT+02:00"

    @Test
    fun `an all day diary keeps its date after the device time zone changes`() {
        val diary = allDayDiary(dateString = "2023-05-27", writtenIn = utcPlus12)

        // The instant the entry stores is still the previous day in UTC+2, which is what the app showed
        // before the fix.
        withDefaultTimeZone(utcPlus2) {
            assertEquals("2023-05-26", dateOf(diary.currentTimeMillis))
            assertEquals("2023-05-27", dateOf(diary.dateAnchorTimeMillis()))
        }
    }

    @Test
    fun `the D-Day badge of an all day diary does not shift with the time zone`() {
        withDefaultTimeZone(utcPlus2) {
            // 'Today' is taken in the time zone the diary is read in, so the expected day count cannot
            // depend on where the test machine's own time zone is around midnight.
            val threeDaysAgo = LocalDate.now().minusDays(3)
            val diary = allDayDiary(dateString = threeDaysAgo.toString(), writtenIn = utcPlus12)

            assertEquals("D＋3", DateUtils.getOnlyDayRemaining(diary.dateAnchorTimeMillis()))
        }
    }

    @Test
    fun `a diary that is not all day keeps its own instant`() {
        val diary = allDayDiary(dateString = "2023-05-27", writtenIn = utcPlus12).copy(isAllDay = false)

        withDefaultTimeZone(utcPlus2) {
            assertEquals(diary.currentTimeMillis, diary.dateAnchorTimeMillis())
        }
    }

    @Test
    fun `an all day diary without a usable date string falls back to the stored instant`() {
        withDefaultTimeZone(utcPlus2) {
            listOf(null, "", "not a date").forEach { dateString ->
                val diary = allDayDiary(dateString = dateString, writtenIn = utcPlus12)
                assertEquals(diary.currentTimeMillis, diary.dateAnchorTimeMillis())
            }
        }
    }

    @Test
    fun `saving an all day diary again keeps the date it was stored with`() {
        val diary = allDayDiary(dateString = "2023-05-27", writtenIn = utcPlus12)

        withDefaultTimeZone(utcPlus2) {
            // Writing the entry again (a link to another entry, encryption, a backup restore) must not
            // rewrite the stored date from the instant the time zone change moved to the previous day.
            assertEquals("2023-05-27", diary.toEntity().dateString)

            // A diary that brings no date of its own (a fresh entry, a draft, a save from the editor)
            // still takes the date of the instant it stores.
            assertEquals("2023-05-26", diary.copy(dateString = null).toEntity().dateString)
        }
    }

    @Test
    fun `a diary that is not all day takes the date of its instant`() {
        val diary = allDayDiary(dateString = "2023-05-27", writtenIn = utcPlus12).copy(isAllDay = false)

        withDefaultTimeZone(utcPlus2) {
            assertEquals("2023-05-26", diary.toEntity().dateString)
        }
    }

    /** An all day diary of [dateString], written at 00:00 of that day in [writtenIn]. */
    private fun allDayDiary(
        dateString: String?,
        writtenIn: String,
    ) = Diary(
        currentTimeMillis =
            LocalDate
                .parse("2023-05-27")
                .atStartOfDay(ZoneId.of(writtenIn))
                .toInstant()
                .toEpochMilli(),
        dateString = dateString,
        isAllDay = true,
    )

    private fun dateOf(timeMillis: Long): String = DateUtils.timeMillisToDate(timeMillis, DateUtilConstants.DATE_PATTERN_DASH)

    private fun <T> withDefaultTimeZone(
        zoneId: String,
        block: () -> T,
    ): T {
        val original = TimeZone.getDefault()
        return try {
            TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
            block()
        } finally {
            TimeZone.setDefault(original)
        }
    }
}
