package me.blog.korn123.easydiary.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlin.math.abs
import kotlin.math.ceil

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

    fun getRemainDays(): String {
        val oneDayMillis: Long = 1000 * 60 * 60 * 24
        val currentTimeStamp = System.currentTimeMillis()
        val diffDays = abs(targetTimeStamp.minus(currentTimeStamp).div(oneDayMillis.toDouble())).toInt()
        return if (targetTimeStamp > currentTimeStamp) "D－$diffDays" else "D＋${diffDays.unaryPlus()}"
    }

    fun getRemainHours(): String {
        val oneDayMillis: Long = 1000 * 60 * 60 * 24
        val oneHourMillis: Long = 1000 * 60 * 60
        val currentTimeStamp = System.currentTimeMillis()
        val remainMillis = abs(targetTimeStamp.minus(currentTimeStamp) % oneDayMillis)
        return remainMillis.div(oneHourMillis).toString()
    }
}