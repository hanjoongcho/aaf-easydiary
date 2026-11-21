package me.blog.korn123.easydiary.utils

import android.content.Context
import android.text.format.DateFormat
import me.blog.korn123.easydiary.enums.DateTimeFormat
import me.blog.korn123.easydiary.enums.TimeFormatMode
import me.blog.korn123.easydiary.extensions.EMPTY
import me.blog.korn123.easydiary.extensions.storedDatetimeFormat
import me.blog.korn123.easydiary.extensions.storedTimeFormat
import java.text.DateFormat as JavaDateFormat
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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
        timeFormatMode: TimeFormatMode = TimeFormatMode.SYSTEM_DEFAULT,
    ): String {
        val date = Date(timeMillis)
        val simpleDateFormat = SimpleDateFormat.getTimeInstance(timeFormat, locale)
        return simpleDateFormat.formatWithTimeFormatMode(date, locale, timeFormatMode)
    }

    fun getDateTimeStringFromTimeMillis(
        timeMillis: Long,
        dateFormat: Int = SimpleDateFormat.FULL,
        timeFormat: Int = SimpleDateFormat.SHORT,
        dateTimeFormat: DateTimeFormat? = null,
        locale: Locale = Locale.getDefault(),
        timeFormatMode: TimeFormatMode = TimeFormatMode.SYSTEM_DEFAULT,
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
        return simpleDateFormat.formatWithTimeFormatMode(date, locale, timeFormatMode)
    }

    fun getDateTimeStringForceFormatting(
        timeMillis: Long,
        context: Context,
    ) = getDateTimeStringFromTimeMillis(
        timeMillis = timeMillis,
        dateFormat = -1,
        timeFormat = -1,
        dateTimeFormat = context.storedDatetimeFormat(),
        timeFormatMode = context.storedTimeFormat(),
    )

    /**
     * [timeFormatMode]에 맞게 시간 필드가 변환된 SimpleDateFormat 패턴을 반환합니다.
     *
     * SimpleDateFormat.getTimeInstance, getDateTimeInstance가 돌려주는 패턴은 로케일마다
     * 12시간제('a h:mm') 또는 24시간제('HH:mm')로 고정되어 있고, 앱에서 이를 바꾸려면
     * 패턴 문자열을 변환한 뒤 다시 포맷해야 합니다. 이 함수는 그 변환만 순수하게 수행합니다.
     *
     * - [TimeFormatMode.HOUR_12]: 24시간제 hour 필드(H, k)를 로케일 관례대로 앞자리 0이 없는 12시간제(h)로
     *   바꾸고, 오전/오후 표시(a)가 없으면 [amPmMarkerIsPrefix] 위치에 추가합니다.
     * - [TimeFormatMode.HOUR_24]: 12시간제 hour 필드(h, K)를 두 자리 24시간제(HH)로 바꾸고,
     *   오전/오후 표시(a)를 제거합니다.
     * - 따옴표(')로 감싼 리터럴 안의 문자는 변환하지 않습니다.
     */
    fun convertTimeFormatPattern(
        pattern: String,
        timeFormatMode: TimeFormatMode,
        amPmMarkerIsPrefix: Boolean,
    ): String =
        when (timeFormatMode) {
            TimeFormatMode.SYSTEM_DEFAULT -> {
                pattern
            }

            TimeFormatMode.HOUR_12 -> {
                pattern.convertHourField(HOUR_FIELDS_OF_24_HOUR_CLOCK, HOUR_FIELD_12).insertAmPmMarker(amPmMarkerIsPrefix)
            }

            TimeFormatMode.HOUR_24 -> {
                pattern.convertHourField(HOUR_FIELDS_OF_12_HOUR_CLOCK, HOUR_FIELD_24).removeAmPmMarker()
            }
        }

    fun getOnlyDayRemaining(
        targetTimeStamp: Long,
        onlyDays: Boolean = true,
        yearFormat: String = String.EMPTY,
        dayFormat: String = String.EMPTY,
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
     * The start of the day [dateString] ('yyyy-MM-dd') names, in the device time zone, as epoch millis.
     * Returns null when the string is missing or not a date in that form.
     *
     * An 'All day' diary stores the day itself as its date (issue #214), so the date has to be anchored
     * to 00:00 of that day in whatever time zone the device is in now, instead of to the instant the
     * entry was written at, which a time zone change moves to another day.
     */
    fun startOfDayTimeMillis(dateString: String?): Long? {
        val text = dateString?.trim().orEmpty()
        if (text.isEmpty()) return null
        return try {
            LocalDate
                .parse(text, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: DateTimeParseException) {
            null
        }
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

    // ------------------------------------------------------------------
    // Time format mode helpers
    // ------------------------------------------------------------------
    private fun JavaDateFormat.formatWithTimeFormatMode(
        date: Date,
        locale: Locale,
        timeFormatMode: TimeFormatMode,
    ): String {
        if (timeFormatMode == TimeFormatMode.SYSTEM_DEFAULT) return format(date)
        val simpleDateFormat = this as? SimpleDateFormat ?: return format(date)
        return simpleDateFormat
            .applyTimeFormatMode(locale, timeFormatMode)
            .format(date)
    }

    private fun SimpleDateFormat.applyTimeFormatMode(
        locale: Locale,
        timeFormatMode: TimeFormatMode,
    ): SimpleDateFormat {
        val pattern = toPattern()
        val convertedPattern = convertTimeFormatPattern(pattern, timeFormatMode, isAmPmMarkerPrefix(locale))
        if (convertedPattern == pattern) return this
        return SimpleDateFormat(convertedPattern, locale).apply { timeZone = this@applyTimeFormatMode.timeZone }
    }

    /**
     * 로케일이 오전/오후 표시를 시각 앞에 쓰는지 여부를 반환합니다.
     * (예: 한국어 '오후 3:30'은 앞, 영어 '3:30 PM'은 뒤)
     *
     * ICU의 12시간제 스켈레톤("hm")으로 로케일별 최적 패턴을 얻어 판단합니다.
     */
    private fun isAmPmMarkerPrefix(locale: Locale): Boolean {
        val bestPattern = DateFormat.getBestDateTimePattern(locale, "hm")
        val hourIndex = bestPattern.indexOfFirst { it in HOUR_FIELDS }
        val amPmIndex = bestPattern.indexOfFirst { it == AM_PM_FIELD }
        return amPmIndex >= 0 && hourIndex >= 0 && amPmIndex < hourIndex
    }

    private fun String.convertHourField(
        sourceFields: String,
        replacement: String,
    ): String {
        val mask = unquotedMask()
        val builder = StringBuilder(length)
        var index = 0
        while (index < length) {
            if (mask[index] && this[index] in sourceFields) {
                builder.append(replacement)
                while (index + 1 < length && mask[index + 1] && this[index + 1] == this[index]) index++
            } else {
                builder.append(this[index])
            }
            index++
        }
        return builder.toString()
    }

    private fun String.insertAmPmMarker(amPmMarkerIsPrefix: Boolean): String {
        val mask = unquotedMask()
        if (indices.any { mask[it] && this[it] == AM_PM_FIELD }) return this
        val hourIndex = indices.firstOrNull { mask[it] && this[it] in HOUR_FIELDS } ?: return this
        return when (amPmMarkerIsPrefix) {
            true -> {
                substring(0, hourIndex) + "$AM_PM_FIELD " + substring(hourIndex)
            }

            false -> {
                val lastTimeFieldIndex = indices.lastOrNull { mask[it] && this[it] in TIME_FIELDS } ?: hourIndex
                substring(0, lastTimeFieldIndex + 1) + " $AM_PM_FIELD" + substring(lastTimeFieldIndex + 1)
            }
        }
    }

    private fun String.removeAmPmMarker(): String {
        val mask = unquotedMask()
        val amPmIndex = indices.firstOrNull { mask[it] && this[it] == AM_PM_FIELD } ?: return this
        val startIndex = if (amPmIndex > 0 && this[amPmIndex - 1] == SPACE) amPmIndex - 1 else amPmIndex
        val endIndex =
            if (startIndex == amPmIndex && amPmIndex + 1 < length && this[amPmIndex + 1] == SPACE) {
                amPmIndex + 2
            } else {
                amPmIndex + 1
            }
        return removeRange(startIndex, endIndex)
    }

    /**
     * 따옴표(')로 감싼 리터럴을 제외한 위치를 true로 표시한 배열을 반환합니다.
     * ('를 두 번 연속으로 쓴 경우는 리터럴 따옴표 한 개로 처리합니다.)
     */
    private fun String.unquotedMask(): BooleanArray {
        val mask = BooleanArray(length)
        var quoted = false
        var index = 0
        while (index < length) {
            when {
                this[index] == QUOTE && index + 1 < length && this[index + 1] == QUOTE -> {
                    index++
                }

                this[index] == QUOTE -> {
                    quoted = !quoted
                }

                !quoted -> {
                    mask[index] = true
                }
            }
            index++
        }
        return mask
    }

    private const val AM_PM_FIELD = 'a'
    private const val SPACE = ' '
    private const val QUOTE = '\''
    private const val HOUR_FIELD_12 = "h"
    private const val HOUR_FIELD_24 = "HH"
    private const val HOUR_FIELDS_OF_12_HOUR_CLOCK = "hK"
    private const val HOUR_FIELDS_OF_24_HOUR_CLOCK = "Hk"
    private const val HOUR_FIELDS = "hHkK"
    private const val TIME_FIELDS = "hHkKms"
}
