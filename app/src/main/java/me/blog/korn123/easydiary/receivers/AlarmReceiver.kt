package me.blog.korn123.easydiary.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.blog.korn123.easydiary.activities.Alarm
import me.blog.korn123.easydiary.activities.isScreenOn
import me.blog.korn123.easydiary.activities.showAlarmNotification

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (context.isScreenOn()) {
            val alarm = Alarm(1000, 0, 0, isEnabled = false, vibrate = false, soundTitle = "", soundUri = "", label = "")
            context.showAlarmNotification(alarm)
        } else {

        }
    }
}