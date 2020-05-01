package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.viewholders.AlarmViewHolder

internal class AlarmAdapter(
        val activity: Activity,
        private val alarmList: List<Alarm>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_alarm, parent, false)
        return AlarmViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(null, it, holder.adapterPosition, holder.itemId)
        }
        holder.bindTo(alarmList[position])
    }

    override fun getItemCount() = alarmList.size
}
