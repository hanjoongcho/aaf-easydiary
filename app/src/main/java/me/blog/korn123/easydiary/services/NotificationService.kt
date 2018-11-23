package me.blog.korn123.easydiary.services

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import me.blog.korn123.easydiary.helper.NOTIFICATION_COMPLETE_ID


class NotificationService(name: String = "EasyDiaryNotificationService") : IntentService(name) {

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_DISMISS -> handleActionDismiss()
                ACTION_SNOOZE -> handleActionSnooze()
                ACTION_RECOVER_CANCEL -> handleActionRecoverCancel()
                ACTION_BACKUP_CANCEL -> handleActionBackupCancel()
            }
        }
    }

    private fun handleActionDismiss() {
        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.cancel(NOTIFICATION_COMPLETE_ID)
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
    
    companion object {
        const val ACTION_DISMISS = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS"
        const val ACTION_SNOOZE = "me.blog.korn123.easydiary.services.ACTION_SNOOZE"
        const val ACTION_BACKUP_CANCEL = "me.blog.korn123.easydiary.services.ACTION_BACKUP_CANCEL"
        const val ACTION_RECOVER_CANCEL = "me.blog.korn123.easydiary.services.ACTION_RECOVER_CANCEL"
    }
}