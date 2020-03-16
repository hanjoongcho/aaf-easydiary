package me.blog.korn123.easydiary.services

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import me.blog.korn123.easydiary.helper.*


class NotificationService(name: String = "EasyDiaryNotificationService") : IntentService(name) {
    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_DISMISS -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPLETE_ID)
                ACTION_DISMISS_COMPRESS -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPRESS_ID)
                ACTION_DISMISS_DECOMPRESS -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
                ACTION_SNOOZE -> handleActionSnooze()
                ACTION_RECOVER_CANCEL -> handleActionRecoverCancel()
                ACTION_BACKUP_CANCEL -> handleActionBackupCancel()
                ACTION_FULL_BACKUP_CANCEL -> handleActionFullBackupCancel()
                ACTION_FULL_RECOVERY_CANCEL -> handleActionFullRecoveryCancel()
            }
        }
    }

    private fun handleActionSnooze() {}

    private fun handleActionRecoverCancel() {
        val recoverPhotoService = Intent(this, RecoverPhotoService::class.java)
        stopService(recoverPhotoService)
    }

    private fun handleActionBackupCancel() {
        val recoverPhotoService = Intent(this, BackupPhotoService::class.java)
        stopService(recoverPhotoService)
    }

    private fun handleActionFullBackupCancel() {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_MANAGER_BACKUP)
    }

    private fun handleActionFullRecoveryCancel() {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_MANAGER_RECOVERY)
    }

    companion object {
        const val ACTION_DISMISS = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS"
        const val ACTION_DISMISS_COMPRESS = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS_COMPRESS"
        const val ACTION_DISMISS_DECOMPRESS = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS_DECOMPRESS"
        const val ACTION_SNOOZE = "me.blog.korn123.easydiary.services.ACTION_SNOOZE"
        const val ACTION_BACKUP_CANCEL = "me.blog.korn123.easydiary.services.ACTION_BACKUP_CANCEL"
        const val ACTION_RECOVER_CANCEL = "me.blog.korn123.easydiary.services.ACTION_RECOVER_CANCEL"
        const val ACTION_FULL_BACKUP_CANCEL = "me.blog.korn123.easydiary.services.ACTION_FULL_BACKUP_CANCEL"
        const val ACTION_FULL_RECOVERY_CANCEL = "me.blog.korn123.easydiary.services.ACTION_FULL_RECOVERY_CANCEL"
    }
}