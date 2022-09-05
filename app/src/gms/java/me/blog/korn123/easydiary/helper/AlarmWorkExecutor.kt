package me.blog.korn123.easydiary.helper

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import me.blog.korn123.easydiary.extensions.isScreenOn
import me.blog.korn123.easydiary.extensions.reExecuteGmsBackup
import me.blog.korn123.easydiary.extensions.scheduleNextAlarm
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.services.FullBackupService

class AlarmWorkExecutor(context: Context) : BaseAlarmWorkExecutor(context) {

    override fun executeWork(alarm: Alarm) {
        super.executeWork(alarm)

        context.run {
            when (alarm.workMode) {
                Alarm.WORK_MODE_DIARY_BACKUP_GMS -> {
//                    executeGmsBackup(alarm)
                    scheduleNextAlarm(alarm, isScreenOn())
                    GoogleOAuthHelper.getGoogleSignAccount(this)?.account?.let { account ->
                        DriveServiceHelper(this, account).run {
                            initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                                if (photoFolderId != null) {
                                    Intent(context, FullBackupService::class.java).apply {
                                        putExtra(DriveServiceHelper.WORKING_FOLDER_ID, photoFolderId)
                                        putExtra(SettingsScheduleFragment.ALARM_ID, alarm.id)
                                        ContextCompat.startForegroundService(context, this)
                                    }
                                } else {
                                    reExecuteGmsBackup(alarm, "The photo folder ID not valid.", AlarmWorkExecutor::class.java.name)
                                }
                            }
                        }
                    } ?: reExecuteGmsBackup(alarm, "Authentication token is not valid.", AlarmWorkExecutor::class.java.name)

                }
                else -> {}
            }
        }
    }
}