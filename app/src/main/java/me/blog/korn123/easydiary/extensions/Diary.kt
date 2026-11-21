package me.blog.korn123.easydiary.extensions

import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.utils.DateUtils

/**
 * The instant the day of this diary starts at in the device time zone (issue #214).
 *
 * An 'All day' diary keeps its date in [Diary.dateString], and [Diary.currentTimeMillis] is only the
 * moment the day was saved at (00:00 of that day in the time zone the device was in then). A time zone
 * change moves that instant to another day - 00:00 in UTC+12 is the previous day in UTC+2 - which is how
 * entries appeared to change their date after a flight. The stored date string is therefore the
 * authority for an all-day diary, and this returns the start of that day in the current time zone.
 *
 * A diary that is not all day keeps its date and time from [Diary.currentTimeMillis], and so does an
 * all-day diary whose date string is missing or unreadable.
 */
fun Diary.dateAnchorTimeMillis(): Long {
    if (!isAllDay) return currentTimeMillis
    return DateUtils.startOfDayTimeMillis(dateString) ?: currentTimeMillis
}
