package me.blog.korn123.easydiary.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.commons.extensions.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.alarmRepository
import me.blog.korn123.easydiary.extensions.executeScheduledTask
import me.blog.korn123.easydiary.helper.DOZE_SCHEDULE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SettingConstants

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.getBooleanExtra(DOZE_SCHEDULE, false)) {
            true -> {
                context.run {
                    CoroutineScope(Dispatchers.Default).launch {
                        alarmRepository.getAllAlarms().filter { it.retryCount > 0 }.forEach { alarm ->
                            executeScheduledTask(alarm)
                            alarm.retryCount = 0
                            alarmRepository.updateAlarm(alarm)
                        }
                        toast(getString(R.string.schedule_pending_guide_message))
                    }
                }
            }

            false -> {
                CoroutineScope(Dispatchers.Default).launch {
                    val alarmId = intent.getIntExtra(SettingConstants.ALARM_ID, -1)
                    context.alarmRepository.getAlarmById(alarmId)?.let {
                        context.executeScheduledTask(it)
                    }
                }
            }
        }
    }
}
