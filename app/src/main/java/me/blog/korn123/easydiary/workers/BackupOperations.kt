package me.blog.korn123.easydiary.workers

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import me.blog.korn123.easydiary.helper.WORK_MANAGER_BACKUP
import me.blog.korn123.easydiary.helper.WORK_MANAGER_RECOVERY
import me.blog.korn123.easydiary.helper.WorkerConstants

internal class BackupOperations private constructor(
    val continuation: WorkContinuation,
) {
    /**
     * @param password zip password of the full backup. On [WorkerConstants.WORK_MODE_BACKUP] it encrypts the archive
     * that is written, on [WorkerConstants.WORK_MODE_RECOVERY] it opens the encrypted archive that was chosen. It is
     * handed to the worker as one-off work data and never stored anywhere else; null or blank keeps the archive
     * unencrypted.
     * @param folderUriString tree uri of a folder a scheduled full backup writes into. When it is set, [uriString] is
     * not used as the destination document and the worker names and replaces the file inside that folder itself.
     * @param fileNameMode [me.blog.korn123.easydiary.helper.AlarmConstants.BACKUP_FILE_MODE_OVERWRITE] or
     * [me.blog.korn123.easydiary.helper.AlarmConstants.BACKUP_FILE_MODE_TIMESTAMPED], read only with a folder.
     * @param alarmId and @param alarmLabel identify the schedule that started the run, so a failed run can report
     * itself as that schedule.
     */
    internal class Builder(
        private val context: Context,
        private val uriString: String,
        val workMode: String,
        private val password: String? = null,
        private val folderUriString: String? = null,
        private val fileNameMode: Int = 0,
        private val alarmId: Int = 0,
        private val alarmLabel: String? = null,
    ) {
        @SuppressLint("EnqueueWork")
        fun build(): BackupOperations {
            val data = Data.Builder()
            data.putString(WorkerConstants.URI_STRING, uriString)
            if (!password.isNullOrEmpty()) data.putString(WorkerConstants.PASSWORD_STRING, password)
            if (!folderUriString.isNullOrEmpty()) {
                data.putString(WorkerConstants.FOLDER_URI_STRING, folderUriString)
                data.putInt(WorkerConstants.FILE_NAME_MODE, fileNameMode)
                data.putInt(WorkerConstants.ALARM_ID, alarmId)
                if (!alarmLabel.isNullOrEmpty()) data.putString(WorkerConstants.ALARM_LABEL, alarmLabel)
            }
            return if (workMode == WorkerConstants.WORK_MODE_BACKUP) {
                val workRequest = OneTimeWorkRequest.Builder(FullBackupWorker::class.java)
                workRequest.setInputData(data.build())
                val continuation = WorkManager.getInstance(context).beginUniqueWork(WORK_MANAGER_BACKUP, ExistingWorkPolicy.REPLACE, workRequest.build())
                BackupOperations(continuation)
            } else {
                val workRequest = OneTimeWorkRequest.Builder(FullRecoveryWorker::class.java)
                workRequest.setInputData(data.build())
                val continuation = WorkManager.getInstance(context).beginUniqueWork(WORK_MANAGER_RECOVERY, ExistingWorkPolicy.REPLACE, workRequest.build())
                BackupOperations(continuation)
            }
        }
    }
}
