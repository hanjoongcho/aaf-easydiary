package me.blog.korn123.easydiary.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.blog.korn123.easydiary.activities.Alarm
import me.blog.korn123.easydiary.activities.DevActivity
import me.blog.korn123.easydiary.activities.showAlarmNotification

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarm = Alarm(intent.getIntExtra(DevActivity.ALARM_ID, -1), 0, 0, isEnabled = false, vibrate = false, soundTitle = "", soundUri = "", label = "")
        context.showAlarmNotification(alarm)
    }
}