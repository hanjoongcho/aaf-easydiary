package me.blog.korn123.easydiary.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for GoogleCalendarUtils.
 */
class GoogleCalendarUtilsTest {
    @Test
    fun `recognizes the Korean holiday calendars`() {
        assertTrue(GoogleCalendarUtils.isGoogleHolidayCalendar("ko.south_korea#holiday@group.v.calendar.google.com"))
        assertTrue(GoogleCalendarUtils.isGoogleHolidayCalendar("en.south_korea#holiday@group.v.calendar.google.com"))
        assertTrue(
            GoogleCalendarUtils.isGoogleHolidayCalendar(
                "ko.south_korea.official#holiday@group.v.calendar.google.com",
            ),
        )
    }

    @Test
    fun `recognizes holiday calendars of other countries`() {
        assertTrue(GoogleCalendarUtils.isGoogleHolidayCalendar("en.usa#holiday@group.v.calendar.google.com"))
        assertTrue(GoogleCalendarUtils.isGoogleHolidayCalendar("ja.japanese#holiday@group.v.calendar.google.com"))
        assertTrue(GoogleCalendarUtils.isGoogleHolidayCalendar("de.german#holiday@group.v.calendar.google.com"))
        assertTrue(GoogleCalendarUtils.isGoogleHolidayCalendar("fr.french#holiday@group.v.calendar.google.com"))
        assertTrue(GoogleCalendarUtils.isGoogleHolidayCalendar("en.uk#holiday@group.v.calendar.google.com"))
        assertTrue(GoogleCalendarUtils.isGoogleHolidayCalendar("zh-cn.china#holiday@group.v.calendar.google.com"))
    }

    @Test
    fun `ignores case and surrounding whitespace of a holiday calendar id`() {
        assertTrue(
            GoogleCalendarUtils.isGoogleHolidayCalendar(
                "EN.USA#HOLIDAY@GROUP.V.CALENDAR.GOOGLE.COM",
            ),
        )
        assertTrue(
            GoogleCalendarUtils.isGoogleHolidayCalendar(
                "  en.usa#holiday@group.v.calendar.google.com  ",
            ),
        )
    }

    @Test
    fun `does not treat personal or shared calendars as holiday calendars`() {
        assertFalse(GoogleCalendarUtils.isGoogleHolidayCalendar("someone@gmail.com"))
        assertFalse(GoogleCalendarUtils.isGoogleHolidayCalendar("hanjoongcho@gmail.com"))
        assertFalse(GoogleCalendarUtils.isGoogleHolidayCalendar("abc123def456@group.calendar.google.com"))
        assertFalse(
            GoogleCalendarUtils.isGoogleHolidayCalendar(
                "someone@gmail.com#contacts@group.v.calendar.google.com",
            ),
        )
    }

    @Test
    fun `does not treat look-alike ids as holiday calendars`() {
        assertFalse(GoogleCalendarUtils.isGoogleHolidayCalendar(""))
        assertFalse(GoogleCalendarUtils.isGoogleHolidayCalendar("   "))
        assertFalse(GoogleCalendarUtils.isGoogleHolidayCalendar("#holiday@group.v.calendar.google.com"))
        assertFalse(
            GoogleCalendarUtils.isGoogleHolidayCalendar(
                "en.usa#holiday@group.v.calendar.google.com.evil.example",
            ),
        )
        assertFalse(
            GoogleCalendarUtils.isGoogleHolidayCalendar(
                "en.usa#holiday@group.v.calendar.google.co",
            ),
        )
        assertFalse(GoogleCalendarUtils.isGoogleHolidayCalendar("en.usa#holiday"))
    }
}
