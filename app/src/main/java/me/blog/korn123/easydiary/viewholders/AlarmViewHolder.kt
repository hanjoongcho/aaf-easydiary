package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.app.TimePickerDialog
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.addBit
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.removeBit
import kotlinx.android.synthetic.main.viewholder_alarm.view.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.*
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Alarm
import java.util.*
import kotlin.math.pow

class AlarmViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {
    fun bindTo(alarm: Alarm) {
        updateAlarmTime(alarm)

        val dayLetters = activity.resources.getStringArray(R.array.week_day_letters).toList() as ArrayList<String>
        val dayIndexes = arrayListOf(0, 1, 2, 3, 4, 5, 6)
        itemView.edit_alarm_days_holder.removeAllViews()
        dayIndexes.forEach {
            val pow = 2.0.pow(it.toDouble()).toInt()
            val day = activity.layoutInflater.inflate(R.layout.alarm_day, itemView.edit_alarm_days_holder, false) as TextView
            day.text = dayLetters[it]

            val isDayChecked = alarm.days and pow != 0
            day.background = getProperDayDrawable(isDayChecked)

            day.setTextColor(if (isDayChecked) activity.config.backgroundColor else activity.config.textColor)
            day.setOnClickListener {
                EasyDiaryDbHelper.beginTransaction()
                val selectDay = alarm.days and pow == 0
                alarm.days = if (selectDay) {
                    alarm.days.addBit(pow)
                } else {
                    alarm.days.removeBit(pow)
                }
                day.background = getProperDayDrawable(selectDay)
                day.setTextColor(if (selectDay) activity.config.backgroundColor else activity.config.textColor)
                itemView.alarm_days.text = activity.getSelectedDaysString(alarm.days)
                EasyDiaryDbHelper.commitTransaction()
            }

            itemView.edit_alarm_days_holder.addView(day)
        }
        itemView.alarm_days.text = activity.getSelectedDaysString(alarm.days)
        itemView.alarm_days.setTextColor(activity.config.textColor)
        itemView.alarm_switch.isChecked = alarm.isEnabled
        itemView.alarmTitle.setText("Easy Diary")
        itemView.alarmDescription.setText(alarm.label)

        itemView.edit_alarm_time.setOnClickListener {
            TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                EasyDiaryDbHelper.beginTransaction()
                alarm.timeInMinutes = hourOfDay * 60 + minute
                updateAlarmTime(alarm)
                EasyDiaryDbHelper.commitTransaction()
            }, alarm.timeInMinutes / 60, alarm.timeInMinutes % 60, DateFormat.is24HourFormat(activity)).show()
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
        itemView.deleteAlarm.setOnClickListener {
            EasyDiaryDbHelper.deleteAlarm(alarm.sequence)
            (activity as DevActivity).updateAlarmList()
        }
    }

    private fun updateAlarmTime(alarm: Alarm) {
        itemView.edit_alarm_time.text = activity.getFormattedTime(alarm.timeInMinutes * 60, false, true)
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.circle_background_filled else R.drawable.circle_background_stroke
        val drawable = ContextCompat.getDrawable(activity, drawableId)
        drawable!!.applyColorFilter(activity.config.textColor)
        return drawable
    }
}
