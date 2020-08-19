package me.blog.korn123.easydiary.services

import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.NOTIFICATION_GMS_BACKUP_COMPLETE_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_GMS_RECOVERY_COMPLETE_ID

class NotificationService : BaseNotificationService() {
    override fun onHandleIntent(intent: Intent?) {
        super.onHandleIntent(intent)
        intent?.let { it ->
            when (it.action) {
                ACTION_PHOTO_BACKUP_GMS_DISMISS -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_GMS_BACKUP_COMPLETE_ID)
                ACTION_PHOTO_RECOVER_GMS_DISMISS -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_GMS_RECOVERY_COMPLETE_ID)
                ACTION_FULL_BACKUP_GMS_DISMISS -> {
                    it.getIntExtra(SettingsScheduleFragment.ALARM_ID, -1).let { notificationId ->
                        if (notificationId > 0) NotificationManagerCompat.from(applicationContext).cancel(notificationId)
                    }
                }
                ACTION_PHOTO_BACKUP_GMS_CANCEL -> {
                    Intent(this, BackupPhotoService::class.java).apply {
                        stopService(this)
                    }
                }
                ACTION_PHOTO_RECOVER_GMS_CANCEL -> {
                    Intent(this, RecoverPhotoService::class.java).apply {
                        stopService(this)
                    }
                }
                ACTION_FULL_BACKUP_GMS_CANCEL -> {
                    Intent(this, FullBackupService::class.java).apply {
                        stopService(this)
                    }
                }
                else -> {}
            }
        }
    }

    companion object {
        /*FullBackupService*/
        const val ACTION_FULL_BACKUP_GMS_CANCEL  = "me.blog.korn123.easydiary.services.ACTION_FULL_BACKUP_GMS_CANCEL"
        const val ACTION_FULL_BACKUP_GMS_DISMISS = "me.blog.korn123.easydiary.services.ACTION_FULL_BACKUP_GMS_DISMISS"

        /*BackupPhotoService*/
        const val ACTION_PHOTO_BACKUP_GMS_CANCEL  = "me.blog.korn123.easydiary.services.ACTION_PHOTO_BACKUP_GMS_CANCEL"
        const val ACTION_PHOTO_BACKUP_GMS_DISMISS = "me.blog.korn123.easydiary.services.ACTION_PHOTO_BACKUP_GMS_DISMISS"

        /*RecoverPhotoService*/
        const val ACTION_PHOTO_RECOVER_GMS_CANCEL  = "me.blog.korn123.easydiary.services.ACTION_PHOTO_RECOVER_GMS_CANCEL"
        const val ACTION_PHOTO_RECOVER_GMS_DISMISS = "me.blog.korn123.easydiary.services.ACTION_PHOTO_RECOVER_GMS_DISMISS"
    }
}