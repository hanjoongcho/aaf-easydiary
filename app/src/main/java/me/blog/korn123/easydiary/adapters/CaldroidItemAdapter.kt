package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.roomorama.caldroid.CaldroidFragment
import com.roomorama.caldroid.CaldroidGridAdapter
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

class CaldroidItemAdapter(
        context: Context,
        month: Int,
        year: Int,
        caldroidData: Map<String, Any>,
        extraData: Map<String, Any>
) : CaldroidGridAdapter(context, month, year, caldroidData, extraData) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var cellView = convertView

        // For reuse
        if (convertView == null) {
            cellView = inflater.inflate(R.layout.fragment_custom_cell, null)
        }

        if (cellView is ViewGroup) {
//            context?.updateTextColors(cellView)
            context?.initTextSize(cellView, context)
            FontUtils.setFontsTypeface(context, context.assets, null, cellView)
        }
        
        val topPadding = cellView?.paddingTop ?: 0
        val leftPadding = cellView?.paddingLeft ?: 0
        val bottomPadding = cellView?.paddingBottom ?: 0
        val rightPadding = cellView?.paddingRight ?: 0

        val tv1 = cellView?.findViewById<TextView>(R.id.tv1)
        val tv2 = cellView?.findViewById<TextView>(R.id.diaryCount)
        val imageView1 = cellView?.findViewById<ImageView>(R.id.weather)

        tv1?.setTextColor(Color.BLACK)

        // Get dateTime of this cell
        val dateTime = this.datetimeList[position]
        val resources = context.resources

        // Set color of the dates in previous / next month
        if (dateTime.month != month) {
            tv1?.setTextColor(resources
                    .getColor(com.caldroid.R.color.caldroid_darker_gray))
        }

        var shouldResetDiabledView = false
        var shouldResetSelectedView = false

        // Customize for disabled dates and date outside min/max dates
        if (minDateTime != null && dateTime.lt(minDateTime)
                || maxDateTime != null && dateTime.gt(maxDateTime)
                || disableDates != null && disableDates.indexOf(dateTime) != -1) {

            tv1?.setTextColor(CaldroidFragment.disabledTextColor)
            if (CaldroidFragment.disabledBackgroundDrawable == -1) {
                cellView?.setBackgroundResource(com.caldroid.R.drawable.disable_cell)
            } else {
                cellView?.setBackgroundResource(CaldroidFragment.disabledBackgroundDrawable)
            }

            if (dateTime == getToday()) {
                cellView?.setBackgroundResource(com.caldroid.R.drawable.red_border_gray_bg)
            }

        } else {
            shouldResetDiabledView = true
        }

        // Customize for selected dates
        if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
            cellView?.setBackgroundResource(R.drawable.bg_card_cell_select_selector)

            tv1?.setTextColor(Color.BLACK)

        } else {
            shouldResetSelectedView = true
        }

        if (shouldResetDiabledView && shouldResetSelectedView) {
            // Customize for today
            if (dateTime == getToday()) {
                cellView?.setBackgroundResource(R.drawable.bg_card_cell_today_selector)
            } else {
                cellView?.setBackgroundResource(R.drawable.bg_card_cell_default)
            }
        }

        tv1?.text = "${dateTime.day}"

        val dateString = dateTime.format("YYYY-MM-DD")
        val count = EasyDiaryDbHelper.countDiaryBy(dateString)

        val mDiaryList = EasyDiaryDbHelper.readDiaryByDateString(dateString)
        var initWeather = false
        if (mDiaryList.size > 0) {
            for (diaryDto in mDiaryList) {
                if (diaryDto.weather > 0) {
                    initWeather = true
                    EasyDiaryUtils.initWeatherView(imageView1, diaryDto.weather)
                    break
                }
            }
            if (!initWeather) {
                imageView1?.visibility = View.GONE
                imageView1?.setImageResource(0)
            }
        } else {
            imageView1?.visibility = View.GONE
            imageView1?.setImageResource(0)
        }

        if (count > 0) {
            tv2?.text = count.toString() + parent.resources.getString(R.string.diary_count)
            tv2?.setTextColor(parent.resources.getColor(R.color.diaryCountText))
        } else {
            tv2?.text = null
        }
        // Somehow after setBackgroundResource, the padding collapse.
        // This is to recover the padding
        cellView?.setPadding(leftPadding, topPadding, rightPadding,
                bottomPadding)

        // Set custom color if required
        setCustomResources(dateTime, cellView, tv1)

        return cellView
    }
}
