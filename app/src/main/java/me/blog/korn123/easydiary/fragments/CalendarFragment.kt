package me.blog.korn123.easydiary.fragments

import com.roomorama.caldroid.CaldroidFragment
import com.roomorama.caldroid.CaldroidGridAdapter

import me.blog.korn123.easydiary.adapters.CaldroidItemAdapter

class CalendarFragment : CaldroidFragment() {
    override fun getNewDatesGridAdapter(month: Int, year: Int): CaldroidGridAdapter {
        // TODO Auto-generated method stub
        return CaldroidItemAdapter(activity!!, month, year,
                getCaldroidData(), extraData)
    }
}
