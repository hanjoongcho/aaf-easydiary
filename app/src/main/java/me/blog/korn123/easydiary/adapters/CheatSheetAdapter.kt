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
import me.blog.korn123.easydiary.viewholders.CheatSheetViewHolder

internal class CheatSheetAdapter(
        val activity: Activity,
        private val items: List<CheatSheetViewHolder.CheatSheet>,
        private val onItemClickListener: AdapterView.OnItemClickListener?
) : RecyclerView.Adapter<CheatSheetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheatSheetViewHolder {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.viewholder_cheat_sheet, parent, false)
        return CheatSheetViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: CheatSheetViewHolder, position: Int) {
        if (holder.itemView is ViewGroup) {
            holder.itemView.run {
                setOnClickListener {
                    onItemClickListener?.onItemClick(null, it, position, holder.itemId)
                }
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
