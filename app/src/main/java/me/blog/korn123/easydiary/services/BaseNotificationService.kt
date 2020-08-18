package me.blog.korn123.easydiary.services

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import me.blog.korn123.easydiary.activities.BaseDevActivity
import me.blog.korn123.easydiary.helper.*


open class BaseNotificationService(name: String = "EasyDiaryNotificationService") : IntentService(name) {
    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_DISMISS_COMPRESS -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPRESS_ID)
                ACTION_DISMISS_DECOMPRESS -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
                ACTION_DISMISS_DEV -> NotificationManagerCompat.from(applicationContext).cancel(BaseDevActivity.NOTIFICATION_ID_DEV)
                ACTION_FULL_BACKUP_CANCEL -> handleActionFullBackupCancel()
                ACTION_FULL_RECOVERY_CANCEL -> handleActionFullRecoveryCancel()
            }
        }
    }

    private fun handleActionFullBackupCancel() {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_MANAGER_BACKUP)
    }

    private fun handleActionFullRecoveryCancel() {
        WorkManager.getInstance(this).cancelUniqueWork(WORK_MANAGER_RECOVERY)
    }

    companion object {
        /*BaseDevActivity*/
        const val ACTION_DISMISS_DEV = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS_DEV"

        /*ZipHelper*/
        const val ACTION_DISMISS_COMPRESS = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS_COMPRESS"
        const val ACTION_DISMISS_DECOMPRESS = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS_DECOMPRESS"
        const val ACTION_FULL_BACKUP_CANCEL = "me.blog.korn123.easydiary.services.ACTION_FULL_BACKUP_CANCEL"
        const val ACTION_FULL_RECOVERY_CANCEL = "me.blog.korn123.easydiary.services.ACTION_FULL_RECOVERY_CANCEL"
    }
}