package me.blog.korn123.easydiary.services

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.simplemobiletools.commons.extensions.toast
import me.blog.korn123.easydiary.helper.NOTIFICATION_COMPRESS_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_DECOMPRESS_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_ID
import me.blog.korn123.easydiary.helper.NotificationConstants
import me.blog.korn123.easydiary.helper.WORK_MANAGER_BACKUP
import me.blog.korn123.easydiary.helper.WORK_MANAGER_RECOVERY

open class BaseNotificationService(
    name: String = "EasyDiaryNotificationService",
) : IntentService(name) {
    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                NotificationConstants.ACTION_DISMISS_COMPRESS -> {
                    NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPRESS_ID)
                }

                NotificationConstants.ACTION_DISMISS_DECOMPRESS -> {
                    NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
                }

                NotificationConstants.ACTION_DEV_DISMISS -> {
                    NotificationManagerCompat.from(applicationContext).cancel(intent.getIntExtra(NOTIFICATION_ID, 0))
                }

                NotificationConstants.ACTION_DEV_TOAST -> {
                    NotificationManagerCompat.from(applicationContext).cancel(intent.getIntExtra(NOTIFICATION_ID, 0))
                    applicationContext.toast("Notification ID: ${intent.getIntExtra(NOTIFICATION_ID, 0)}")
                }

                NotificationConstants.ACTION_FULL_BACKUP_CANCEL -> {
                    handleActionFullBackupCancel()
                }

                NotificationConstants.ACTION_FULL_RECOVERY_CANCEL -> {
                    handleActionFullRecoveryCancel()
                }
            }
        }
    }

    private fun handleActionFullBackupCancel() {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_MANAGER_BACKUP)
    }

    private fun handleActionFullRecoveryCancel() {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_MANAGER_RECOVERY)
    }
}
