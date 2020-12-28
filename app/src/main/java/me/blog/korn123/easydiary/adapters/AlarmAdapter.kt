package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.getSelectedDaysString
import kotlinx.android.synthetic.main.viewholder_alarm.view.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Alarm

class AlarmAdapter(
        val activity: Activity,
        private val alarmList: List<Alarm>,
        private val onItemClickListener: AdapterView.OnItemClickListener?
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.viewholder_alarm, parent, false)
        return AlarmViewHolder(activity, view, this)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bindTo(alarmList[position])
    }

    override fun getItemCount() = alarmList.size

    fun onItemHolderClick(itemHolder: AlarmViewHolder) {
        onItemClickListener?.run {
            onItemClick(null, itemHolder.itemView, itemHolder.adapterPosition, itemHolder.itemId)
        }
    }

    fun onItemCheckedChange(position: Int, isChecked: Boolean) {
        val alarm = alarmList[position]
        EasyDiaryDbHelper.beginTransaction()
        alarm.isEnabled = isChecked
        if (isChecked) {
            activity.scheduleNextAlarm(alarm, true)
//            alarm.label = itemView.alarmDescription.text.toString()
        } else {
            activity.cancelAlarmClock(alarm)
        }
        EasyDiaryDbHelper.commitTransaction()
    }

    class AlarmViewHolder(
            val activity: Activity, itemView: View, val adapter: AlarmAdapter
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        init {
            itemView.run {
                if (itemView is ViewGroup) {
                    activity.run {
                        initTextSize(itemView)
                        updateTextColors(itemView)
                        updateAppViews(itemView)
                        updateCardViewPolicy(itemView)
                        FontUtils.setFontsTypeface(this, this.assets, null, itemView)
                    }

                    setOnClickListener(this@AlarmViewHolder)
                    alarm_switch.setOnCheckedChangeListener(this@AlarmViewHolder)
                }
            }
        }

        fun bindTo(alarm: Alarm) {
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
        }

        override fun onClick(view: View?) {
            adapter.onItemHolderClick(this)
        }

        override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
            adapter.onItemCheckedChange(this.adapterPosition, p1)
        }
    }
}
