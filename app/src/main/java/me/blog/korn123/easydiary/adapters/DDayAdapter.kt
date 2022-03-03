package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.toast
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemDdayAddBinding
import me.blog.korn123.easydiary.databinding.ItemDdayBinding
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.models.DDay

class DDayAdapter(
    val activity: Activity,
    private val dDayItems: MutableList<DDay>

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType == 0) {
            true -> DDayViewHolder(ItemDdayBinding.inflate(activity.layoutInflater))
            false -> DDayAddViewHolder(ItemDdayAddBinding.inflate(activity.layoutInflater))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dDayItems.size == position.plus(1)) {
            true -> 1
            false -> 0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (dDayItems.size == position.plus(1)) {
            true -> (holder as DDayAddViewHolder).bindTo(dDayItems[position])
            false -> (holder as DDayViewHolder).bindTo(dDayItems[position])
        }
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
            EasyDiaryUtils.boldStringForce(activity, itemDDayBinding.remainDays)
            val diffDays = dDay.targetTimeStamp.minus(System.currentTimeMillis()).div((1000 * 60 * 60 * 24))
            itemDDayBinding.run {
                title.text = dDay.title
                targetDate.text = DateUtils.getDateStringFromTimeMillis(dDay.targetTimeStamp)
                remainDays.text = if (diffDays >= 0) "D－$diffDays" else "D＋$diffDays"
            }
        }
    }

    inner class DDayAddViewHolder(private val itemDDayAddBinding: ItemDdayAddBinding) : RecyclerView.ViewHolder(itemDDayAddBinding.root) {

        init {
            activity.run {
                itemDDayAddBinding.root.also {
                    initTextSize(it)
                    updateTextColors(it)
                    updateAppViews(it)
                    updateCardViewPolicy(it)
                    FontUtils.setFontsTypeface(this, this.assets, null, it)
                }
            }
        }

        fun bindTo(dDay: DDay) {
            itemDDayAddBinding.root.setOnClickListener { activity.toast(dDay.title ?: "") }
            itemDDayAddBinding.run {}
        }
    }

    override fun getItemCount(): Int = dDayItems.size
}