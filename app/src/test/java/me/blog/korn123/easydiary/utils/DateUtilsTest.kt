package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.enums.TimeFormatMode
import me.blog.korn123.easydiary.helper.DateUtilConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone

// Fixed offsets instead of named zones: the tests must not depend on daylight saving rules.
private const val UTC_PLUS_2 = "GMT+02:00"

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
    fun `getOnlyDayRemaining returns D-Day for a diary of today`() {
        assertEquals("D-Day", DateUtils.getOnlyDayRemaining(noonOf(daysFromToday = 0)))
    }

    @Test
    fun `getOnlyDayRemaining returns D plus the day count for a past diary`() {
        val remaining = DateUtils.getOnlyDayRemaining(noonOf(daysFromToday = -3))
        assertTrue(remaining.startsWith("D＋"))
        assertTrue(remaining.removePrefix("D＋").toInt() > 0)
    }

    @Test
    fun `getOnlyDayRemaining returns D minus the day count for a future diary`() {
        val remaining = DateUtils.getOnlyDayRemaining(noonOf(daysFromToday = 2))
        assertTrue(remaining.startsWith("D－"))
        assertTrue(remaining.removePrefix("D－").toInt() > 0)
    }

    // getOnlyDayRemaining() normalizes both dates to midnight in the device time zone, so the
    // tests work on noon of a date and stay clear of midnight boundaries.
    private fun noonOf(daysFromToday: Long): Long =
        LocalDate
            .now(ZoneId.systemDefault())
            .plusDays(daysFromToday)
            .atTime(12, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    @Test
    fun `startOfDayTimeMillis names the stored date in the current time zone`() {
        withDefaultTimeZone(UTC_PLUS_2) {
            val startOfDay = requireNotNull(DateUtils.startOfDayTimeMillis("2023-05-27"))

            assertEquals("2023-05-27", DateUtils.timeMillisToDate(startOfDay, DateUtilConstants.DATE_PATTERN_DASH))
            assertEquals("00", DateUtils.timeMillisToDate(startOfDay, "HH"))
        }
    }

    @Test
    fun `startOfDayTimeMillis returns null for a missing or unreadable date string`() {
        assertNull(DateUtils.startOfDayTimeMillis(null))
        assertNull(DateUtils.startOfDayTimeMillis(""))
        assertNull(DateUtils.startOfDayTimeMillis("   "))
        assertNull(DateUtils.startOfDayTimeMillis("2023-5-27"))
        assertNull(DateUtils.startOfDayTimeMillis("May 27, 2023"))
        assertNull(DateUtils.startOfDayTimeMillis("2023-02-30"))
    }

    /**
     * Runs [block] with [zoneId] as the device time zone and always restores the previous one, so a failing
     * assertion cannot leave the rest of the suite on a fixed offset.
     */
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

    @Test
    fun `getCurrentDateTime returns non empty formatted string`() {
        val current = DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss")
        assertNotNull(current)
        assertEquals(15, current.length)
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
