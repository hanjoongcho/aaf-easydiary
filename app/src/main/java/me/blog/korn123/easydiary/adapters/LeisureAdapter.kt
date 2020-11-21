package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.Leisure
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.viewholders.LeisureViewHolder

internal class LeisureAdapter(
        val activity: Activity,
        private val items: List<Leisure>,
        private val onItemClickListener: AdapterView.OnItemClickListener?
) : RecyclerView.Adapter<LeisureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeisureViewHolder {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.viewholder_leisure, parent, false)
        return LeisureViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: LeisureViewHolder, position: Int) {
        if (holder.itemView is ViewGroup) {
            holder.itemView.run {
                activity.initTextSize(this)
                activity.updateTextColors(this)
                activity.updateAppViews(this)
                activity.updateCardViewPolicy(this)
                FontUtils.setFontsTypeface(activity, activity.assets, null, this)
            }
        }
        holder.bindTo(items[position])
    }

    override fun getItemCount() = items.size
}
