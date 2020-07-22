package me.blog.korn123.easydiary.helper

import android.content.Context
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Alarm

class AlarmWorkExecutor(context: Context) : BaseAlarmWorkExecutor(context) {

    override fun executeWork(alarm: Alarm) {
        super.executeWork(alarm)

        context?.run {
            when (alarm.workMode) {
                Alarm.WORK_MODE_DIARY_BACKUP_GMS -> {
                    val realmPath = EasyDiaryDbHelper.getRealmPath()
                    EasyDiaryDbHelper.insertActionLog(ActionLog("AlarmWorkExecutor", "executeWork", "isValidGoogleSignAccount", GoogleOAuthHelper.isValidGoogleSignAccount(context).toString()), context)
                    GoogleOAuthHelper.getGoogleSignAccount(context)?.account?.let { account ->
                        DriveServiceHelper(context, account).run {
                            initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_REALM_FOLDER_NAME) {
                                createFile(
                                        it, realmPath,
                                        DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
                                        EasyDiaryUtils.easyDiaryMimeType
                                ).addOnSuccessListener {
                                    openNotification(alarm)
                                }.addOnFailureListener { e ->
                                    EasyDiaryDbHelper.insertActionLog(ActionLog("AlarmWorkExecutor", "executeWork", "error", e.message), context)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}