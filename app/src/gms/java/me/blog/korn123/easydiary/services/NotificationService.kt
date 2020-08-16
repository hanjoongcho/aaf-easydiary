package me.blog.korn123.easydiary.services

import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import me.blog.korn123.easydiary.helper.NOTIFICATION_GMS_BACKUP_COMPLETE_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_GMS_RECOVERY_COMPLETE_ID

class NotificationService : BaseNotificationService() {
    override fun onHandleIntent(intent: Intent?) {
        super.onHandleIntent(intent)
        intent?.let {
            when (it.action) {
                ACTION_DISMISS_GMS_RECOVERY_COMPLETE -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_GMS_RECOVERY_COMPLETE_ID)
                ACTION_DISMISS_GMS_BACKUP_COMPLETE -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_GMS_BACKUP_COMPLETE_ID)
                ACTION_RECOVER_CANCEL -> handleActionRecoverCancel()
                ACTION_BACKUP_CANCEL -> handleActionBackupCancel()
            }
        }
    }

    private fun handleActionRecoverCancel() {
        val recoverPhotoService = Intent(this, RecoverPhotoService::class.java)
        stopService(recoverPhotoService)
    }

    private fun handleActionBackupCancel() {
        val recoverPhotoService = Intent(this, BackupPhotoService::class.java)
        stopService(recoverPhotoService)
    }

    companion object {
        /*FullBackupService*/
        const val ACTION_FULL_BACKUP_GMS_CANCEL = "me.blog.korn123.easydiary.services.ACTION_FULL_BACKUP_GMS_CANCEL"

        /*BackupPhotoService*/
        const val ACTION_BACKUP_CANCEL = "me.blog.korn123.easydiary.services.ACTION_BACKUP_CANCEL"
        const val ACTION_DISMISS_GMS_BACKUP_COMPLETE = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS_GMS_BACKUP_COMPLETE"

        /*RecoverPhotoService*/
        const val ACTION_RECOVER_CANCEL = "me.blog.korn123.easydiary.services.ACTION_RECOVER_CANCEL"
        const val ACTION_DISMISS_GMS_RECOVERY_COMPLETE = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS_GMS_RECOVERY_COMPLETE"
    }
}