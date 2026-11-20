package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.helper.GCalendarConstants

/**
 * Recognizes the Google calendars that carry public holidays.
 *
 * Google builds the id of a public holiday calendar as
 * "<language>.<country>#holiday@group.v.calendar.google.com", so the id itself says whether it is
 * a holiday calendar, whatever the country is:
 *
 * - "ko.south_korea#holiday@group.v.calendar.google.com"
 * - "en.south_korea#holiday@group.v.calendar.google.com"
 * - "en.usa#holiday@group.v.calendar.google.com"
 * - "ja.japanese#holiday@group.v.calendar.google.com"
 * - "de.german#holiday@group.v.calendar.google.com"
 *
 * A personal calendar ("someone@gmail.com"), a shared calendar ("abc123@group.calendar.google.com")
 * and the contacts' birthdays calendar ("...#contacts@group.v.calendar.google.com") do not end with
 * that suffix and are not holiday calendars.
 */
object GoogleCalendarUtils {
    fun isGoogleHolidayCalendar(calendarId: String): Boolean {
        val id = calendarId.trim()
        return (id.length > GCalendarConstants.HOLIDAY_CALENDAR_SUFFIX.length) &&
            id.endsWith(GCalendarConstants.HOLIDAY_CALENDAR_SUFFIX, ignoreCase = true)
    }

    /**
     * Determines whether an event in a Google Holiday Calendar is a public holiday.
     * Excludes non-public holiday events such as Observances ("Observance", "기념일", "記念日", etc.)
     * and Seasons ("Season", "절기", "節気", etc.) across various languages.
     */
    fun isPublicHoliday(description: String?): Boolean {
        if (description.isNullOrBlank()) return true

        val nonPublicKeywords =
            listOf(
                // English, French, Spanish, Portuguese (Observance / Observancia / Observância)
                "observan",
                "observân",
                "season",
                "optional holiday",
                "restricted holiday",
                // Korean (ko.#holiday)
                "기념일",
                "절기",
                // Japanese (ja.#holiday)
                "記念日",
                "行事",
                "節気",
                // Chinese Simplified & Traditional (zh.#holiday)
                "纪念日",
                "紀念日",
                "节气",
                "節氣",
                // German (de.#holiday)
                "gedenktag",
                // Hindi / India (hi.#holiday)
                "प्रतिबंधित अवकाश",
                "जयंती",
                "ऋतु",
                // Russian (ru.#holiday)
                "памятная дата",
            )
        return nonPublicKeywords.none { keyword -> description.contains(keyword, ignoreCase = true) }
    }
}
