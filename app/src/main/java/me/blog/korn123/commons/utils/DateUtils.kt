package me.blog.korn123.commons.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object DateUtils {
    val HOURS_24 = 24
    val MINUTES_60 = 60
    val SECONDS_60 = 60
    val MILLI_SECONDS_1000 = 1000
    private val UNIT_HEX = 16
    val DATE_PATTERN_DASH = "yyyy-MM-dd"
    val TIME_PATTERN = "HH:mm"
    val TIME_PATTERN_WITH_SECONDS = "HH:mm ss"
    val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
    val DATE_TIME_PATTERN_WITHOUT_DELIMITER = "yyyyMMddHHmmss"
    val DATE_HMS_PATTERN = "yyyyMMddHHmmss"
    val TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
    val YEAR_PATTERN = "yyyy"
    val MONTH_PATTERN = "MM"
    val DAY_PATTERN = "dd"
    val DATE_PATTERN = "yyyyMMdd"
    val TIME_HMS_PATTERN = "HHmmss"
    val TIME_HMS_PATTERN_COLONE = "HH:mm:ss"

    fun getFullPatternDate(timeMillis: Long): String {
        val date = Date(timeMillis)
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getFullPatternDateWithTime(timeMillis: Long): String {
        val date = Date(timeMillis)
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
        val hourFormat = SimpleDateFormat(TIME_PATTERN)
        return String.format("%s %s", dateFormat.format(date), hourFormat.format(date))
    }
    
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
