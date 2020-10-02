package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.item_timeline.view.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils


/**
 * Created by hanjoong on 2017-07-16.
 * Refactored code on 2019-12-25.
 *
 */

class TimelineItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val list: List<DiaryDto>
) : ArrayAdapter<DiaryDto>(context, layoutResourceId, list) {
    private var mPrimaryColor = 0
    var currentQuery: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)

        when (itemView.tag is ViewHolder) {
            true -> itemView.tag as ViewHolder
            false -> {
                val holder = ViewHolder(
                        itemView.diarySymbol, itemView.text1, itemView.title,
                        itemView.horizontalLine2, itemView.titleContainer, itemView.circle,
                        itemView.topLine, itemView.item_holder
                )
                itemView.tag = holder
                holder
            }
        }.run {
            if (mPrimaryColor == 0) {
                mPrimaryColor = context.config.primaryColor
            }
            setFontsTypeface(this)

            val diaryDto = list[position]
            if (position > 0 && StringUtils.equals(diaryDto.dateString, list[position - 1].dateString)) {
                titleContainer.visibility = View.GONE
                topLine.visibility = View.GONE
            } else {
                title.text = DateUtils.getFullPatternDate(diaryDto.currentTimeMillis)
                titleContainer.visibility = View.VISIBLE
                topLine.visibility = View.VISIBLE
            }

            FlavorUtils.initWeatherView(context, diarySymbol, diaryDto.weather, false)
            val mergedContents = when (StringUtils.isNotEmpty(diaryDto.title)) {
                true -> "${diaryDto.title}\n${diaryDto.contents}"
                false -> "${diaryDto.contents}"
            }
            textView1.text = when (diaryDto.isAllDay) {
                true -> applyBoldToDate(context.resources.getString(R.string.all_day), mergedContents)
                false -> applyBoldToDate(DateUtils.timeMillisToDateTime(diaryDto.currentTimeMillis, DateUtils.TIME_PATTERN_WITH_SECONDS), mergedContents)
            }
            item_holder.let {
                context.updateTextColors(it, 0, 0)
                context.updateAppViews(it)
                context.updateCardViewPolicy(it)
                context.initTextSize(it)
            }

            if (!currentQuery.isNullOrEmpty()) {
                if (context.config.diarySearchQueryCaseSensitive) {
                    EasyDiaryUtils.highlightString(textView1, currentQuery)
                } else {
                    EasyDiaryUtils.highlightStringIgnoreCase(textView1, currentQuery)
                }
            }

            when (context.config.enableContentsSummary) {
                true -> {
                    textView1.maxLines = context.config.summaryMaxLines.plus(1)
                    textView1.ellipsize = TextUtils.TruncateAt.valueOf("END")
                }
                false -> {
                    textView1.maxLines = Integer.MAX_VALUE
                    textView1.ellipsize = null
                }
            }
        }

        return itemView
    }

    private fun applyBoldToDate(dateString: String, summary: String): SpannableString {
        val spannableString = SpannableString("$dateString\n$summary")
        if (context.config.boldStyleEnable) spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, dateString.length, 0)
        return spannableString
    }

    private fun setFontsTypeface(holder: ViewHolder) {
        FontUtils.setFontsTypeface(context, context.assets, null, holder.item_holder)
    }

    private class ViewHolder (
            val diarySymbol: ImageView, val textView1: TextView, val title: TextView,
            val horizontalLine2: View, val titleContainer: ViewGroup, val circle: ImageView,
            val topLine: TextView, val item_holder: ViewGroup
    )
}
