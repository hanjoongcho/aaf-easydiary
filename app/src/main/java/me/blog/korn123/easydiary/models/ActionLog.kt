package me.blog.korn123.easydiary.models

import io.github.aafactory.commons.utils.DateUtils
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ActionLog : RealmObject {

    @PrimaryKey
    var sequence: Int = 0
    var className: String? = null
    var signature: String? = null
    var key: String? = null
    var value: String? = null

    constructor()

    constructor(className: String?, signature: String?, key: String?, value: String?) : super() {
        this.sequence = sequence
        this.className = "[${DateUtils.getFullPatternDateWithTimeAndSeconds(System.currentTimeMillis())}] $className"
        this.signature = signature
        this.key = key
        this.value = value
    }
}