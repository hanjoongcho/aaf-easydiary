package me.blog.korn123.easydiary.viewmodels

import android.content.Context
import androidx.work.*
import me.blog.korn123.easydiary.helper.WORK_MANAGER_BACKUP
import me.blog.korn123.easydiary.workers.FullBackupWorker

class BackupOperations(val continuation: WorkContinuation) {
    companion object {
        const val URI_STRING = "uri_string"
    }

    internal class Builder(private val context: Context, private val uriString: String) {
        fun build(): BackupOperations {
            val data = Data.Builder()
            data.putString(URI_STRING, uriString)
            val workRequest = OneTimeWorkRequest.Builder(FullBackupWorker::class.java)
            workRequest.setInputData(data.build())
            val continuation = WorkManager.getInstance(context).beginUniqueWork(WORK_MANAGER_BACKUP, ExistingWorkPolicy.REPLACE, workRequest.build())
            return BackupOperations(continuation)
        }
    }
}