package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.Leisure
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
        holder.bindTo(items[position])
    }

    override fun getItemCount() = items.size
}
