package me.blog.korn123.easydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.blog.korn123.easydiary.helper.AlarmConstants

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    var alarmId: Int = 0,
    var timeInMinutes: Int = 0,
    var days: Int = 0,
    var isEnabled: Boolean = false,
    var vibrate: Boolean = false,
    var soundTitle: String? = null,
    var soundUri: String? = null,
    var label: String? = null,
    var workMode: Int = AlarmConstants.WORK_MODE_DIARY_WRITING,
    var retryCount: Int = 0
)
