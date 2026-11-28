package me.blog.korn123.easydiary.helper

import android.content.Context
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.extensions.applicationScope
import me.blog.korn123.easydiary.extensions.exportLegacyRealmFile
import me.blog.korn123.easydiary.extensions.exportRoomData
import me.blog.korn123.easydiary.extensions.openBackupDestinationUnavailableNotification
import me.blog.korn123.easydiary.extensions.openNotification
import me.blog.korn123.easydiary.extensions.scheduleNextAlarm
import me.blog.korn123.easydiary.utils.ExternalBackupFolder
import me.blog.korn123.easydiary.workers.BackupOperations
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

                AlarmConstants.WORK_MODE_DIARY_FULL_BACKUP_EXTERNAL -> {
                    applicationScope.launch {
                        executeExternalFullBackup(alarm)
                    }
                }

                AlarmConstants.WORK_MODE_DIARY_WRITING -> {
                    openNotification(alarm)
                }
            }
        }
    }

    /**
     * Runs the full backup of #302 into the folder the schedule keeps.
     *
     * The archive is produced by the same worker the manual `[BETA] Backup full data` uses, so both write the same
     * format and either is restored by `[BETA] Recover full data`; this only prepares the working directory the worker
     * packs and names the destination. A folder whose grant is gone, or that no longer exists, is reported through the
     * schedule's own notification instead of failing quietly, and the schedule keeps its next run either way.
     */
    private suspend fun executeExternalFullBackup(alarm: AlarmDomain) {
        val folderUri = alarm.backupFolderUri
        if (ExternalBackupFolder.status(context, folderUri) != ExternalBackupFolder.Status.READY) {
            context.openBackupDestinationUnavailableNotification(alarm.alarmId, alarm.label)
            context.scheduleNextAlarm(alarm, false)
            return
        }

        // The same two exports the manual full backup runs before FullBackupWorker packs the working directory.
        context.exportLegacyRealmFile()
        context.exportRoomData()
        BackupOperations
            .Builder(
                context = context,
                uriString = folderUri.orEmpty(),
                workMode = WorkerConstants.WORK_MODE_BACKUP,
                password = alarm.backupPassword,
                folderUriString = folderUri,
                fileNameMode = alarm.backupFileNameMode,
                alarmId = alarm.alarmId,
                alarmLabel = alarm.label,
            ).build()
            .apply {
                continuation.enqueue()
            }

        context.openNotification(alarm)
    }
}
