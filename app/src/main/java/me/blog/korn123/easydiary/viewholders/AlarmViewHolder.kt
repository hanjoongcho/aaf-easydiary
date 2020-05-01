package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.viewholder_alarm.view.*
import me.blog.korn123.easydiary.activities.cancelAlarmClock
import me.blog.korn123.easydiary.activities.getFormattedTime
import me.blog.korn123.easydiary.activities.getSelectedDaysString
import me.blog.korn123.easydiary.activities.scheduleNextAlarm
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Alarm

class AlarmViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {
    fun bindTo(alarm: Alarm) {
        itemView.alarm_days.text = activity.getSelectedDaysString(alarm.days)
        itemView.alarm_days.setTextColor(activity.config.textColor)
        itemView.alarm_switch.isChecked = alarm.isEnabled
        itemView.alarmTitle.text = "Easy Diary"
        itemView.alarmDescription.text = alarm.label
        itemView.edit_alarm_time.text = activity.getFormattedTime(alarm.timeInMinutes * 60, false, true)

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
        
        itemView.checkTT.setOnCheckedChangeListener { compoundButton, b ->  }
    }
}
