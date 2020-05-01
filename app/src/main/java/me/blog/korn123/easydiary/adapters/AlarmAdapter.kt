package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.viewholders.AlarmViewHolder

internal class AlarmAdapter(
        val activity: Activity,
        private val alarmList: List<Alarm>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.viewholder_alarm, parent, false)
        return AlarmViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        if (holder.itemView is ViewGroup) {
            holder.itemView.run {
                setOnClickListener {
                    onItemClickListener.onItemClick(null, it, holder.adapterPosition, holder.itemId)
                }
                activity.initTextSize(this)
                activity.updateTextColors(this)
                activity.updateAppViews(this)
                activity.updateCardViewPolicy(this)
                FontUtils.setFontsTypeface(activity, activity.assets, null, this)
            }
        }
        holder.bindTo(alarmList[position])
    }

    override fun getItemCount() = alarmList.size
}
