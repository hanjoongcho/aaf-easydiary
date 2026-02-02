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
    internal class Builder(
        private val context: Context,
        private val uriString: String,
        val workMode: String,
    ) {
        @SuppressLint("EnqueueWork")
        fun build(): BackupOperations {
            val data = Data.Builder()
            data.putString(WorkerConstants.URI_STRING, uriString)
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
