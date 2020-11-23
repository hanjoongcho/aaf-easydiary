package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.viewholder_symbol_filter.view.*
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.adapters.SymbolFilterAdapter

class SymbolFilterViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {
    fun bindTo(symbolFilter: SymbolFilterAdapter.SymbolFilter) {
        FlavorUtils.initWeatherView(activity, itemView.symbol, symbolFilter.sequence)
    }
}
