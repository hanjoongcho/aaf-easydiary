package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.partial_daily_symbol.view.*
import kotlinx.android.synthetic.main.viewholder_daily_symbol.view.*
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import java.util.*

class DailySymbolAdapter(
        val activity: Activity,
        private val items: List<DailySymbol>
) : RecyclerView.Adapter<DailySymbolAdapter.DailySymbolViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailySymbolViewHolder {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.viewholder_daily_symbol, parent, false)
        return DailySymbolViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: DailySymbolViewHolder, position: Int) {
        holder.bindTo(items[position])
    }

    override fun getItemCount() = items.size

    class DailySymbolViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {
        init {
            if (itemView is ViewGroup) {
                activity.run {
                    initTextSize(itemView)
                    updateTextColors(itemView)
                    updateAppViews(itemView)
                    updateCardViewPolicy(itemView)
                    FontUtils.setFontsTypeface(this, this.assets, null, itemView)
                }
            }
        }

        fun bindTo(dailySymbol: DailySymbol) {
            itemView.dayOfMonth.text = dailySymbol.dayOfMonth
            itemView.dayOfWeek.text = dailySymbol.dayOfWeekStr
            itemView.dayOfWeek.setTextColor(when (dailySymbol.dayOfWeekNum) {
                Calendar.SATURDAY -> Color.BLUE
                Calendar.SUNDAY -> Color.RED
                else -> Color.BLACK
            })
            val pair = EasyDiaryDbHelper.findDiaryByDateString(dailySymbol.dateString).partition { item ->
                activity.config.selectedSymbols.split(",").find { it.toInt() == item.weather } != null
            }

            when (pair.first.isEmpty()) {
                true -> itemView.noItemMessage.visibility = View.VISIBLE
                false -> itemView.noItemMessage.visibility = View.GONE
            }

            itemView.symbolFlexbox.removeAllViews()
            (activity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater).let {
                pair.first.map { diary ->
                    val symbolCard = it.inflate(R.layout.partial_daily_symbol, null)
                    FlavorUtils.initWeatherView(activity, symbolCard.dailySymbol, diary.weather)
                    itemView.symbolFlexbox.addView(symbolCard)
                }
            }
        }
    }

    data class DailySymbol(var dateString: String, var dayOfWeekNum:Int, var dayOfWeekStr: String, var dayOfMonth: String, var date: String)
}
