package me.blog.korn123.easydiary.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.text.MessageFormat
import java.util.*
import kotlin.math.abs

open class DDay : RealmObject {
    @PrimaryKey
    var sequence: Int = -1
    var targetTimeStamp: Long = 0
    var title: String? = null

    constructor()

    constructor(title: String) {
        this.title = title
        this.targetTimeStamp = System.currentTimeMillis()
    }

    constructor(title: String, targetTimeStamp: Long) {
        this.title = title
        this.targetTimeStamp = targetTimeStamp
    }

    fun getDayRemaining(onlyDays: Boolean = true, yearFormat: String = "", dayFormat: String = ""): String {
        val oneDayMillis: Long = 1000 * 60 * 60 * 24
        val todayTimeStamp = System.currentTimeMillis()
        val diffDays = abs(targetTimeStamp.minus(todayTimeStamp).div(oneDayMillis))
        val dayRemaining = when (onlyDays) {
            true -> if (targetTimeStamp > todayTimeStamp) "D－$diffDays" else "D＋$diffDays"
            false -> {
                // Check Leaf Year
                val start = todayTimeStamp.coerceAtMost(targetTimeStamp)
                val end = todayTimeStamp.coerceAtLeast(targetTimeStamp)
                val calendar: Calendar = Calendar.getInstance(Locale.getDefault())
                calendar.timeInMillis = start
                var countYear = 0
                while (true) {
                    calendar.add(Calendar.YEAR, 1)
                    if (calendar.timeInMillis > end) {
                        calendar.add(Calendar.YEAR, -1)
                        break;
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

    fun getTimeRemaining(): String {
        val oneDayMillis: Long = 1000 * 60 * 60 * 24
        val oneHourMillis: Long = 1000 * 60 * 60
        val oneMinuteMillis: Long = 1000 * 60
        val currentTimeStamp = System.currentTimeMillis()
        val remainHourMillis = abs(targetTimeStamp.minus(currentTimeStamp).rem(oneDayMillis))
        val remainMinuteMillis = abs(remainHourMillis.rem(oneHourMillis))
        return "${MessageFormat.format("{0,number} {0,choice,1#Hour|1<Hours}", remainHourMillis.div(oneHourMillis))} ${MessageFormat.format("{0,number} {0,choice,1#Minute|1<Minutes}", remainMinuteMillis.div(oneMinuteMillis))}"
    }
}