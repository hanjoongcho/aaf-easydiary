package me.blog.korn123.easydiary.helper

import GoogleAuthManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.extensions.isScreenOn
import me.blog.korn123.easydiary.extensions.openNotification
import me.blog.korn123.easydiary.extensions.reExecuteGmsBackup
import me.blog.korn123.easydiary.extensions.scheduleNextAlarm
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.services.FullBackupService

class AlarmWorkExecutor(
    context: Context,
) : BaseAlarmWorkExecutor(context) {
    private val authManager by lazy { GoogleAuthManager(context) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun executeWork(alarm: Alarm) {
        super.executeWork(alarm)

        context.run {
            when (alarm.workMode) {
                AlarmConstants.WORK_MODE_DIARY_BACKUP_GMS -> {
                    scheduleNextAlarm(alarm, isScreenOn())
                    authManager.getLastSignedInAccount()?.let { account ->
                        val alarmId = alarm.id
                        applicationScope.launch {
                            runCatching {
                                DriveServiceHelper(context, account).initDriveWorkingDirectory(
                                    GDriveConstants.AAF_EASY_DIARY_PHOTO_FOLDER_NAME,
                                )
                            }.onSuccess { photoFolderId ->
                                Intent(context, FullBackupService::class.java).apply {
                                    putExtra(
                                        GDriveConstants.WORKING_FOLDER_ID,
                                        photoFolderId,
                                    )
                                    putExtra(SettingConstants.ALARM_ID, alarmId)
                                    ContextCompat.startForegroundService(context, this)
                                }
                            }.onFailure {
                                reExecuteGmsBackup(
                                    alarm,
                                    "The photo folder ID not valid.",
                                    AlarmWorkExecutor::class.java.name,
                                )
                            }
                        }
                    }
                }

                AlarmConstants.WORK_MODE_CALENDAR_SCHEDULE_SYNC -> {
                    authManager.getCalendarCredential()?.let {
                        val calendarService =
                            authManager.getCalendarService(
                                context,
                                it,
                            )
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = calendarService.calendarList().list().execute()
                            result.items.forEach { calendar ->
                                authManager.fetchData(context, calendarService, calendar.id, null)
                            }
                        }
                        openNotification(alarm)
                    }
                }

                else -> {}
            }
        }
    }
}
