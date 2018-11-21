package me.blog.korn123.easydiary.services

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import me.blog.korn123.easydiary.helper.NOTIFICATION_COMPLETE_ID


class NotificationService(name: String = "EasyDiaryNotificationService") : IntentService(name) {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_DISMISS == action) {
                handleActionDismiss()
            } else if (ACTION_SNOOZE == action) {
                handleActionSnooze()
            }
        }
    }

    private fun handleActionDismiss() {
        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.cancel(NOTIFICATION_COMPLETE_ID)
    }

    private fun handleActionSnooze() {}

    companion object {
        const val ACTION_DISMISS = "me.blog.korn123.easydiary.services.action.DISMISS";
        const val ACTION_SNOOZE = "me.blog.korn123.easydiary.services.action.SNOOZE";
    }
}