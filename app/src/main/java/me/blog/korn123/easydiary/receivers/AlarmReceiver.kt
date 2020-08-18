package me.blog.korn123.easydiary.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.commons.extensions.toast
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.executeScheduledTask
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.DOZE_SCHEDULE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.getBooleanExtra(DOZE_SCHEDULE, false)) {
            true -> {
                context.run {
                    EasyDiaryDbHelper.readSnoozeAlarms().forEach { alarm ->
                        EasyDiaryDbHelper.beginTransaction()
                        alarm.retryCount = 0
                        EasyDiaryDbHelper.commitTransaction()
                        executeScheduledTask(alarm)
                    }
                    toast(getString(R.string.schedule_pending_guide_message))
                }
            }
            false -> {
                val alarmId = intent.getIntExtra(SettingsScheduleFragment.ALARM_ID, -1)
                EasyDiaryDbHelper.readAlarmBy(alarmId)?.let {
                    context.executeScheduledTask(it)
                }
            }
        }
    }
}