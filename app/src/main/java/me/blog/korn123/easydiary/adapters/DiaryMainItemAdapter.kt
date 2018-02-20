package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryMainItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val list: List<DiaryDto>
) : ArrayAdapter<DiaryDto>(context, layoutResourceId, list) {
    var currentQuery: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row = convertView
        var holder: ViewHolder? = null
        if (row == null) {
            val inflater = (this.context as Activity).layoutInflater
            row = inflater.inflate(this.layoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row.findViewById(R.id.text1)
            holder.textView2 = row.findViewById(R.id.text2)
            holder.textView3 = row.findViewById(R.id.text3)
            holder.imageView = row.findViewById(R.id.weather)
            holder.item_holder = row.findViewById(R.id.item_holder)
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        val diaryDto = list[position]
        if (StringUtils.isEmpty(diaryDto.title)) {
            holder.textView1?.visibility = View.GONE
        } else {
            holder.textView1?.visibility = View.VISIBLE
        }
        holder.textView1?.text = diaryDto.title
        holder.textView2?.text = diaryDto.contents

        // highlight current query
        if (StringUtils.isNotEmpty(currentQuery)) {
            if (CommonUtils.loadBooleanPreference(context, Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE)) {
                EasyDiaryUtils.highlightString(holder.textView1, currentQuery)
                EasyDiaryUtils.highlightString(holder.textView2, currentQuery)
            } else {
                EasyDiaryUtils.highlightStringIgnoreCase(holder.textView1, currentQuery)
                EasyDiaryUtils.highlightStringIgnoreCase(holder.textView2, currentQuery)
            }

        }
        holder.textView3?.text = DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
        EasyDiaryUtils.initWeatherView(holder.imageView, diaryDto.weather)

        holder.item_holder?.let {
            context.updateTextColors(it, 0, 0)
            context.initTextSize(it, context)
        }

        FontUtils.setFontsTypeface(context, context.assets, null, holder.textView1, holder.textView2, holder.textView3)
        return row
    }

    private class ViewHolder {
        internal var textView1: TextView? = null
        internal var textView2: TextView? = null
        internal var textView3: TextView? = null
        internal var imageView: ImageView? = null
        internal var item_holder: ViewGroup? = null
    }
}
