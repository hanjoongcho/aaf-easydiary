package me.blog.korn123.easydiary.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_diary_main.toolbar
import me.blog.korn123.commons.utils.ChartUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
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

        val symbolMap = FlavorUtils.getDiarySymbolMap(this)


        val calendar = Calendar.getInstance()
        val endMillis = calendar.timeInMillis
        calendar.add(Calendar.DATE, -30)
        val startMillis = calendar.timeInMillis


        val lastMonthFragment = DashBoardCardFragment()
        supportFragmentManager.beginTransaction().run {
            replace(R.id.lifetime, lastMonthFragment)
            commit()
        }

        val lifetimeFragment = DashBoardCardFragment()
        supportFragmentManager.beginTransaction().run {
            replace(R.id.lastMonth, lifetimeFragment)
            commit()
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}
