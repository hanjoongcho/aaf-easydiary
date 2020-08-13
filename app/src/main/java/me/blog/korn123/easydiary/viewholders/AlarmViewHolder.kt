package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.getSelectedDaysString
import kotlinx.android.synthetic.main.viewholder_alarm.view.*
import me.blog.korn123.easydiary.extensions.cancelAlarmClock
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getFormattedTime
import me.blog.korn123.easydiary.extensions.scheduleNextAlarm
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Alarm

class AlarmViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {
    fun bindTo(alarm: Alarm) {
        itemView.alarm_switch.setOnCheckedChangeListener(null)
        itemView.alarm_days.text = activity.getSelectedDaysString(alarm.days)
        itemView.alarm_days.setTextColor(activity.config.textColor)
        itemView.alarm_switch.isChecked = alarm.isEnabled
        itemView.alarmDescription.text = alarm.label
        itemView.edit_alarm_time.text = activity.getFormattedTime(alarm.timeInMinutes * 60, false, true)

        val prefix = if (activity.config.enableDebugMode) "[${alarm.sequence}] " else ""
        itemView.alarmLabel.text = when (alarm.workMode) {
            Alarm.WORK_MODE_DIARY_WRITING -> "${prefix}diary-writing"
            Alarm.WORK_MODE_DIARY_BACKUP_LOCAL -> "${prefix}diary-backup-local"
            Alarm.WORK_MODE_DIARY_BACKUP_GMS -> "${prefix}diary-backup-gms"
            else -> "${prefix}unclassified"
        }
        itemView.alarm_switch.setOnCheckedChangeListener { _, isChecked ->
            EasyDiaryDbHelper.beginTransaction()
            alarm.isEnabled = isChecked
            if (isChecked) {
                activity.scheduleNextAlarm(alarm, true)
                alarm.label = itemView.alarmDescription.text.toString()
            } else {
                activity.cancelAlarmClock(alarm)
            }
            EasyDiaryDbHelper.commitTransaction()
        }
    }
}
