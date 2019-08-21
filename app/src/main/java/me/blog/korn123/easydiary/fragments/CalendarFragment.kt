package me.blog.korn123.easydiary.fragments

import com.roomorama.caldroid.CaldroidFragment
import com.roomorama.caldroid.CaldroidGridAdapter

import me.blog.korn123.easydiary.adapters.CaldroidItemAdapter
import me.blog.korn123.easydiary.adapters.WeekdayArrayAdapter

class CalendarFragment : CaldroidFragment() {
    override fun getNewDatesGridAdapter(month: Int, year: Int): CaldroidGridAdapter {
        return CaldroidItemAdapter(activity!!, month, year,
                getCaldroidData(), extraData)
    }

    override fun getNewWeekdayAdapter(themeResource: Int): WeekdayArrayAdapter {
        return WeekdayArrayAdapter(
                activity!!, com.caldroid.R.layout.weekday_textview,
                daysOfWeek, themeResource)
    }
}
