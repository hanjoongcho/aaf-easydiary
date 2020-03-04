package me.blog.korn123.easydiary.viewmodels

import android.content.Context
import androidx.work.*
import me.blog.korn123.easydiary.helper.WORK_MANAGER_BACKUP
import me.blog.korn123.easydiary.helper.WORK_MANAGER_RECOVERY
import me.blog.korn123.easydiary.workers.FullBackupWorker
import me.blog.korn123.easydiary.workers.FullRecoveryWorker

class BackupOperations(val continuation: WorkContinuation) {
    companion object {
        const val URI_STRING = "uri_string"
        const val WORK_MODE_BACKUP = "work_mode_backup"
        const val WORK_MODE_RECOVERY = "work_mode_recovery"
    }

    internal class Builder(private val context: Context, private val uriString: String, val workMode: String) {
        fun build(): BackupOperations {
            val data = Data.Builder()
            data.putString(URI_STRING, uriString)
            return if (workMode == WORK_MODE_BACKUP) {
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