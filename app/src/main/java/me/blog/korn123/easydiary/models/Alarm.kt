package me.blog.korn123.easydiary.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import me.blog.korn123.easydiary.helper.AlarmConstants

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
    var workMode: Int = AlarmConstants.WORK_MODE_DIARY_WRITING // 0: Diary write alarm, 1: Auto backup with google drive, 2: Auto backup with application storage
    var retryCount: Int = 0
    val id: Int
        get() = this.sequence

    constructor()

    constructor(days: Int) {
        this.days = days
    }
}
