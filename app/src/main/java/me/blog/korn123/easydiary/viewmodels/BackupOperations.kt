package me.blog.korn123.easydiary.viewmodels

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import me.blog.korn123.easydiary.helper.WORK_MANAGER_BACKUP
import me.blog.korn123.easydiary.workers.FullBackupWorker

class BackupOperations(val continuation: WorkContinuation) {

    internal class Builder(private val mContext: Context) {

        fun build(): BackupOperations {
            var continuation = WorkManager.getInstance(mContext)
                    .beginUniqueWork(WORK_MANAGER_BACKUP,
                            ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequest.from(FullBackupWorker::class.java))

            return BackupOperations(continuation)
        }
    }
}