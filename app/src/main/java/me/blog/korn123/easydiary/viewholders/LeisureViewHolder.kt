package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.viewholder_leisure.view.*
import me.blog.korn123.easydiary.activities.Leisure

class LeisureViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {
    fun bindTo(leisure: Leisure) {
        itemView.date.text = leisure.date
        itemView.dayOfMonth.text = leisure.dayOfMonth
        itemView.dayOfWeek.text = leisure.dayOfWeek
    }
}
