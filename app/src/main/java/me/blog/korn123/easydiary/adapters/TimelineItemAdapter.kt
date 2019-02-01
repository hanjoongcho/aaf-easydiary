package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import io.github.aafactory.commons.extensions.updateAppViews
import io.github.aafactory.commons.extensions.updateTextColors
import io.github.aafactory.commons.helpers.BaseConfig
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils


/**
 * Created by hanjoong on 2017-07-16.
 */

class TimelineItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val list: List<DiaryDto>
) : ArrayAdapter<DiaryDto>(context, layoutResourceId, list) {
    private var mPrimaryColor = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val holder: ViewHolder? 
        if (row == null) {
            val inflater = (this.context as Activity).layoutInflater
            row = inflater.inflate(this.layoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row!!.findViewById(R.id.text1)
            holder.title = row.findViewById(R.id.title)
            holder.horizontalLine2 = row.findViewById(R.id.horizontalLine2)
            holder.titleContainer = row.findViewById(R.id.titleContainer)
            holder.weather = row.findViewById(R.id.weather)
            holder.circle = row.findViewById(R.id.circle)
            holder.topLine = row.findViewById(R.id.topLine)
            holder.item_holder = row.findViewById<ViewGroup>(R.id.item_holder) 
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        if (mPrimaryColor == 0) {
            mPrimaryColor = BaseConfig(context).primaryColor
        }
        holder.titleContainer?.setBackgroundColor(mPrimaryColor)
        //        GradientDrawable drawable = (GradientDrawable) holder.circle.getDrawable();
        //        drawable.setColor(mPrimaryColor);

        setFontsTypeface(holder)

        val diaryDto = list[position]
        if (position > 0 && StringUtils.equals(diaryDto.dateString, list[position - 1].dateString)) {
            holder.titleContainer!!.visibility = View.GONE
            holder.topLine?.visibility = View.GONE
            holder.weather?.setImageResource(0)
        } else {
            //            holder.title.setText(diaryDto.getDateString() + " " + DateUtils.timeMillisToDateTime(diaryDto.getCurrentTimeMillis(), "EEEE"));
            holder.title?.text = DateUtils.getFullPatternDate(diaryDto.currentTimeMillis)
            holder.titleContainer?.visibility = View.VISIBLE
            holder.topLine?.visibility = View.VISIBLE
            // 현재 날짜의 목록을 조회
            val mDiaryList = EasyDiaryDbHelper.readDiaryByDateString(diaryDto.dateString)
            var initWeather = false
            if (mDiaryList.isNotEmpty()) {
                for (temp in mDiaryList) {
                    if (temp.weather > 0) {
                        initWeather = true
                        EasyDiaryUtils.initWeatherView(context, holder.weather, temp.weather)
                        break
                    }
                }
                if (!initWeather) {
                    holder.weather?.visibility = View.GONE
                    holder.weather?.setImageResource(0)
                }
            } else {
                holder.weather?.visibility = View.GONE
                holder.weather?.setImageResource(0)
            }
        }

        holder.textView1?.text = when (diaryDto.isAllDay) {
            true -> applyBoldToDate(context.resources.getString(R.string.all_day), getSummary(diaryDto) ?: "")
            false -> applyBoldToDate(DateUtils.timeMillisToDateTime(diaryDto.currentTimeMillis, DateUtils.TIME_PATTERN_WITH_SECONDS), getSummary(diaryDto)!!) 
        }
        holder.item_holder?.let {
            context.updateTextColors(it, 0, 0)
            context.updateAppViews(it)
            context.initTextSize(it, context)
        }
        return row
    }

    private fun applyBoldToDate(dateString: String, summary: String): SpannableString {
        val spannableString = SpannableString("$dateString\n$summary")
        if (context.config.boldStyleEnable) spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, dateString.length, 0)
        return spannableString
    }
    
    private fun getSummary(diaryDto: DiaryDto): String? = when (context.config.enableContentsSummary) {
        true -> {
            when (StringUtils.isNotEmpty(diaryDto.title)) {
                true -> diaryDto.title
                false -> StringUtils.abbreviate(diaryDto.contents, 10)
            }
        }
        false -> {
            when (StringUtils.isNotEmpty(diaryDto.title)) {
                true -> "${diaryDto.title}\n${diaryDto.contents}"
                false -> "${diaryDto.contents}" 
            }
        }
    }

    private fun setFontsTypeface(holder: ViewHolder) {
        FontUtils.setFontsTypeface(context, context.assets, null, holder.item_holder)
    }

    private class ViewHolder {
        internal var textView1: TextView? = null
        internal var title: TextView? = null
        internal var horizontalLine2: View? = null
        internal var titleContainer: ViewGroup? = null
        internal var weather: ImageView? = null
        internal var circle: ImageView? = null
        internal var topLine: TextView? = null
        internal var item_holder: ViewGroup? = null
    }
}
