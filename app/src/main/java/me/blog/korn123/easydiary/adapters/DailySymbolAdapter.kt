package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemDailySymbolBinding
import me.blog.korn123.easydiary.databinding.PartialDailySymbolBinding
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import java.util.Calendar
import java.util.Locale

class DailySymbolAdapter(
    val activity: Activity,
    private val items: List<DailySymbol>,
) : RecyclerView.Adapter<DailySymbolAdapter.DailySymbolViewHolder>() {
    val map: MutableMap<String, Pair<List<Diary>, List<Diary>>> = mutableMapOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DailySymbolViewHolder = DailySymbolViewHolder(ItemDailySymbolBinding.inflate(activity.layoutInflater), activity, map)

    override fun onBindViewHolder(
        holder: DailySymbolViewHolder,
        position: Int,
    ) {
        holder.bindTo(items[position])
    }

    override fun getItemCount() = items.size

    class DailySymbolViewHolder(
        private val binding: ItemDailySymbolBinding,
        val activity: Activity,
        val map: Map<String, Pair<List<Diary>, List<Diary>>>,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            if (itemView is ViewGroup) {
                activity.run {
                    initTextSize(itemView as ViewGroup)
                    updateTextColors(itemView as ViewGroup)
                    updateAppViews(itemView as ViewGroup)
                    updateCardViewPolicy(itemView as ViewGroup)
                    FontUtils.setFontsTypeface(this, null, itemView as ViewGroup)
                }
            }
        }

        fun bindTo(dailySymbol: DailySymbol) {
            binding.dayOfMonth.text = dailySymbol.dayOfMonth
            binding.dayOfMonth.setTextColor(activity.config.textColor)
            binding.dayOfWeek.text = dailySymbol.dayOfWeekStr.uppercase(Locale.getDefault())
            binding.dayOfWeek.setTextColor(
                when (dailySymbol.dayOfWeekNum) {
                    Calendar.SATURDAY -> Color.rgb(0, 0, 139)
                    Calendar.SUNDAY -> Color.RED
                    else -> activity.config.textColor
                },
            )
            val pair = map[dailySymbol.dateString] ?: Pair(emptyList(), emptyList())

            when (pair.first.isEmpty()) {
                true -> binding.noItemMessage.visibility = View.VISIBLE
                false -> binding.noItemMessage.visibility = View.GONE
            }

            binding.symbolFlexbox.removeAllViews()
            pair.first.map { diary ->
                val partialDailySymbolBinding = PartialDailySymbolBinding.inflate(activity.layoutInflater)
                FlavorUtils.initWeatherView(activity, partialDailySymbolBinding.dailySymbol, diary.symbolSequence)
                binding.symbolFlexbox.addView(partialDailySymbolBinding.root)
            }
            activity.updateAppViews(binding.symbolFlexbox)
        }
    }

    data class DailySymbol(
        var dateString: String,
        var dayOfWeekNum: Int,
        var dayOfWeekStr: String,
        var dayOfMonth: String,
        var date: String,
    )
}
