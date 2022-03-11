package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_MONDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SATURDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SUNDAY
import me.blog.korn123.easydiary.views.CalendarItem
import java.util.*

class WeekdayArrayAdapter(context: Context, private val textViewResourceId: Int,
                               objects: List<String>, themeResource: Int) : com.roomorama.caldroid.WeekdayArrayAdapter(context, textViewResourceId, objects, themeResource) {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var mDiameter = 0
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val weekDayView = inflater.inflate(textViewResourceId, null).apply {
            val textWeekday = findViewById<CalendarItem>(R.id.text_weekday)
            // Set content
            FontUtils.setFontsTypeface(context, "", this as ViewGroup)
            context.initTextSize(this)
            textWeekday.run {
                if (mDiameter == 0) mDiameter = FontUtils.measureTextWidth(paint, "55")
                text = getItem(position)
                layoutParams?.width = mDiameter
                layoutParams?.height = mDiameter
            }

            when (context.config.calendarStartDay) {
                CALENDAR_START_DAY_SUNDAY -> {
                    when (position) {
                        0 -> textWeekday.setTextColor(Color.RED)
                        6 -> textWeekday.setTextColor(Color.BLUE)
                        else -> textWeekday.setTextColor(context.config.textColor)
                    }
                }
                CALENDAR_START_DAY_MONDAY -> {
                    when (position) {
                        5 -> textWeekday.setTextColor(Color.BLUE)
                        6 -> textWeekday.setTextColor(Color.RED)
                        else -> textWeekday.setTextColor(context.config.textColor)
                    }
                }
                CALENDAR_START_DAY_SATURDAY -> {
                    when (position) {
                        0 -> textWeekday.setTextColor(Color.BLUE)
                        1 -> textWeekday.setTextColor(Color.RED)
                        else -> textWeekday.setTextColor(context.config.textColor)
                    }
                }
            }
        }
        return weekDayView
    }
}