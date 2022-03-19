package me.blog.korn123.commons.utils

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        fun getDateStringFromTimeMillis(timeMillis: Long, dateFormat: Int = SimpleDateFormat.FULL, locale: Locale = Locale.getDefault()): String {
            val date = Date(timeMillis)
            val dateFormat = SimpleDateFormat.getDateInstance(dateFormat, locale)
            return dateFormat.format(date)
        }

        fun getTimeStringFromTimeMillis(timeMillis: Long, timeFormat: Int = SimpleDateFormat.SHORT, locale: Locale = Locale.getDefault()): String {
            val date = Date(timeMillis)
            val dateFormat = SimpleDateFormat.getTimeInstance(timeFormat, locale)
            return dateFormat.format(date)
        }

        fun getDateTimeStringFromTimeMillis(timeMillis: Long, dateFormat: Int = SimpleDateFormat.FULL, timeFormat: Int = SimpleDateFormat.SHORT, locale: Locale = Locale.getDefault()): String {
            val date = Date(timeMillis)
            val dateFormat = SimpleDateFormat.getDateTimeInstance(dateFormat, timeFormat, locale)
            return dateFormat.format(date)
        }


        /// ------------------------------------------------------------------
        /// Awesome Application Factory legacy functions
        /// ------------------------------------------------------------------
        const val TIME_PATTERN = "HH:mm"
        const val TIME_PATTERN_WITH_SECONDS = "HH:mm ss"
        const val DATE_PATTERN_DASH = "yyyy-MM-dd"
        const val DATE_TIME_PATTERN_WITHOUT_DELIMITER = "yyyyMMddHHmmss"
        const val YEAR_PATTERN = "yyyy"
        const val MONTH_PATTERN = "MM"
        const val DAY_PATTERN = "dd"

        fun getFullPatternDate(timeMillis: Long): String {
            val date = Date(timeMillis)
            val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
            return dateFormat.format(date)
        }

//        fun getFullPatternDateWithTime(timeMillis: Long): String {
//            val date = Date(timeMillis)
//            val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
//            val hourFormat = SimpleDateFormat(TIME_PATTERN)
//            return String.format("%s %s", dateFormat.format(date), hourFormat.format(date))
//        }

        fun getFullPatternDateWithTimeAndSeconds(timeMillis: Long, locale: Locale = Locale.getDefault()): String {
            val date = Date(timeMillis)
            val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, locale)
            val hourFormat = SimpleDateFormat(TIME_PATTERN_WITH_SECONDS)
            return String.format("%s %s", dateFormat.format(date), hourFormat.format(date))
        }

        fun getCurrentDateTime(pattern: String): String {
            val date = Date()
            val dateFormat = SimpleDateFormat(pattern)
            return dateFormat.format(date)
        }

        fun timeMillisToDateTime(timeMillis: Long, pattern: String): String {
            val date = Date(timeMillis)
            val dateFormat = SimpleDateFormat(pattern)
            return dateFormat.format(date)
        }
    }
}