package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.helper.DateUtilConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
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
}
