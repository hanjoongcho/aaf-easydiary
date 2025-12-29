package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.getSelectedDaysString
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ItemAlarmBinding
import me.blog.korn123.easydiary.extensions.cancelAlarmClock
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.scheduleNextAlarm
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.ui.components.AlarmCard
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SimpleText
import me.blog.korn123.easydiary.ui.theme.AppTheme

class AlarmAdapter(
        val activity: Activity,
        private val alarmList: List<Alarm>,
        private val onItemClickListener: AdapterView.OnItemClickListener?
) : RecyclerView.Adapter<AlarmAdapter.AlarmCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmCardViewHolder {
        return AlarmCardViewHolder(parent)
    }

    override fun onBindViewHolder(holder: AlarmCardViewHolder, position: Int) {
        holder.bind(alarmList[position], position)
    }

    override fun getItemCount() = alarmList.size

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

    inner class AlarmCardViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
                ItemAlarmBinding.inflate(activity.layoutInflater, parent, false).root
    ) {
        private val composeView: ComposeView = itemView.findViewById(R.id.compose_view)

        fun bind(alarm: Alarm, position: Int) {
            composeView.setContent {
                AppTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row {
                            val modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)

                            val prefix = if (activity.config.enableDebugOptionVisibleAlarmSequence) "[${alarm.sequence}] " else ""
                            val alarmTag = when (alarm.workMode) {
                                Alarm.WORK_MODE_DIARY_WRITING -> "${prefix}diary-writing"
                                Alarm.WORK_MODE_DIARY_BACKUP_LOCAL -> "${prefix}diary-backup-local"
                                Alarm.WORK_MODE_DIARY_BACKUP_GMS -> "${prefix}diary-backup-gms"
                                Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC -> "${prefix}calendar-schedule-sync"
                                else -> "${prefix}unclassified"
                            }

                            var isOn by remember { mutableStateOf(alarm.isEnabled) }
                            AlarmCard(
                                alarmTime = alarm.timeInMinutes,
                                alarmDays = activity.getSelectedDaysString(alarm.days),
                                alarmDescription =  alarm.label ?: "",
                                modifier = modifier,
                                isOn = isOn,
                                alarmTag = alarmTag,
                                checkedChangeCallback = {
                                    isOn = isOn.not()
                                    onItemCheckedChange(position, isOn)
                                }
                            ) {
                                onItemClickListener?.run {
                                    onItemClick(null, null, position, 0)
                                }
                            }
                        }

                        if (position == alarmList.size - 1) {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }

            }
        }
    }
}
