package me.blog.korn123.easydiary.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.blog.korn123.easydiary.activities.rescheduleEnabledAlarms

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.rescheduleEnabledAlarms()
    }
}