package me.blog.korn123.easydiary.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Alarm : RealmObject {
    @PrimaryKey
    var sequence: Int = -1
    var timeInMinutes: Int = 0
    var days: Int = 0
    var isEnabled: Boolean = false
    var vibrate: Boolean = false
    var soundTitle: String? = null
    var soundUri: String? = null
    var label: String? = null
    val id: Int
        get() = this.sequence

    constructor()

    constructor(days: Int) {
        this.days = days
    }
}