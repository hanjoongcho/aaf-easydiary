package me.blog.korn123.easydiary.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_diary_main.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.fragments.BarChartFragment
import me.blog.korn123.easydiary.fragments.BarChartFragmentT2
import me.blog.korn123.easydiary.fragments.DashBoardCardFragment
import me.blog.korn123.easydiary.fragments.HorizontalBarChartFragment

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DashboardActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = "Dashboard"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lifetime, DashBoardCardFragment().apply {
                val args = Bundle()
                args.putString(DashBoardCardFragment.MODE_FLAG, DashBoardCardFragment.MODE_LIFETIME)
                arguments = args
            })
            commit()
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lastMonth, DashBoardCardFragment().apply {
                val args = Bundle()
                args.putString(DashBoardCardFragment.MODE_FLAG, DashBoardCardFragment.MODE_LAST_MONTH)
                arguments = args
            })
            commit()
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lastWeek, DashBoardCardFragment().apply {
                val args = Bundle()
                args.putString(DashBoardCardFragment.MODE_FLAG, DashBoardCardFragment.MODE_LAST_WEEK)
                arguments = args
            })
            commit()
        }

        val chartTitle = getString(R.string.statistics_creation_time)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.statistics1, BarChartFragment().apply {
                val args = Bundle()
                args.putString(BarChartFragment.CHART_TITLE, chartTitle)
                arguments = args
            })
            commit()
        }

        val symbolAllTitle = getString(R.string.statistics_symbol_all)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.statistics2, BarChartFragmentT2().apply {
                val args = Bundle()
                args.putString(BarChartFragment.CHART_TITLE, symbolAllTitle)
                arguments = args
            })
            commit()
        }

        val symbolTopTenTitle = getString(R.string.statistics_symbol_top_ten)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.statistics3, HorizontalBarChartFragment().apply {
                val args = Bundle()
                args.putString(BarChartFragment.CHART_TITLE, symbolTopTenTitle)
                arguments = args
            })
            commit()
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}
