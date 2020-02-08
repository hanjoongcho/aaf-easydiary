package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.WorkManager
import me.blog.korn123.easydiary.helper.WORK_MANAGER_BACKUP

class BackupViewModel(application: Application) : AndroidViewModel(application) {
    private val mWorkManager: WorkManager = WorkManager.getInstance(application)

    fun apply(backupOperations: BackupOperations) {
        backupOperations.continuation.enqueue()
    }

    fun cancel() {
        mWorkManager.cancelUniqueWork(WORK_MANAGER_BACKUP)
    }
}