package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ViewholderCheatSheetBinding
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors

class CheatSheetAdapter(
        val activity: Activity,
        private val items: List<CheatSheet>,
        private val onItemClickListener: AdapterView.OnItemClickListener?
) : RecyclerView.Adapter<CheatSheetAdapter.CheatSheetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheatSheetViewHolder {
        return CheatSheetViewHolder(activity, ViewholderCheatSheetBinding.inflate(activity.layoutInflater, parent, false), this)
    }

    override fun onBindViewHolder(holder: CheatSheetViewHolder, position: Int) {
        holder.bindTo(items[position])
    }

    override fun getItemCount() = items.size

    fun onItemHolderClick(itemHolder: CheatSheetViewHolder) {
        onItemClickListener?.run {
            onItemClick(null, itemHolder.itemView, itemHolder.adapterPosition, itemHolder.itemId)
        }
    }

    class CheatSheetViewHolder(activity: Activity, private val viewHolderCheatSheetBinding: ViewholderCheatSheetBinding, val adapter: CheatSheetAdapter) : RecyclerView.ViewHolder(viewHolderCheatSheetBinding.root), View.OnClickListener {
        init {
            if (itemView is ViewGroup) {
                itemView.run {
                    activity.initTextSize(this)
                    activity.updateTextColors(this)
                    activity.updateAppViews(this)
                    activity.updateCardViewPolicy(this)
                    FontUtils.setFontsTypeface(activity, activity.assets, null, this)
                    setOnClickListener(this@CheatSheetViewHolder)
                }
            }
        }

        fun bindTo(cheatSheet: CheatSheet) {
            viewHolderCheatSheetBinding.textTitle.text = cheatSheet.title
            viewHolderCheatSheetBinding.textDescription.text = cheatSheet.description
        }

        override fun onClick(p0: View?) {
            adapter.onItemHolderClick(this)
        }
    }

    data class CheatSheet(val title: String, val description: String, val url: String, val forceAppendCodeBlock: Boolean = false)
}
