package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.models.Diary
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 * Refactored code on 2019-12-26.
 *
 */

class DiaryCalendarItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val list: List<Diary>
) : ArrayAdapter<Diary>(context, layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)

        when (itemView.tag is ViewHolder) {
            true -> itemView.tag as ViewHolder
            false -> {
                val viewHolder = ViewHolder(itemView.findViewById(R.id.text1), itemView.findViewById(R.id.weather), itemView.findViewById(R.id.item_holder))
                itemView.tag = viewHolder
                viewHolder
            }
        }.run {
            FontUtils.setFontsTypeface(context, null, item_holder)
            val diaryDto = list[position]
            textView1.run {
                when (StringUtils.isNotEmpty(diaryDto.title)) {
                    true -> context.applyMarkDownPolicy(this, "${diaryDto.title}\n${diaryDto.contents}", false, arrayListOf(), true)
                    false -> context.applyMarkDownPolicy(this, "${diaryDto.contents}", false, arrayListOf(), true)
                }

                if (layoutResourceId != R.layout.item_diary_dashboard_calendar) {
                    maxLines = when (context.config.enableContentsSummary) {
                        true -> {
                            context.config.summaryMaxLines
                            //                        ellipsize = TextUtils.TruncateAt.valueOf("END")
                        }

                        false -> {
                            Integer.MAX_VALUE
                            //                        ellipsize = null
                        }
                    }
                }

                if (context.config.enableMarkdown) {
                    textView1.tag = diaryDto.sequence
                    EasyDiaryUtils.applyMarkDownEllipsize(textView1, diaryDto.sequence, 0)
                }
            }

            FlavorUtils.initWeatherView(context, imageView, diaryDto.weather)
            item_holder.let {
                context.updateTextColors(it, 0, 0)
                context.updateAppViews(it)
                context.initTextSize(it)
            }

            val cardView = item_holder.getChildAt(0)
            if (cardView is androidx.cardview.widget.CardView) {
                if (context.config.enableCardViewPolicy) {
                    cardView.useCompatPadding = true
                    cardView.cardElevation = context.dpToPixelFloatValue(2F)
                } else {
                    cardView.useCompatPadding = false
                    cardView.cardElevation = 0F
                }
            }
        }

        return itemView
    }

    private class ViewHolder(val textView1: TextView, val imageView: ImageView, val item_holder: RelativeLayout)
}
