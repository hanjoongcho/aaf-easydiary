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
                ACTION_CANCEL -> handleActionCancel()
            }
        }
    }

    private fun handleActionDismiss() {
        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.cancel(NOTIFICATION_COMPLETE_ID)
    }

    private fun handleActionSnooze() {}

    private fun handleActionCancel() {
        val recoverPhotoService = Intent(this, RecoverPhotoService::class.java)
        stopService(recoverPhotoService)
    }

    companion object {
        const val ACTION_DISMISS = "me.blog.korn123.easydiary.services.action.DISMISS";
        const val ACTION_SNOOZE = "me.blog.korn123.easydiary.services.action.SNOOZE";
        const val ACTION_CANCEL = "me.blog.korn123.easydiary.services.action.CANCEL";
    }
}