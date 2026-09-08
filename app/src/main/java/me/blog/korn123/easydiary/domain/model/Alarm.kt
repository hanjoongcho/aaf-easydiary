package me.blog.korn123.easydiary.domain.model

import me.blog.korn123.easydiary.helper.AlarmConstants

data class Alarm(
    val alarmId: Int = 0,
    var timeInMinutes: Int = 0,
    var days: Int = 0,
    var isEnabled: Boolean = false,
    var vibrate: Boolean = false,
    var soundTitle: String? = null,
    var soundUri: String? = null,
    var label: String? = null,
    var workMode: Int = AlarmConstants.WORK_MODE_DIARY_WRITING,
    var retryCount: Int = 0,
)
