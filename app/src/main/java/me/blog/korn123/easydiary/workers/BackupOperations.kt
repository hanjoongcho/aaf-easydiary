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
     */
    internal class Builder(
        private val context: Context,
        private val uriString: String,
        val workMode: String,
        private val password: String? = null,
    ) {
        @SuppressLint("EnqueueWork")
        fun build(): BackupOperations {
            val data = Data.Builder()
            data.putString(WorkerConstants.URI_STRING, uriString)
            if (!password.isNullOrEmpty()) data.putString(WorkerConstants.PASSWORD_STRING, password)
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
