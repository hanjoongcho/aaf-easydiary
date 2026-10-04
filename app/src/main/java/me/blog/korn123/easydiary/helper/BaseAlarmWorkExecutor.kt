package me.blog.korn123.easydiary.helper

import android.content.Context
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.extensions.applicationScope
import me.blog.korn123.easydiary.extensions.exportRoomData
import me.blog.korn123.easydiary.extensions.openNotification
import me.blog.korn123.easydiary.domain.model.Alarm as AlarmDomain

open class BaseAlarmWorkExecutor(
    val context: Context,
) {
    open fun executeWork(alarm: AlarmDomain) {
        context.run {
            when (alarm.workMode) {
                AlarmConstants.WORK_MODE_DIARY_BACKUP_LOCAL -> {
                    applicationScope.launch {
                        exportRoomData()
                    }

                    openNotification(alarm)
                }

                AlarmConstants.WORK_MODE_DIARY_WRITING -> {
                    openNotification(alarm)
                }
            }
        }
    }
}
