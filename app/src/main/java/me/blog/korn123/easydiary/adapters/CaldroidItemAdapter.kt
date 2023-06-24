package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.roomorama.caldroid.CaldroidGridAdapter
import hirondelle.date4j.DateTime
import io.realm.Sort
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.FragmentCustomCellBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.makeToast
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
    var mDiameter = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val itemView: View = convertView ?: run {
            val binding = FragmentCustomCellBinding.inflate(activity.layoutInflater)
            binding.root.apply {
                tag = binding
                context?.initTextSize(this)
                FontUtils.setFontsTypeface(context, null, this)
                setBackgroundColor(context.config.backgroundColor)
            }
        }

        val binding = itemView.tag as FragmentCustomCellBinding

        // Get dateTime of this cell
        val dateTime = this.datetimeList[position]
        val calendarDate = binding.calendarDate.apply {
//            changeDateColor(binding, dateTime)
            text = datetimeList[position].day.toString()
            if (mDiameter == 0) mDiameter = FontUtils.measureTextWidth(activity , paint, "22", 1.3F)
            layoutParams?.width = mDiameter
            layoutParams?.height = mDiameter
        }

        if (dateTime.month != month) { // Set color of the dates in previous / next month
            calendarDate.alpha = 0.5F
        } else {
            calendarDate.alpha = 1.0F
        }

        binding.run {
            item1.tag = dateTime.format("YYYY-MM-DD")
            CoroutineScope(Dispatchers.IO).launch {
                delay(200)
                withContext(Dispatchers.Main) {
                    val dateString = dateTime.format("YYYY-MM-DD")
                    if (!item1.tag.equals(dateString)) {
                        cancel()
                    }
                    val count = EasyDiaryDbHelper.countDiaryBy(dateString)
                    val topPadding = root.paddingTop
                    val leftPadding = root.paddingLeft
                    val bottomPadding = root.paddingBottom
                    val rightPadding = root.paddingRight

                    // Customize for selected dates
                    if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
                        changeDateColor(binding, dateTime, true)
                        root.setBackgroundResource(R.drawable.bg_card_cell_select)
                        (item1.getChildAt(1) as TextView).setTextColor(Color.BLACK)
                        (item2.getChildAt(1) as TextView).setTextColor(Color.BLACK)
                        (item3.getChildAt(1) as TextView).setTextColor(Color.BLACK)
                    } else {
                        changeDateColor(binding, dateTime)
                        root.setBackgroundColor(context.config.backgroundColor)
                        (item1.getChildAt(1) as TextView).setTextColor(context.config.textColor)
                        (item2.getChildAt(1) as TextView).setTextColor(context.config.textColor)
                        (item3.getChildAt(1) as TextView).setTextColor(context.config.textColor)
                    }

                    // Somehow after setBackgroundResource, the padding collapse.
                    // This is to recover the padding
                    root.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)

                    val sort: Sort = if (context.config.calendarSorting == CALENDAR_SORTING_ASC) Sort.ASCENDING else Sort.DESCENDING
                    val mDiaryList = EasyDiaryDbHelper.findDiaryByDateString(dateString, sort)
                    itemCount.run {
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
                                (getChildAt(1) as TextView).text = " "
                            }
                            item2.run {
                                (getChildAt(0) as ImageView).setImageResource(0)
                                (getChildAt(1) as TextView).text = " "
                            }
                            item3.run {
                                (getChildAt(0) as ImageView).setImageResource(0)
                                (getChildAt(1) as TextView).text = " "
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
                                (getChildAt(1) as TextView).text = " "
                            }
                            item3.run {
                                (getChildAt(0) as ImageView).setImageResource(0)
                                (getChildAt(1) as TextView).text = " "
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
                                (getChildAt(1) as TextView).text = " "
                            }
                        }
                        else -> {
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
                    setCustomResources(dateTime, itemView, calendarDate)
                }
            }
        }
        return itemView
    }

    private fun changeDateColor(binding: FragmentCustomCellBinding, dateTime: DateTime, isSelect: Boolean = false) {
        binding.calendarDate.run {
            if (dateTime == getToday()) {
                setBackgroundResource(R.drawable.bg_calendar_circle)
                setTextColor(Color.WHITE)
            } else {
                setBackgroundResource(0)
                when (dateTime.weekDay) {
                    1 -> setTextColor(Color.RED)
                    7 -> setTextColor(Color.rgb(0, 0, 139))
                    else -> {
                        if (isSelect) setTextColor(Color.BLACK) else setTextColor(context.config.textColor)
                    }
                }
            }
        }
    }
}
