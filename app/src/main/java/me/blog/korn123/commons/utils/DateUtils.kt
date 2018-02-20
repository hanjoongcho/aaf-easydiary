package me.blog.korn123.commons.utils

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
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

    val currentDateAsString: String
        get() = getCurrentDateAsString("yyyy-MM-dd")

    val currentDateTime: String
        get() = getCurrentDateTime("yyyy-MM-dd HH:mm:ss")

    fun getCurrentDateAsString(pattern: String): String {
        val df = SimpleDateFormat(pattern)
        return df.format(Date())
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(currentDateTime)
    }

    fun getCurrentDateTime(pattern: String): String {
        val dt = DateTime()
        val fmt = DateTimeFormat.forPattern(pattern)
        return fmt.print(dt)
    }

    fun getDateTime(currentTimeMillis: Long): String {
        val dt = DateTime(currentTimeMillis)
        val fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        return fmt.print(dt)
    }

    fun timeMillisToDateTime(timeMillis: Long, pattern: String): String {
        val dt = DateTime(timeMillis)
        val fmt = DateTimeFormat.forPattern(pattern)
        return fmt.print(dt)
    }

    fun timeMillisToDateTime(timeMillis: Long): String {
        val dt = DateTime(timeMillis)
        val fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        return fmt.print(dt)
    }

    fun timeMillisToHour(timeMillis: Long): String {
        val dt = DateTime(timeMillis)
        val fmt = DateTimeFormat.forPattern("HH")
        return fmt.print(dt)
    }

    fun getFullPatternDate(timeMillis: Long): String {
        //        DateTime dt = new DateTime(timeMillis);
        //        DateTimeFormatter fmt = DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("LL", Locale.getDefault()));
        //        return fmt.print(dt);
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

    fun getFullPatternDateWithTimeAndSeconds(timeMillis: Long): String {
        val date = Date(timeMillis)
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
        val hourFormat = SimpleDateFormat(TIME_PATTERN_WITH_SECONDS)
        return String.format("%s %s", dateFormat.format(date), hourFormat.format(date))
    }
}
