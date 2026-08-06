package me.blog.korn123.easydiary.domain.model

import me.blog.korn123.easydiary.helper.AlarmConstants

data class Alarm(
    val alarmId: Int = 0,
    val timeInMinutes: Int = 0,
    val days: Int = 0,
    val isEnabled: Boolean = false,
    val vibrate: Boolean = false,
    val soundTitle: String? = null,
    val soundUri: String? = null,
    val label: String? = null,
    val workMode: Int = AlarmConstants.WORK_MODE_DIARY_WRITING,
    val retryCount: Int = 0
)
