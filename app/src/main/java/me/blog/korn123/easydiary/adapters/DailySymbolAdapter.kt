package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.PartialDailySymbolBinding
import me.blog.korn123.easydiary.databinding.ItemDailySymbolBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import java.util.*

class DailySymbolAdapter(
        val activity: Activity,
        private val items: List<DailySymbol>
) : RecyclerView.Adapter<DailySymbolAdapter.DailySymbolViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailySymbolViewHolder {
        return DailySymbolViewHolder(ItemDailySymbolBinding.inflate(activity.layoutInflater), activity)
    }

    override fun onBindViewHolder(holder: DailySymbolViewHolder, position: Int) {
        holder.bindTo(items[position])
    }

    override fun getItemCount() = items.size

    class DailySymbolViewHolder(private val binding: ItemDailySymbolBinding, val activity: Activity) : RecyclerView.ViewHolder(binding.root) {
        init {
            if (itemView is ViewGroup) {
                activity.run {
                    initTextSize(itemView)
                    updateTextColors(itemView)
                    updateAppViews(itemView)
                    updateCardViewPolicy(itemView)
                    FontUtils.setFontsTypeface(this, null, itemView)
                }
            }
        }

        fun bindTo(dailySymbol: DailySymbol) {
            binding.dayOfMonth.text = dailySymbol.dayOfMonth
            binding.dayOfMonth.setTextColor(activity.config.textColor)
            binding.dayOfWeek.text = dailySymbol.dayOfWeekStr.uppercase(Locale.getDefault())
            binding.dayOfWeek.setTextColor(when (dailySymbol.dayOfWeekNum) {
                Calendar.SATURDAY -> Color.rgb(0, 0, 139)
                Calendar.SUNDAY -> Color.RED
                else -> activity.config.textColor
            })
            val pair = EasyDiaryDbHelper.findDiaryByDateString(dailySymbol.dateString).partition { item ->
                activity.config.selectedSymbols.split(",").find { it.toInt() == item.weather } != null
            }

            when (pair.first.isEmpty()) {
                true -> binding.noItemMessage.visibility = View.VISIBLE
                false -> binding.noItemMessage.visibility = View.GONE
            }

            binding.symbolFlexbox.removeAllViews()
            pair.first.map { diary ->
                val partialDailySymbolBinding = PartialDailySymbolBinding.inflate(activity.layoutInflater)
                FlavorUtils.initWeatherView(activity, partialDailySymbolBinding.dailySymbol, diary.weather)
                binding.symbolFlexbox.addView(partialDailySymbolBinding.root)
            }
            activity.updateAppViews(binding.symbolFlexbox)
        }
    }

    data class DailySymbol(var dateString: String, var dayOfWeekNum:Int, var dayOfWeekStr: String, var dayOfMonth: String, var date: String)
}
