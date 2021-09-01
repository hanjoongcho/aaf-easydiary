package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy

class SymbolFilterAdapter(
        val activity: Activity,
        private val items: List<SymbolFilter>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<SymbolFilterAdapter.SymbolFilterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymbolFilterViewHolder {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.viewholder_symbol_filter, parent, false)
        return SymbolFilterViewHolder(view, activity, this)
    }

    override fun onBindViewHolder(holder: SymbolFilterViewHolder, position: Int) {
        holder.bindTo(items[position])
    }

    override fun getItemCount() = items.size

    fun onItemHolderClick(itemHolder: SymbolFilterViewHolder) {
        onItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.adapterPosition, itemHolder.itemId)
    }

    class SymbolFilterViewHolder(itemView: View, val activity: Activity, adapter: SymbolFilterAdapter) : RecyclerView.ViewHolder(itemView) {
        init {
            if (itemView is ViewGroup) {
                activity.run {
//                initTextSize(this)
//                updateTextColors(this)
                  updateAppViews(itemView)
                  updateCardViewPolicy(itemView)
//                FontUtils.setFontsTypeface(this, this.assets, null, itemView)
                }
                itemView.setOnClickListener { adapter.onItemHolderClick(this) }
            }
        }

        fun bindTo(symbolFilter: SymbolFilterAdapter.SymbolFilter) {
            itemView.run {
                FlavorUtils.initWeatherView(activity, findViewById(R.id.symbol), symbolFilter.sequence)
            }
        }
    }

    data class SymbolFilter (var sequence: Int, var isChecked: Boolean = false)
}
