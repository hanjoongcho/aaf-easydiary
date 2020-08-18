package me.blog.korn123.easydiary.helper

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.services.FullBackupService

class AlarmWorkExecutor(context: Context) : BaseAlarmWorkExecutor(context) {

    override fun executeWork(alarm: Alarm) {
        super.executeWork(alarm)

        context.run {
            when (alarm.workMode) {
                Alarm.WORK_MODE_DIARY_BACKUP_GMS -> {
//                    executeGmsBackup(alarm)
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
                                    reExecuteGmsBackup(alarm, "The photo folder ID not valid.")
                                }
                            }
                        }
                    } ?: reExecuteGmsBackup(alarm, "Authentication token is not valid.")

                }
                else -> {}
            }
        }
    }

    private fun reExecuteGmsBackup(alarm: Alarm, errorMessage: String) {
        EasyDiaryDbHelper.insertActionLog(ActionLog("AlarmWorkExecutor", "reExecuteGmsBackup", "ERROR", errorMessage), context)
        EasyDiaryDbHelper.beginTransaction()
        alarm.retryCount = alarm.retryCount.plus(1)
        EasyDiaryDbHelper.commitTransaction()
        openSnoozeNotification(alarm)
    }

    private fun executeGmsBackup(alarm: Alarm) {
        val realmPath = EasyDiaryDbHelper.getRealmPath()
        GoogleOAuthHelper.getGoogleSignAccount(context)?.account?.let { account ->
            DriveServiceHelper(context, account).run {
                initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_REALM_FOLDER_NAME) { realmFolderId ->
                    if (realmFolderId != null) {
                        createFile(
                                realmFolderId, realmPath,
                                DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
                                EasyDiaryUtils.easyDiaryMimeType
                        ).addOnSuccessListener {
                            openNotification(alarm)
                        }.addOnFailureListener { e ->
                        }
                    } else {
                        reExecuteGmsBackup(alarm, "The realm folder ID is not valid.")
                    }
                }
            }
        }
    }
}