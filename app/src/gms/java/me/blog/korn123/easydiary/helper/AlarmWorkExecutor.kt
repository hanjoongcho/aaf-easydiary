package me.blog.korn123.easydiary.helper

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.setScreenOrientationSensor
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.services.BackupPhotoService

class AlarmWorkExecutor(context: Context) : BaseAlarmWorkExecutor(context) {

    override fun executeWork(alarm: Alarm) {
        super.executeWork(alarm)

        context.run {
            when (alarm.workMode) {
                Alarm.WORK_MODE_DIARY_BACKUP_GMS -> {
                    executeGmsBackup(alarm)
                }
                else -> {}
            }
        }
    }

    private fun reExecuteGmsBackup(alarm: Alarm) {
        EasyDiaryDbHelper.beginTransaction()
        alarm.retryCount = alarm.retryCount.plus(1)
        EasyDiaryDbHelper.commitTransaction()
        openSnoozeNotification(alarm)
    }

    private fun executeGmsBackup(alarm: Alarm) {
        val realmPath = EasyDiaryDbHelper.getRealmPath()
        GoogleOAuthHelper.getGoogleSignAccount(context)?.account?.let { account ->
            DriveServiceHelper(context, account).run {
                initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                    if (photoFolderId != null) {
                        Intent(context, BackupPhotoService::class.java).apply {
                            putExtra(DriveServiceHelper.WORKING_FOLDER_ID, photoFolderId)
                            ContextCompat.startForegroundService(context, this)
                        }
                    } else {
                        reExecuteGmsBackup(alarm)
                    }
                }

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
                        reExecuteGmsBackup(alarm)
                    }
                }
            }
        }
    }
}