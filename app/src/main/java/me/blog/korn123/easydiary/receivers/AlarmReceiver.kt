package me.blog.korn123.easydiary.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.blog.korn123.easydiary.activities.*
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(DevActivity.ALARM_ID, -1)
        val alarm = EasyDiaryDbHelper.readAlarmBy(alarmId)
        if (context.isScreenOn()) {
            alarm?.let {
                context.showAlarmNotification(it)
            }
        } else {
            Intent(context, DiaryReminderActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.pauseLock() // Disables the active lock
                context.startActivity(this)
            }
        }
    }
}