package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemCheatSheetBinding
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
        return CheatSheetViewHolder(activity, ItemCheatSheetBinding.inflate(activity.layoutInflater, parent, false), this)
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

    class CheatSheetViewHolder(
            activity: Activity, private val ItemCheatSheetBinding: ItemCheatSheetBinding, val adapter: CheatSheetAdapter
    ) : RecyclerView.ViewHolder(ItemCheatSheetBinding.root), View.OnClickListener {
        init {
            ItemCheatSheetBinding.run {
                activity.initTextSize(root)
                activity.updateTextColors(root)
                activity.updateAppViews(root)
                activity.updateCardViewPolicy(root)
                FontUtils.setFontsTypeface(activity, activity.assets, null, root)
                cardItem.setOnClickListener(this@CheatSheetViewHolder)
            }
        }

        fun bindTo(cheatSheet: CheatSheet) {
            ItemCheatSheetBinding.textTitle.text = cheatSheet.title
            ItemCheatSheetBinding.textDescription.text = cheatSheet.description
        }

        override fun onClick(p0: View?) {
            adapter.onItemHolderClick(this)
        }
    }

    data class CheatSheet(val title: String, val description: String, val url: String, val forceAppendCodeBlock: Boolean = false)
}
