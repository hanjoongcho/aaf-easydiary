package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_MONDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SATURDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SUNDAY

class WeekdayArrayAdapter(context: Context, private val textViewResourceId: Int,
                               objects: List<String>, themeResource: Int) : com.roomorama.caldroid.WeekdayArrayAdapter(context, textViewResourceId, objects, themeResource) {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // To customize text size and color
        val textView = inflater.inflate(textViewResourceId, null) as TextView

        // Set content
        val item = getItem(position)
        textView.text = item
        FontUtils.setFontsTypeface(context, context.assets, "", parent)
        context.initTextSize(parent)

        when (context.config.calendarStartDay) {
            CALENDAR_START_DAY_SUNDAY -> {
                when (position) {
                    0 -> textView.setTextColor(Color.RED)
                    6 -> textView.setTextColor(Color.BLUE)
                    else -> textView.setTextColor(Color.BLACK)
                }
            }
            CALENDAR_START_DAY_MONDAY -> {
                when (position) {
                    5 -> textView.setTextColor(Color.BLUE)
                    6 -> textView.setTextColor(Color.RED)
                    else -> textView.setTextColor(Color.BLACK)
                }
            }
            CALENDAR_START_DAY_SATURDAY -> {
                when (position) {
                    0 -> textView.setTextColor(Color.BLUE)
                    1 -> textView.setTextColor(Color.RED)
                    else -> textView.setTextColor(Color.BLACK)
                }
            }
        }

        return textView
    }
}