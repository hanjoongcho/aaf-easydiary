package me.blog.korn123.easydiary.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Unit tests for the time contract of an 'All day' diary (issue #235, 'All day by default').
 *
 * BaseDiaryEditingActivity.toggleTimePickerTool() resets the time to 00:00:00 and hides the time/seconds
 * pickers while 'All day' is checked, so a diary written with the default enabled stores the start of the
 * selected day and the writing screen shows a date without a time.
 */
class DiaryAllDayTest {
    private val year = 2026
    private val month = Calendar.MARCH
    private val dayOfMonth = 21

    @Test
    fun `an all day diary stores the start of the selected day`() {
        val timeMillis =
            EasyDiaryUtils.datePickerToTimeMillis(
                dayOfMonth = dayOfMonth,
                month = month,
                year = year,
                hour = 0,
                minute = 0,
                second = 0,
            )

        val calendar = Calendar.getInstance(Locale.getDefault()).apply { setTimeInMillis(timeMillis) }
        assertEquals(year, calendar.get(Calendar.YEAR))
        assertEquals(month, calendar.get(Calendar.MONTH))
        assertEquals(dayOfMonth, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `an all day diary is rendered as a date without a time`() {
        val timeMillis =
            EasyDiaryUtils.datePickerToTimeMillis(
                dayOfMonth = dayOfMonth,
                month = month,
                year = year,
                hour = 0,
                minute = 0,
                second = 0,
            )

        val dateString = DateUtils.getDateStringFromTimeMillis(timeMillis, SimpleDateFormat.FULL, Locale.ENGLISH)
        assertTrue(dateString.contains(dayOfMonth.toString()))
        assertTrue(dateString.contains("2026"))
        assertFalse("A date only format must not contain a time", dateString.contains(":"))
    }

    @Test
    fun `a diary which is not all day keeps the selected time`() {
        val timeMillis =
            EasyDiaryUtils.datePickerToTimeMillis(
                dayOfMonth = dayOfMonth,
                month = month,
                year = year,
                hour = 9,
                minute = 30,
                second = 45,
            )

        val calendar = Calendar.getInstance(Locale.getDefault()).apply { setTimeInMillis(timeMillis) }
        assertEquals(9, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, calendar.get(Calendar.MINUTE))
        assertEquals(45, calendar.get(Calendar.SECOND))
    }
}
