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





        supportFragmentManager.beginTransaction().run {
            replace(R.id.lifetime, DashBoardCardFragment().apply {
                val args = Bundle()
                args.putString("FLAG", "LIFETIME")
                arguments = args
            })
            commit()
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lastMonth, DashBoardCardFragment().apply {
                val args = Bundle()
                args.putString("FLAG", "LAST_MONTH")
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
