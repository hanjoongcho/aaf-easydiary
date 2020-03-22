package me.blog.korn123.easydiary.fragments

import com.roomorama.caldroid.CaldroidFragmentEx
import com.roomorama.caldroid.CaldroidGridAdapter
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.CaldroidItemAdapter
import me.blog.korn123.easydiary.adapters.WeekdayArrayAdapter
import me.blog.korn123.easydiary.extensions.config

class CalendarFragment : CaldroidFragmentEx() {
    override fun getBackgroundColor(): Int {
        return context?.config?.backgroundColor ?: 0
    }

    override fun getNewDatesGridAdapter(month: Int, year: Int): CaldroidGridAdapter {
        return CaldroidItemAdapter(activity!!, month, year,
                getCaldroidData(), extraData)
    }

    override fun getNewWeekdayAdapter(themeResource: Int): WeekdayArrayAdapter {
        return WeekdayArrayAdapter(
                activity!!, R.layout.item_weekday,
                daysOfWeek, themeResource)
    }
}
