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

    @Test
    fun `identifies public holiday vs non-public holiday descriptions across multiple languages`() {
        // Public holidays (English, Korean, Japanese, Chinese, German, French, Spanish, Hindi, Indonesian)
        assertTrue(GoogleCalendarUtils.isPublicHoliday("Public holiday in South Korea"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("Public holiday in United States"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("대한민국의 공휴일"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("国民の祝日"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("法定节假日"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("Gesetzlicher Feiertag"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("Jour férié"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("Día festivo"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("राजपत्रित अवकाश"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("Hari Libur Nasional"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday("Cuti Bersama"))
        assertTrue(GoogleCalendarUtils.isPublicHoliday(null))
        assertTrue(GoogleCalendarUtils.isPublicHoliday(""))

        // Non-public holidays in various languages (Observances, Seasons, etc.)
        // English
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Observance in South Korea"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("observance"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("OBSERVANCE"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Season of the year"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("season"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Optional holiday"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Restricted holiday"))

        // Korean
        assertFalse(GoogleCalendarUtils.isPublicHoliday("대한민국의 기념일"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("24절기 중 하나"))

        // Japanese
        assertFalse(GoogleCalendarUtils.isPublicHoliday("日本の記念日"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("年中行事"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("二十四節気"))

        // Chinese
        assertFalse(GoogleCalendarUtils.isPublicHoliday("中国纪念日"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("中國紀念日"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("二十四节气"))

        // Spanish & Portuguese
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Observancia en España"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Observância"))

        // German
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Gedenktag"))

        // Hindi (India)
        assertFalse(GoogleCalendarUtils.isPublicHoliday("प्रतिबंधित अवकाश"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("जयंती"))

        // Indonesian
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Hari Peringatan"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Peringatan"))
        assertFalse(GoogleCalendarUtils.isPublicHoliday("Musim"))
    }
}
