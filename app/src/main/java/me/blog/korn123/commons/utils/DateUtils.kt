package me.blog.korn123.commons.utils

import android.content.Context
import me.blog.korn123.easydiary.enums.DateTimeFormat
import me.blog.korn123.easydiary.extensions.storedDatetimeFormat
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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

    /**
     * 날짜 문자열을 입력받아 해당 날짜의 가장 마지막 시점(23:59:59.999)의 Epoch Millisecond(Long)로 변환합니다.
     *
     * @param dateString 변환할 날짜 문자열 (예: "2026-09-09")
     * @param pattern dateString의 날짜 포맷 패턴 (예: "yyyy-MM-dd")
     * @param zoneId 적용할 타임존 (기본값: 시스템 기본 타임존)
     * @return 해당 날짜 23:59:59.999 시점의 Epoch 밀리초 (Long)
     */
    fun dateToTimeMillis(
        dateString: String,
        pattern: String,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Long {
        // 1. 날짜 패턴 포맷터 생성
        val formatter = DateTimeFormatter.ofPattern(pattern)

        // 2. 문자열을 시간 정보가 없는 LocalDate로 파싱
        val localDate = LocalDate.parse(dateString, formatter)

        // 3. 해당 날짜의 최대 시간(23:59:59.999...)을 결합
        val endOfDay = localDate.atTime(LocalTime.MAX)

        // 4. 타임존을 결합하여 Instant로 변환 후 Epoch 밀리초 추출
        return endOfDay
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    /**
     * java.time API를 사용한 안전하고 명확한 타임존 변환 함수
     *
     * @param timeMillis 변환할 밀리초 단위의 Epoch 시간
     * @param pattern 날짜 포맷 패턴
     * @param zoneId 적용할 타임존 ID (기본값: ZoneId.systemDefault())
     */
    fun timeMillisToDate(
        timeMillis: Long,
        pattern: String,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): String {
        val instant = Instant.ofEpochMilli(timeMillis)
        val formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
        return formatter.format(instant)
    }

    // ------------------------------------------------------------------
    // Awesome Application Factory legacy functions
    // ------------------------------------------------------------------
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
