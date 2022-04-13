package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ItemTimelineBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.models.Diary
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat


/**
 * Created by hanjoong on 2017-07-16.
 * Refactored code on 2019-12-25.
 *
 */

class TimelineItemAdapter(
        private val activity: Activity,
        layoutResourceId: Int,
        private val list: List<Diary>
) : ArrayAdapter<Diary>(activity, layoutResourceId, list) {
    private lateinit var itemTimelineBinding: ItemTimelineBinding
    private var mPrimaryColor = 0
    var currentQuery: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: run {
            itemTimelineBinding = ItemTimelineBinding.inflate(activity.layoutInflater)
            itemTimelineBinding.root
        }

        when (itemView.tag is ItemTimelineBinding) {
            true -> itemView.tag as ItemTimelineBinding
            false -> {
                val holder = itemTimelineBinding
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
                title.text = DateUtils.getDateStringFromTimeMillis(diaryDto.currentTimeMillis)
                titleContainer.visibility = View.VISIBLE
                topLine.visibility = View.VISIBLE
            }

            FlavorUtils.initWeatherView(context, diarySymbol, diaryDto.weather, false)
            var mergedContents = when (StringUtils.isNotEmpty(diaryDto.title)) {
                true -> "${diaryDto.title}\n${diaryDto.contents}"
                false -> "${diaryDto.contents}"
            }
            if (context.config.enableDebugMode) mergedContents = "[${diaryDto.originSequence}] $mergedContents"
            text1.text = when (diaryDto.isAllDay) {
                true -> applyBoldToDate(context.resources.getString(R.string.all_day), mergedContents)
//                false -> applyBoldToDate(DateUtils.timeMillisToDateTime(diaryDto.currentTimeMillis, DateUtils.TIME_PATTERN_WITH_SECONDS), mergedContents)
                false -> applyBoldToDate(DateUtils.getTimeStringFromTimeMillis(diaryDto.currentTimeMillis, SimpleDateFormat.MEDIUM), mergedContents)
            }
            itemHolder.let {
                context.updateTextColors(it, 0, 0)
                context.updateAppViews(it)
                context.updateCardViewPolicy(it)
                context.initTextSize(it)
            }

            if (!currentQuery.isNullOrEmpty()) {
                if (context.config.diarySearchQueryCaseSensitive) {
                    EasyDiaryUtils.highlightString(text1, currentQuery)
                } else {
                    EasyDiaryUtils.highlightStringIgnoreCase(text1, currentQuery)
                }
            }

            when (context.config.enableContentsSummary) {
                true -> {
                    text1.maxLines = context.config.summaryMaxLines.plus(1)
                    text1.ellipsize = TextUtils.TruncateAt.valueOf("END")
                }
                false -> {
                    text1.maxLines = Integer.MAX_VALUE
                    text1.ellipsize = null
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

    private fun setFontsTypeface(itemTimelineBinding: ItemTimelineBinding) {
        FontUtils.setFontsTypeface(context, null, itemTimelineBinding.root)
    }
}
