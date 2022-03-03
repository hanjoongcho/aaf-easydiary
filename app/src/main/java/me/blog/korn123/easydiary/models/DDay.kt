package me.blog.korn123.easydiary.models

open class DDay {
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
        val diffDays = targetTimeStamp.minus(System.currentTimeMillis()).div((1000 * 60 * 60 * 24))
        return if (diffDays >= 0) "D－$diffDays" else "D＋$diffDays"
    }
}