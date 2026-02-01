package me.blog.korn123.easydiary.services

import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.NOTIFICATION_GMS_BACKUP_COMPLETE_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_GMS_RECOVERY_COMPLETE_ID
import me.blog.korn123.easydiary.helper.NotificationConstants
import me.blog.korn123.easydiary.helper.SettingConstants

class NotificationService : BaseNotificationService() {
    override fun onHandleIntent(intent: Intent?) {
        super.onHandleIntent(intent)
        intent?.let { it ->
            when (it.action) {
                NotificationConstants.ACTION_PHOTO_BACKUP_GMS_DISMISS -> {
                    NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_GMS_BACKUP_COMPLETE_ID)
                }

                NotificationConstants.ACTION_PHOTO_RECOVER_GMS_DISMISS -> {
                    NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_GMS_RECOVERY_COMPLETE_ID)
                }

                NotificationConstants.ACTION_FULL_BACKUP_GMS_DISMISS -> {
                    it.getIntExtra(SettingConstants.ALARM_ID, -1).let { notificationId ->
                        if (notificationId > 0) NotificationManagerCompat.from(applicationContext).cancel(notificationId)
                    }
                }

                NotificationConstants.ACTION_PHOTO_BACKUP_GMS_CANCEL -> {
                    Intent(this, BackupPhotoService::class.java).apply {
                        stopService(this)
                    }
                }

                NotificationConstants.ACTION_PHOTO_RECOVER_GMS_CANCEL -> {
                    Intent(this, RecoverPhotoService::class.java).apply {
                        stopService(this)
                    }
                }

                NotificationConstants.ACTION_FULL_BACKUP_GMS_CANCEL -> {
                    Intent(this, FullBackupService::class.java).apply {
                        stopService(this)
                    }
                }

                else -> {}
            }
        }
    }
}
