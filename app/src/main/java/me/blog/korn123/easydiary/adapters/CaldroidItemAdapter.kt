package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.roomorama.caldroid.CaldroidGridAdapter
import io.realm.Sort
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

class CaldroidItemAdapter(
        context: Context,
        month: Int,
        year: Int,
        caldroidData: Map<String, Any?>,
        extraData: Map<String, Any>
) : CaldroidGridAdapter(context, month, year, caldroidData, extraData) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var cellView = convertView

        if (convertView == null) {
            cellView = inflater.inflate(R.layout.fragment_custom_cell, null).apply {
                if (this is ViewGroup) {
                    context?.initTextSize(this)
                    FontUtils.setFontsTypeface(context, context.assets, null, this)
                }
            }
            Log.i(AAF_TEST, "$position: new instance")
        } else {
            Log.i(AAF_TEST, "$position: recycle instance ${this.datetimeList[position].month}")
        }
        
        val topPadding = cellView?.paddingTop ?: 0
        val leftPadding = cellView?.paddingLeft ?: 0
        val bottomPadding = cellView?.paddingBottom ?: 0
        val rightPadding = cellView?.paddingRight ?: 0

        // Get dateTime of this cell
        val dateTime = this.datetimeList[position]
        val resources = context.resources

        val calendarDate = cellView?.findViewById<TextView>(R.id.calendarDate)
        calendarDate?.run {
            //        calendarDate?.setBackgroundColor(Color.WHITE)
            setBackgroundResource(0)
            layoutParams?.width = (textSize * 2).toInt()
            layoutParams?.height = (textSize * 2).toInt()
        }
        when (dateTime.weekDay) {
            1 -> calendarDate?.setTextColor(Color.RED)
            7 -> calendarDate?.setTextColor(Color.BLUE)
            else -> calendarDate?.setTextColor(Color.BLACK)
        }

        if (dateTime.month != month) { // Set color of the dates in previous / next month
            calendarDate?.alpha = 0.5F
        } else {
            calendarDate?.alpha = 1.0F
        }

        // Customize for selected dates
        if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
            cellView?.let {
                it.setBackgroundResource(R.drawable.bg_card_cell_select)
                (it.findViewById<LinearLayout>(R.id.item1).getChildAt(1) as TextView).setTextColor(Color.BLACK)
                (it.findViewById<LinearLayout>(R.id.item2).getChildAt(1) as TextView).setTextColor(Color.BLACK)
                (it.findViewById<LinearLayout>(R.id.item3).getChildAt(1) as TextView).setTextColor(Color.BLACK)
            }
        } else {
            cellView?.let {
                it.setBackgroundColor(context.config.backgroundColor)
                (it.findViewById<LinearLayout>(R.id.item1).getChildAt(1) as TextView).setTextColor(context.config.textColor)
                (it.findViewById<LinearLayout>(R.id.item2).getChildAt(1) as TextView).setTextColor(context.config.textColor)
                (it.findViewById<LinearLayout>(R.id.item3).getChildAt(1) as TextView).setTextColor(context.config.textColor)
            }
        }

        // Today's symbol
        if (dateTime == getToday()) {
            calendarDate?.setBackgroundResource(R.drawable.bg_calendar_circle)
            calendarDate?.setTextColor(Color.WHITE)
        }

        calendarDate?.text = "${dateTime.day}"

        val dateString = dateTime.format("YYYY-MM-DD")
        val count = EasyDiaryDbHelper.countDiaryBy(dateString)

        val sort: Sort = if (context.config.calendarSorting == CALENDAR_SORTING_ASC) Sort.ASCENDING else Sort.DESCENDING
        val mDiaryList = EasyDiaryDbHelper.readDiaryByDateString(dateString, sort)
        cellView?.findViewById<TextView>(R.id.itemCount)?.run {
            setTextColor(Color.RED)
            if (count > 3) {
                text = context.getString(R.string.diary_item_count, count - 3)
            } else {
                text = null
            }
        }

        when {
            mDiaryList.isEmpty() -> {
                cellView?.findViewById<LinearLayout>(R.id.item1)?.run {
                    (getChildAt(0) as ImageView).setImageResource(0)
                    (getChildAt(1) as TextView).text = null
                }
                cellView?.findViewById<LinearLayout>(R.id.item2)?.run {
                    (getChildAt(0) as ImageView).setImageResource(0)
                    (getChildAt(1) as TextView).text = null
                }
                cellView?.findViewById<LinearLayout>(R.id.item3)?.run {
                    (getChildAt(0) as ImageView).setImageResource(0)
                    (getChildAt(1) as TextView).text = null
                }
            }
            mDiaryList.size == 1 -> {
                cellView?.findViewById<LinearLayout>(R.id.item1)?.run {
                    val item = mDiaryList[0]
                    FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                    (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                }
                cellView?.findViewById<LinearLayout>(R.id.item2)?.run {
                    (getChildAt(0) as ImageView).setImageResource(0)
                    (getChildAt(1) as TextView).text = null
                }
                cellView?.findViewById<LinearLayout>(R.id.item3)?.run {
                    (getChildAt(0) as ImageView).setImageResource(0)
                    (getChildAt(1) as TextView).text = null
                }
            }
            mDiaryList.size == 2 -> {
                cellView?.findViewById<LinearLayout>(R.id.item1)?.run {
                    val item = mDiaryList[0]
                    FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                    (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                }
                cellView?.findViewById<LinearLayout>(R.id.item2)?.run {
                    val item = mDiaryList[1]
                    FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                    (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                }
                cellView?.findViewById<LinearLayout>(R.id.item3)?.run {
                    (getChildAt(0) as ImageView).setImageResource(0)
                    (getChildAt(1) as TextView).text = null
                }
            }
            mDiaryList.size > 2 -> {
                cellView?.findViewById<LinearLayout>(R.id.item1)?.run {
                    val item = mDiaryList[0]
                    FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                    (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                }
                cellView?.findViewById<LinearLayout>(R.id.item2)?.run {
                    val item = mDiaryList[1]
                    FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                    (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                }
                cellView?.findViewById<LinearLayout>(R.id.item3)?.run {
                    val item = mDiaryList[2]
                    FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                    (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                }
            }
        }

        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        cellView?.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding)

        // Set custom color if required
        setCustomResources(dateTime, cellView, calendarDate)
        return cellView
    }
}
