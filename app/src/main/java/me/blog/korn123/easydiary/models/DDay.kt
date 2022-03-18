package me.blog.korn123.easydiary.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import me.blog.korn123.easydiary.R
import java.text.MessageFormat
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
        val currentTimeStamp = System.currentTimeMillis()
        val diffDays = abs(targetTimeStamp.minus(currentTimeStamp).div(oneDayMillis))
        val dayRemaining = when (onlyDays) {
            true -> if (targetTimeStamp > currentTimeStamp) "D－$diffDays" else "D＋$diffDays"
            false -> {
                val years = MessageFormat.format(yearFormat, diffDays.div(365))
                val days = MessageFormat.format(dayFormat, diffDays.rem(365))
                "$years $days"
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