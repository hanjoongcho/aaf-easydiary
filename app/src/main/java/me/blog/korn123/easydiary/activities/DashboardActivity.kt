package me.blog.korn123.easydiary.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_diary_main.toolbar
import me.blog.korn123.commons.utils.ChartUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.fragments.BarChartFragment
import me.blog.korn123.easydiary.fragments.DashBoardCardFragment
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import java.util.*

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

        val chartTitle = getString(R.string.statistics_creation_time)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.statistics1, BarChartFragment().apply {
                val args = Bundle()
                args.putString(BarChartFragment.CHART_TITLE, chartTitle)
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
