package me.blog.korn123.commons.utils

import android.content.Context
import me.blog.korn123.easydiary.enums.DateTimeFormat
import me.blog.korn123.easydiary.extensions.storedDatetimeFormat
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object DateUtils {
    fun getDateStringFromTimeMillis(
        timeMillis: Long,
        dateFormat: Int = SimpleDateFormat.FULL,
        locale: Locale = Locale.getDefault(),
    ): String {
        val date = Date(timeMillis)
        val simpleDateFormat = SimpleDateFormat.getDateInstance(dateFormat, locale)
        return simpleDateFormat.format(date)
    }

    fun getTimeStringFromTimeMillis(
        timeMillis: Long,
        timeFormat: Int = SimpleDateFormat.SHORT,
        locale: Locale = Locale.getDefault(),
    ): String {
        val date = Date(timeMillis)
        val simpleDateFormat = SimpleDateFormat.getTimeInstance(timeFormat, locale)
        return simpleDateFormat.format(date)
    }

    fun getDateTimeStringFromTimeMillis(
        timeMillis: Long,
        dateFormat: Int = SimpleDateFormat.FULL,
        timeFormat: Int = SimpleDateFormat.SHORT,
        dateTimeFormat: DateTimeFormat? = null,
        locale: Locale = Locale.getDefault(),
    ): String {
        val date = Date(timeMillis)
        val simpleDateFormat =
            when (dateTimeFormat == null) {
                true -> {
                    SimpleDateFormat.getDateTimeInstance(dateFormat, timeFormat, locale)
                }

                false -> {
                    SimpleDateFormat.getDateTimeInstance(
                        dateTimeFormat.getDateKey(),
                        dateTimeFormat.getTimeKey(),
                        locale,
                    )
                }
            }
        return simpleDateFormat.format(date)
    }

    fun getDateTimeStringForceFormatting(
        timeMillis: Long,
        context: Context,
    ) = getDateTimeStringFromTimeMillis(timeMillis, -1, -1, context.storedDatetimeFormat())

    fun getOnlyDayRemaining(
        targetTimeStamp: Long,
        onlyDays: Boolean = true,
        yearFormat: String = "",
        dayFormat: String = "",
    ): String {
        val oneDayMillis: Long = 1000 * 60 * 60 * 24
        val diffTarget =
            Calendar
                .getInstance(Locale.getDefault())
                .apply {
                    timeInMillis = targetTimeStamp
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
        val todayTimeStamp =
            Calendar
                .getInstance(Locale.getDefault())
                .apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

        val diffDays = abs(diffTarget.minus(todayTimeStamp).div(oneDayMillis))
        val dayRemaining =
            when (onlyDays) {
                true -> {
                    when {
                        diffTarget > todayTimeStamp -> "D－$diffDays"
                        diffTarget < todayTimeStamp -> "D＋$diffDays"
                        else -> "D-Day"
                    }
                }

                false -> {
                    val start = todayTimeStamp.coerceAtMost(diffTarget)
                    val end = todayTimeStamp.coerceAtLeast(diffTarget)
                    val calendar: Calendar = Calendar.getInstance(Locale.getDefault())
                    calendar.timeInMillis = start
                    var countYear = 0
                    while (true) {
                        calendar.add(Calendar.YEAR, 1)
                        if (calendar.timeInMillis > end) {
                            calendar.add(Calendar.YEAR, -1)
                            break
                        } else {
                            countYear++
                        }
                    }

                    val years = MessageFormat.format(yearFormat, countYear)
                    val days = MessageFormat.format(dayFormat, end.minus(calendar.timeInMillis).div(oneDayMillis))
                    "（$years $days）"
                }
            }
        return dayRemaining
    }

    fun dateStringToTimeStamp(dateString: String): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date = formatter.parse(dateString)
        return date.time
    }

    // / ------------------------------------------------------------------
    // / Awesome Application Factory legacy functions
    // / ------------------------------------------------------------------
//    const val TIME_PATTERN = "HH:mm"
//    const val TIME_PATTERN_WITH_SECONDS = "HH:mm ss"
    const val DATE_PATTERN_DASH = "yyyy-MM-dd"
    const val DATE_TIME_PATTERN_WITHOUT_DASH = "yyyyMMddHHmmss"
//    const val YEAR_PATTERN = "yyyy"
//    const val MONTH_PATTERN = "MM"
//    const val DAY_PATTERN = "dd"

//    fun getFullPatternDate(timeMillis: Long): String {
//        val date = Date(timeMillis)
//        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
//        return dateFormat.format(date)
//    }

//        fun getFullPatternDateWithTime(timeMillis: Long): String {
//            val date = Date(timeMillis)
//            val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
//            val hourFormat = SimpleDateFormat(TIME_PATTERN)
//            return String.format("%s %s", dateFormat.format(date), hourFormat.format(date))
//        }

//    fun getFullPatternDateWithTimeAndSeconds(timeMillis: Long, locale: Locale = Locale.getDefault()): String {
//        val date = Date(timeMillis)
//        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, locale)
//        val hourFormat = SimpleDateFormat(TIME_PATTERN_WITH_SECONDS)
//        return String.format("%s %s", dateFormat.format(date), hourFormat.format(date))
//    }

    fun getCurrentDateTime(pattern: String): String {
        val date = Date()
        val dateFormat = SimpleDateFormat(pattern)
        return dateFormat.format(date)
    }

    fun timeMillisToDateTime(
        timeMillis: Long,
        pattern: String,
    ): String {
        val date = Date(timeMillis)
        val dateFormat = SimpleDateFormat(pattern)
        return dateFormat.format(date)
    }
}
