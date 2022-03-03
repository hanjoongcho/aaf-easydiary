package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemDdayBinding
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.models.DDay

class DDayAdapter(
    val activity: Activity,
    private val dDayItems: MutableList<DDay>

) : RecyclerView.Adapter<DDayAdapter.DDayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DDayViewHolder {
        return DDayViewHolder(ItemDdayBinding.inflate(activity.layoutInflater))
    }

    override fun onBindViewHolder(holder: DDayViewHolder, position: Int) {
        holder.bindTo(dDayItems[position])
    }

    inner class DDayViewHolder(private val itemDDayBinding: ItemDdayBinding) : RecyclerView.ViewHolder(itemDDayBinding.root) {

        init {
            activity.run {
                initTextSize(itemDDayBinding.root)
                updateTextColors(itemDDayBinding.root)
                updateAppViews(itemDDayBinding.root)
                updateCardViewPolicy(itemDDayBinding.root)
                FontUtils.setFontsTypeface(this, this.assets, null, itemDDayBinding.root)
            }
        }

        fun bindTo(dDay: DDay) {
            itemDDayBinding.run {
                title.text = dDay.title
                targetDate.text = "2022.03.03"
                remainDays.text = "+100"
            }
        }
    }

    override fun getItemCount(): Int = dDayItems.size
}