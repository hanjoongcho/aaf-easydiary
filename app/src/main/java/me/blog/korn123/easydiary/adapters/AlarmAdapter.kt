package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.getSelectedDaysString
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemAlarmBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Alarm

class AlarmAdapter(
        val activity: Activity,
        private val alarmList: List<Alarm>,
        private val onItemClickListener: AdapterView.OnItemClickListener?
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        return AlarmViewHolder(ItemAlarmBinding.inflate(activity.layoutInflater, parent, false))
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

    inner class AlarmViewHolder(
            private val viewHolderAlarmBinding: ItemAlarmBinding
    ) : RecyclerView.ViewHolder(viewHolderAlarmBinding.root), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        init {
            activity.run {
                initTextSize(viewHolderAlarmBinding.root)
                updateTextColors(viewHolderAlarmBinding.root)
                updateAppViews(viewHolderAlarmBinding.root)
                updateCardViewPolicy(viewHolderAlarmBinding.root)
                FontUtils.setFontsTypeface(this, null, viewHolderAlarmBinding.root)
            }

            viewHolderAlarmBinding.root.setOnClickListener(this@AlarmViewHolder)
            viewHolderAlarmBinding.alarmSwitch.setOnCheckedChangeListener(this@AlarmViewHolder)
        }

        fun bindTo(alarm: Alarm) {
            viewHolderAlarmBinding.run {
                alarmDays.text = activity.getSelectedDaysString(alarm.days)
                alarmDays.setTextColor(activity.config.textColor)
                alarmSwitch.isChecked = alarm.isEnabled
                alarmDescription.text = alarm.label
                editAlarmTime.text = activity.getFormattedTime(alarm.timeInMinutes * 60, false, true)

                val prefix = if (activity.config.enableDebugMode) "[${alarm.sequence}] " else ""
                alarmLabel.text = when (alarm.workMode) {
                    Alarm.WORK_MODE_DIARY_WRITING -> "${prefix}diary-writing"
                    Alarm.WORK_MODE_DIARY_BACKUP_LOCAL -> "${prefix}diary-backup-local"
                    Alarm.WORK_MODE_DIARY_BACKUP_GMS -> "${prefix}diary-backup-gms"
                    else -> "${prefix}unclassified"
                }
            }
        }

        override fun onClick(view: View?) {
            onItemHolderClick(this)
        }

        override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
            onItemCheckedChange(this.adapterPosition, p1)
        }
    }
}
