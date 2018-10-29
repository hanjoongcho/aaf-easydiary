package me.blog.korn123.easydiary.models

import io.github.aafactory.commons.utils.DateUtils
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

open class DiaryDto : RealmObject {
    @PrimaryKey
    var sequence: Int = 0

    @Ignore
    private val sessionId: Int = 0

    var currentTimeMillis: Long = 0

    var title: String? = null

    var contents: String? = null

    var dateString: String? = null

    var weather: Int = 0

    var photoUris: RealmList<PhotoUriDto>? = null

    var fontName: String? = null

    var fontSize: Float = 0.toFloat()
    
    var isAllDay: Boolean = false

    constructor()

    constructor(sequence: Int, currentTimeMillis: Long, title: String, contents: String) {
        this.sequence = sequence
        this.currentTimeMillis = currentTimeMillis
        this.title = title
        this.contents = contents
        this.dateString = DateUtils.timeMillisToDateTime(currentTimeMillis, DateUtils.DATE_PATTERN_DASH)
    }

    constructor(sequence: Int, currentTimeMillis: Long, title: String, contents: String, weather: Int, isAllDay: Boolean = false) {
        this.sequence = sequence
        this.currentTimeMillis = currentTimeMillis
        this.title = title
        this.contents = contents
        this.dateString = DateUtils.timeMillisToDateTime(currentTimeMillis, DateUtils.DATE_PATTERN_DASH)
        this.weather = weather
        this.isAllDay = isAllDay
    }
}
