package me.blog.korn123.easydiary.helper

import android.content.Context
import me.blog.korn123.easydiary.extensions.exportRealmFile
import me.blog.korn123.easydiary.extensions.openNotification
import me.blog.korn123.easydiary.models.Alarm

open class BaseAlarmWorkExecutor(
    val context: Context,
) {
    open fun executeWork(alarm: Alarm) {
        context.run {
            when (alarm.workMode) {
                AlarmConstants.WORK_MODE_DIARY_BACKUP_LOCAL -> {
                    exportRealmFile()
                    openNotification(alarm)
                }

                AlarmConstants.WORK_MODE_DIARY_WRITING -> {
                    openNotification(alarm)
                }
            }
        }
    }
}
