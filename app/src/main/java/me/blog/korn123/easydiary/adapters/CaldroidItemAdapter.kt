package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.roomorama.caldroid.CaldroidGridAdapter
import io.realm.Sort
import kotlinx.android.synthetic.main.fragment_custom_cell.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        val activity: Activity,
        month: Int,
        year: Int,
        caldroidData: Map<String, Any?>,
        extraData: Map<String, Any>
) : CaldroidGridAdapter(activity, month, year, caldroidData, extraData) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var cellView = convertView

        if (convertView == null) {
            cellView = inflater.inflate(R.layout.fragment_custom_cell, null).apply {
                if (this is ViewGroup) {
                    context?.initTextSize(this)
                    FontUtils.setFontsTypeface(context, context.assets, null, this)
                    setBackgroundColor(context.config.backgroundColor)
                }
            }
        }

        // Get dateTime of this cell
        val dateTime = this.datetimeList[position]

        val calendarDate = cellView?.findViewById<TextView>(R.id.calendarDate)?.apply {
            // Today's symbol
            if (dateTime == getToday()) {
                setBackgroundResource(R.drawable.bg_calendar_circle)
                setTextColor(Color.WHITE)
            } else {
                setBackgroundResource(0)
                when (dateTime.weekDay) {
                    1 -> setTextColor(Color.RED)
                    7 -> setTextColor(Color.BLUE)
                    else -> setTextColor(Color.BLACK)
                }
            }

            text = "${datetimeList[position].day}"
            Log.i(AAF_TEST, "AA: ${textSize}")
            layoutParams?.width = (textSize * 2).toInt()
            layoutParams?.height = (textSize * 2).toInt()
        }

        if (dateTime.month != month) { // Set color of the dates in previous / next month
            calendarDate?.alpha = 0.5F
        } else {
            calendarDate?.alpha = 1.0F
        }

        cellView?.run {
            GlobalScope.launch {
                activity.runOnUiThread {
                    val dateString = dateTime.format("YYYY-MM-DD")
                    val count = EasyDiaryDbHelper.countDiaryBy(dateString)
                    val topPadding = paddingTop ?: 0
                    val leftPadding = paddingLeft ?: 0
                    val bottomPadding = paddingBottom ?: 0
                    val rightPadding = paddingRight ?: 0

                    // Customize for selected dates
                    if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
                        setBackgroundResource(R.drawable.bg_card_cell_select)
                        (item1.getChildAt(1) as TextView).setTextColor(Color.BLACK)
                        (item2.getChildAt(1) as TextView).setTextColor(Color.BLACK)
                        (item3.getChildAt(1) as TextView).setTextColor(Color.BLACK)
                    } else {
                        setBackgroundColor(context.config.backgroundColor)
                        (item1.getChildAt(1) as TextView).setTextColor(context.config.textColor)
                        (item2.getChildAt(1) as TextView).setTextColor(context.config.textColor)
                        (item3.getChildAt(1) as TextView).setTextColor(context.config.textColor)
                    }

                    // Somehow after setBackgroundResource, the padding collapse.
                    // This is to recover the padding
                    setPadding(leftPadding, topPadding, rightPadding, bottomPadding)

                    val sort: Sort = if (context.config.calendarSorting == CALENDAR_SORTING_ASC) Sort.ASCENDING else Sort.DESCENDING
                    val mDiaryList = EasyDiaryDbHelper.readDiaryByDateString(dateString, sort)
                    findViewById<TextView>(R.id.itemCount)?.run {
                        setTextColor(Color.RED)
                        text = if (count > 3) {
                            context.getString(R.string.diary_item_count, count - 3)
                        } else {
                            null
                        }
                    }

                    when {
                        mDiaryList.isEmpty() -> {
                            item1.run {
                                (getChildAt(0) as ImageView).setImageResource(0)
                                (getChildAt(1) as TextView).text = null
                            }
                            item2.run {
                                (getChildAt(0) as ImageView).setImageResource(0)
                                (getChildAt(1) as TextView).text = null
                            }
                            item3.run {
                                (getChildAt(0) as ImageView).setImageResource(0)
                                (getChildAt(1) as TextView).text = null
                            }
                        }
                        mDiaryList.size == 1 -> {
                            item1.run {
                                val item = mDiaryList[0]
                                FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                                (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                            }
                            item2.run {
                                (getChildAt(0) as ImageView).setImageResource(0)
                                (getChildAt(1) as TextView).text = null
                            }
                            item3.run {
                                (getChildAt(0) as ImageView).setImageResource(0)
                                (getChildAt(1) as TextView).text = null
                            }
                        }
                        mDiaryList.size == 2 -> {
                            item1.run {
                                val item = mDiaryList[0]
                                FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                                (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                            }
                            item2.run {
                                val item = mDiaryList[1]
                                FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                                (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                            }
                            item3.run {
                                (getChildAt(0) as ImageView).setImageResource(0)
                                (getChildAt(1) as TextView).text = null
                            }
                        }
                        mDiaryList.size > 2 -> {
                            item1.run {
                                val item = mDiaryList[0]
                                FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                                (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                            }
                            item2.run {
                                val item = mDiaryList[1]
                                FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                                (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                            }
                            item3.run {
                                val item = mDiaryList[2]
                                FlavorUtils.initWeatherView(context, getChildAt(0) as ImageView, item.weather)
                                (getChildAt(1) as TextView).text = EasyDiaryUtils.summaryDiaryLabel(item)
                            }
                        }
                    }

                    // Set custom color if required
                    setCustomResources(dateTime, cellView, calendarDate)
                }
            }
        }
        return cellView
    }
}
