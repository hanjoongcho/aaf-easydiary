package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_diary_main.toolbar
import me.blog.korn123.commons.utils.ChartUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

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
        val sortedMap = ChartUtils.getSortedMapBySymbol(true)
        sortedMap.entries.forEachIndexed { index, entry ->
            when (index) {
                0 -> {
                    FlavorUtils.initWeatherView(this, symbolRank1, entry.key)
                    descriptionRank1.text = symbolMap[entry.key]
                    countRank1.text = "${entry.value}"
                }
                1 -> {
                    FlavorUtils.initWeatherView(this, symbolRank2, entry.key)
                    descriptionRank2.text = symbolMap[entry.key]
                    countRank2.text = "${entry.value}"
                }
                2 -> {
                    FlavorUtils.initWeatherView(this, symbolRank3, entry.key)
                    descriptionRank3.text = symbolMap[entry.key]
                    countRank3.text = "${entry.value}"
                }
                3 -> {
                    FlavorUtils.initWeatherView(this, symbolRank4, entry.key)
                    descriptionRank4.text = symbolMap[entry.key]
                    countRank4.text = "${entry.value}"
                }
            }
            countOfLifetime.text = EasyDiaryDbHelper.readDiary(null).size.toString()
            Log.i("aaf-t", "$index, $entry")
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}
