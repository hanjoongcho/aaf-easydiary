package me.blog.korn123.easydiary.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.easydiary.extensions.showAlarmNotification
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.ActionLog

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(SettingsScheduleFragment.ALARM_ID, -1)
        EasyDiaryDbHelper.readAlarmBy(alarmId)?.let {
            EasyDiaryDbHelper.insertActionLog(ActionLog("AlarmReceiver", "onReceive", "label", it.label), context)
            context.showAlarmNotification(it)
        }
    }
}