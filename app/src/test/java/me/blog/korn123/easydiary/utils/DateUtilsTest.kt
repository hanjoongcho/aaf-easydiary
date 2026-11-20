package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.enums.TimeFormatMode
import me.blog.korn123.easydiary.helper.DateUtilConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

/**
 * Modernized unit test suite for DateUtils.
 * Created by CHO HANJOONG on 2018-03-04.
 * Updated with modern Kotlin test naming and comprehensive method coverage.
 */
class DateUtilsTest {

    private val timeMillis: Long = 1520149040913 // 2018-03-04 16:37:20.913 (Asia/Seoul)

    @Test
    fun `timeMillisToDate with dash pattern and explicit zone returns expected date string`() {
        val result = DateUtils.timeMillisToDate(
            timeMillis = timeMillis,
            pattern = DateUtilConstants.DATE_PATTERN_DASH,
            zoneId = ZoneId.of("Asia/Seoul")
        )
        assertEquals("2018-03-04", result)
    }

    @Test
    fun `timeMillisToDate with hour pattern returns expected hour string`() {
        val result = DateUtils.timeMillisToDate(
            timeMillis = timeMillis,
            pattern = "HH",
            zoneId = ZoneId.of("Asia/Seoul")
        )
        assertEquals("16", result)
    }

    @Test
    fun `timeMillisToDateTime formats timestamp with custom pattern correctly`() {
        val result = DateUtils.timeMillisToDateTime(
            timeMillis = timeMillis,
            pattern = "yyyy-MM-dd HH:mm:ss"
        )
        assertNotNull(result)
        assertTrue(result.startsWith("2018-03-04"))
    }

    @Test
    fun `getDateStringFromTimeMillis with full format returns expected localized date string`() {
        val result = DateUtils.getDateStringFromTimeMillis(
            timeMillis = timeMillis,
            dateFormat = SimpleDateFormat.FULL,
            locale = Locale.KOREA
        )
        assertNotNull(result)
    }

    @Test
    fun `getTimeStringFromTimeMillis returns expected time string`() {
        val result = DateUtils.getTimeStringFromTimeMillis(
            timeMillis = timeMillis,
            timeFormat = SimpleDateFormat.SHORT,
            locale = Locale.KOREA
        )
        assertNotNull(result)
    }

    @Test
    fun `getDateTimeStringFromTimeMillis with default and specific locale formats correctly`() {
        val defaultResult = DateUtils.getDateTimeStringFromTimeMillis(timeMillis)
        assertNotNull(defaultResult)

        val franceResult = DateUtils.getDateTimeStringFromTimeMillis(
            timeMillis = timeMillis,
            dateFormat = SimpleDateFormat.FULL,
            timeFormat = SimpleDateFormat.FULL,
            dateTimeFormat = null,
            locale = Locale.FRANCE
        )
        assertNotNull(franceResult)
        assertTrue(franceResult.contains("2018"))
        assertTrue(franceResult.contains("mars"))
    }

    @Test
    fun `dateStringToTimeStamp parses date string to epoch millis correctly`() {
        val timestamp = DateUtils.dateStringToTimeStamp("2018-03-04")
        assertNotNull(timestamp)
        assertTrue(timestamp > 0L)
    }

    @Test
    fun `dateToTimeMillis returns end of day millis for given date string and zone`() {
        val endOfDayMillis = DateUtils.dateToTimeMillis(
            dateString = "2018-03-04",
            pattern = "yyyy-MM-dd",
            zoneId = ZoneId.of("Asia/Seoul")
        )
        assertNotNull(endOfDayMillis)
        assertTrue(endOfDayMillis > timeMillis)
    }

    @Test
    fun `getOnlyDayRemaining returns correct remaining day format`() {
        val targetMillis = timeMillis + (1000L * 60 * 60 * 24 * 5) // 5 days later
        val remaining = DateUtils.getOnlyDayRemaining(targetMillis, onlyDays = true)
        assertNotNull(remaining)
        assertTrue(remaining.startsWith("D"))
    }

    @Test
    fun `getCurrentDateTime returns non empty formatted string`() {
        val current = DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss")
        assertNotNull(current)
        assertEquals(15, current.length)
    }

    // ------------------------------------------------------------------
    // Elapsed days (issue #248)
    // ------------------------------------------------------------------

    private val seoul: ZoneId = ZoneId.of("Asia/Seoul")

    private fun millis(
        dateTime: LocalDateTime,
        zoneId: ZoneId = seoul,
    ): Long = dateTime.atZone(zoneId).toInstant().toEpochMilli()

    @Test
    fun `getElapsedDays returns 0 for an entry written earlier the same day`() {
        val now = millis(LocalDateTime.of(2026, 11, 20, 18, 0))
        val writtenAt = millis(LocalDateTime.of(2026, 11, 20, 0, 5))
        assertEquals(0L, DateUtils.getElapsedDays(writtenAt, now, seoul))
    }

    @Test
    fun `getElapsedDays counts calendar days instead of 24 hour periods`() {
        // 23:50 yesterday is already `1 day ago` at 00:10 today, although only 20 minutes passed.
        val now = millis(LocalDateTime.of(2026, 11, 20, 0, 10))
        val writtenAt = millis(LocalDateTime.of(2026, 11, 19, 23, 50))
        assertEquals(1L, DateUtils.getElapsedDays(writtenAt, now, seoul))
    }

    @Test
    fun `getElapsedDays returns 0 for a long day that does not cross midnight`() {
        // 23 hours on the same date is still 0 days.
        val now = millis(LocalDateTime.of(2026, 11, 20, 23, 0))
        val writtenAt = millis(LocalDateTime.of(2026, 11, 20, 0, 0))
        assertEquals(0L, DateUtils.getElapsedDays(writtenAt, now, seoul))
    }

    @Test
    fun `getElapsedDays returns several elapsed days`() {
        val now = millis(LocalDateTime.of(2026, 11, 20, 9, 0))
        val writtenAt = millis(LocalDateTime.of(2026, 11, 13, 21, 30))
        assertEquals(7L, DateUtils.getElapsedDays(writtenAt, now, seoul))
    }

    @Test
    fun `getElapsedDays crosses a month boundary`() {
        val now = millis(LocalDateTime.of(2026, 3, 1, 8, 0))
        val writtenAt = millis(LocalDateTime.of(2026, 2, 28, 22, 0))
        assertEquals(1L, DateUtils.getElapsedDays(writtenAt, now, seoul))
    }

    @Test
    fun `getElapsedDays counts the leap day of a leap year`() {
        val now = millis(LocalDateTime.of(2024, 3, 1, 0, 30))
        val writtenAt = millis(LocalDateTime.of(2024, 2, 28, 23, 30))
        assertEquals(2L, DateUtils.getElapsedDays(writtenAt, now, seoul))
    }

    @Test
    fun `getElapsedDays crosses a daylight saving time change`() {
        // US daylight saving time starts on 2024-03-10, so this range is only 47 hours but 2 dates.
        val newYork = ZoneId.of("America/New_York")
        val now = millis(LocalDateTime.of(2024, 3, 11, 0, 30), newYork)
        val writtenAt = millis(LocalDateTime.of(2024, 3, 9, 23, 30), newYork)
        assertEquals(2L, DateUtils.getElapsedDays(writtenAt, now, newYork))
    }

    @Test
    fun `getElapsedDays returns a negative value for a future diary`() {
        val now = millis(LocalDateTime.of(2026, 11, 20, 12, 0))
        val writtenAt = millis(LocalDateTime.of(2026, 11, 22, 9, 0))
        assertEquals(-2L, DateUtils.getElapsedDays(writtenAt, now, seoul))
    }

    @Test
    fun `getElapsedDaysLabel describes today as today`() {
        val label = DateUtils.getElapsedDaysLabel(0L, "Today", "1 day ago", "%d days ago")
        assertEquals("Today", label)
    }

    @Test
    fun `getElapsedDaysLabel uses the singular text for one day ago`() {
        val label = DateUtils.getElapsedDaysLabel(1L, "Today", "1 day ago", "%d days ago")
        assertEquals("1 day ago", label)
    }

    @Test
    fun `getElapsedDaysLabel formats more than one day with the day count`() {
        val label = DateUtils.getElapsedDaysLabel(5L, "Today", "1 day ago", "%d days ago")
        assertEquals("5 days ago", label)
    }

    @Test
    fun `getElapsedDaysLabel returns null for a future diary`() {
        assertNull(DateUtils.getElapsedDaysLabel(-1L, "Today", "1 day ago", "%d days ago"))
    }

    @Test
    fun `convertTimeFormatPattern converts 24 hour pattern to 12 hour pattern with am pm at the end`() {
        val result = DateUtils.convertTimeFormatPattern(
            pattern = "yyyy. M. d. HH:mm",
            timeFormatMode = TimeFormatMode.HOUR_12,
            amPmMarkerIsPrefix = false,
        )
        assertEquals("yyyy. M. d. h:mm a", result)
    }

    @Test
    fun `convertTimeFormatPattern converts 24 hour pattern to 12 hour pattern with am pm at the front`() {
        val result = DateUtils.convertTimeFormatPattern(
            pattern = "yyyy'년' M'월' d'일' HH:mm:ss",
            timeFormatMode = TimeFormatMode.HOUR_12,
            amPmMarkerIsPrefix = true,
        )
        assertEquals("yyyy'년' M'월' d'일' a h:mm:ss", result)
    }

    @Test
    fun `convertTimeFormatPattern keeps 12 hour pattern with am pm unchanged`() {
        val result = DateUtils.convertTimeFormatPattern(
            pattern = "h:mm a",
            timeFormatMode = TimeFormatMode.HOUR_12,
            amPmMarkerIsPrefix = false,
        )
        assertEquals("h:mm a", result)
    }

    @Test
    fun `convertTimeFormatPattern converts 12 hour pattern to 24 hour pattern removing am pm at the end`() {
        val result = DateUtils.convertTimeFormatPattern(
            pattern = "yyyy. M. d. h:mm a",
            timeFormatMode = TimeFormatMode.HOUR_24,
            amPmMarkerIsPrefix = false,
        )
        assertEquals("yyyy. M. d. HH:mm", result)
    }

    @Test
    fun `convertTimeFormatPattern converts 12 hour pattern to 24 hour pattern removing am pm at the front`() {
        val result = DateUtils.convertTimeFormatPattern(
            pattern = "a h:mm:ss z",
            timeFormatMode = TimeFormatMode.HOUR_24,
            amPmMarkerIsPrefix = true,
        )
        assertEquals("HH:mm:ss z", result)
    }

    @Test
    fun `convertTimeFormatPattern keeps quoted literal and does not treat it as am pm marker`() {
        val result = DateUtils.convertTimeFormatPattern(
            pattern = "'a' HH:mm",
            timeFormatMode = TimeFormatMode.HOUR_24,
            amPmMarkerIsPrefix = false,
        )
        assertEquals("'a' HH:mm", result)
    }

    @Test
    fun `convertTimeFormatPattern keeps pattern unchanged on system default mode`() {
        val result = DateUtils.convertTimeFormatPattern(
            pattern = "yyyy. M. d. HH:mm:ss",
            timeFormatMode = TimeFormatMode.SYSTEM_DEFAULT,
            amPmMarkerIsPrefix = false,
        )
        assertEquals("yyyy. M. d. HH:mm:ss", result)
    }

    @Test
    fun `convertTimeFormatPattern keeps date only pattern unchanged`() {
        val result = DateUtils.convertTimeFormatPattern(
            pattern = "yyyy. M. d.",
            timeFormatMode = TimeFormatMode.HOUR_12,
            amPmMarkerIsPrefix = false,
        )
        assertEquals("yyyy. M. d.", result)
    }
}
