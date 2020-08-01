package me.blog.korn123.easydiary.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.blog.korn123.easydiary.extensions.showAlarmNotification
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.DOZE_SCHEDULE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.ActionLog

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.getBooleanExtra(DOZE_SCHEDULE, false)) {
            true -> {
                EasyDiaryDbHelper.readSnoozeAlarms().forEach { alarm ->
                    EasyDiaryDbHelper.beginTransaction()
                    alarm.retryCount = 0
                    EasyDiaryDbHelper.commitTransaction()
                    context.showAlarmNotification(alarm)
                }
            }
            false -> {
                val alarmId = intent.getIntExtra(SettingsScheduleFragment.ALARM_ID, -1)
                EasyDiaryDbHelper.readAlarmBy(alarmId)?.let {
                    EasyDiaryDbHelper.insertActionLog(ActionLog("AlarmReceiver", "onReceive", "label", it.label), context)
                    context.showAlarmNotification(it)
                }
            }
        }
    }
}