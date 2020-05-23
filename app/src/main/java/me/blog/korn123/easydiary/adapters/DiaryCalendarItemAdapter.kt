package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import io.github.aafactory.commons.utils.CommonUtils
import kotlinx.android.synthetic.main.item_diary_calendar.view.*
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 * Refactored code on 2019-12-26.
 *
 */

class DiaryCalendarItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val list: List<DiaryDto>
) : ArrayAdapter<DiaryDto>(context, layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)

        when (itemView.tag is ViewHolder) {
            true -> itemView.tag as ViewHolder
            false -> {
                val viewHolder = ViewHolder(itemView.text1, itemView.weather, itemView.item_holder)
                itemView.tag = viewHolder
                viewHolder
            }
        }.run {
            FontUtils.setFontsTypeface(context, context.assets, null, item_holder)
            val diaryDto = list[position]
            textView1.run {
                text = when (StringUtils.isNotEmpty(diaryDto.title)) {
                    true -> "${diaryDto.title}\n${diaryDto.contents}"
                    false -> "${diaryDto.contents}"
                }

                when (context.config.enableContentsSummary) {
                    true -> {
                        maxLines = context.config.summaryMaxLines
                        ellipsize = TextUtils.TruncateAt.valueOf("END")
                    }
                    false -> {
                        maxLines = Integer.MAX_VALUE
                        ellipsize = null
                    }
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
                    cardView.cardElevation = CommonUtils.dpToPixelFloatValue(context, 2F)
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
