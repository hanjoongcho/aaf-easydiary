package me.blog.korn123.easydiary.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
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
        val diffDays = ceil(targetTimeStamp.minus(System.currentTimeMillis()).div((1000 * 60 * 60 * 24).toDouble())).toInt()
        return if (diffDays >= 0) "D－$diffDays" else "D＋${diffDays.unaryPlus()}"
    }
}