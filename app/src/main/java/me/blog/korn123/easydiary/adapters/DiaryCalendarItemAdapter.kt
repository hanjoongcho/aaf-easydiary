package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import io.github.aafactory.commons.extensions.updateAppViews
import io.github.aafactory.commons.extensions.updateTextColors
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryCalendarItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val list: List<DiaryDto>
) : ArrayAdapter<DiaryDto>(context, layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row = convertView
        var holder: ViewHolder?
        if (row == null) {
            val inflater = (this.context as Activity).layoutInflater
            row = inflater.inflate(this.layoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row.findViewById(R.id.text1)
            holder.imageView = row.findViewById(R.id.weather)
            holder.item_holder = row.findViewById(R.id.item_holder)
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        setFontsTypeface(holder)

        val diaryDto = list[position]
        if (StringUtils.isNotEmpty(diaryDto.title)) {
            holder.textView1?.text = diaryDto.title
        } else {
            holder.textView1?.text = StringUtils.split(diaryDto.contents, "\n")[0]
        }

        EasyDiaryUtils.initWeatherView(holder.imageView, diaryDto.weather)
        holder.item_holder?.let {
            context.updateTextColors(it, 0, 0)
            context.updateAppViews(it)
            context.initTextSize(it, context)
        }
        return row
    }

    private fun setFontsTypeface(holder: ViewHolder) {
        FontUtils.setFontsTypeface(context, context.assets, null, holder.item_holder)
    }

    private class ViewHolder {
        internal var textView1: TextView? = null
        internal var imageView: ImageView? = null
        internal var item_holder: RelativeLayout? = null
    }
}
