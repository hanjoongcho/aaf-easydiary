package me.blog.korn123.easydiary.services

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.simplemobiletools.commons.extensions.toast
import me.blog.korn123.easydiary.activities.BaseDevActivity
import me.blog.korn123.easydiary.helper.NOTIFICATION_COMPRESS_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_DECOMPRESS_ID
import me.blog.korn123.easydiary.helper.WORK_MANAGER_BACKUP
import me.blog.korn123.easydiary.helper.WORK_MANAGER_RECOVERY


open class BaseNotificationService(name: String = "EasyDiaryNotificationService") : IntentService(name) {
    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_DISMISS_COMPRESS -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPRESS_ID)
                ACTION_DISMISS_DECOMPRESS -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
                ACTION_DEV_DISMISS -> NotificationManagerCompat.from(applicationContext).cancel(intent.getIntExtra(BaseDevActivity.NOTIFICATION_ID, 0))
                ACTION_DEV_TOAST -> {
                    NotificationManagerCompat.from(applicationContext).cancel(intent.getIntExtra(BaseDevActivity.NOTIFICATION_ID, 0))
                    applicationContext.toast("Notification ID: ${intent.getIntExtra(BaseDevActivity.NOTIFICATION_ID, 0)}")
                }
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
        const val ACTION_DEV_DISMISS = "me.blog.korn123.easydiary.services.ACTION_DEV_DISMISS"
        const val ACTION_DEV_TOAST = "me.blog.korn123.easydiary.services.ACTION_DEV_TOAST"

        /*ZipHelper*/
        const val ACTION_DISMISS_COMPRESS = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS_COMPRESS"
        const val ACTION_DISMISS_DECOMPRESS = "me.blog.korn123.easydiary.services.action.ACTION_DISMISS_DECOMPRESS"
        const val ACTION_FULL_BACKUP_CANCEL = "me.blog.korn123.easydiary.services.ACTION_FULL_BACKUP_CANCEL"
        const val ACTION_FULL_RECOVERY_CANCEL = "me.blog.korn123.easydiary.services.ACTION_FULL_RECOVERY_CANCEL"
    }
}