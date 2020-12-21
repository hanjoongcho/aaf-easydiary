package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_weekday.view.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_MONDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SATURDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SUNDAY
import java.util.*

class WeekdayArrayAdapter(context: Context, private val textViewResourceId: Int,
                               objects: List<String>, themeResource: Int) : com.roomorama.caldroid.WeekdayArrayAdapter(context, textViewResourceId, objects, themeResource) {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val weekDayView = inflater.inflate(textViewResourceId, null).apply {
            // Set content
            FontUtils.setFontsTypeface(context, context.assets, "", this as ViewGroup)
            context.initTextSize(this)
            text_weekday.run {
                text = getItem(position)
                if (Locale.getDefault().language.matches("ko|ja".toRegex())) {
                    layoutParams?.width = (textSize * 2).toInt()
                    layoutParams?.height = (textSize * 2).toInt()
                }
            }

            when (context.config.calendarStartDay) {
                CALENDAR_START_DAY_SUNDAY -> {
                    when (position) {
                        0 -> text_weekday.setTextColor(Color.RED)
                        6 -> text_weekday.setTextColor(Color.BLUE)
                        else -> text_weekday.setTextColor(Color.BLACK)
                    }
                }
                CALENDAR_START_DAY_MONDAY -> {
                    when (position) {
                        5 -> text_weekday.setTextColor(Color.BLUE)
                        6 -> text_weekday.setTextColor(Color.RED)
                        else -> text_weekday.setTextColor(Color.BLACK)
                    }
                }
                CALENDAR_START_DAY_SATURDAY -> {
                    when (position) {
                        0 -> text_weekday.setTextColor(Color.BLUE)
                        1 -> text_weekday.setTextColor(Color.RED)
                        else -> text_weekday.setTextColor(Color.BLACK)
                    }
                }
            }
        }
        return weekDayView
    }
}