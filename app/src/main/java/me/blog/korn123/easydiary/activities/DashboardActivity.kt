package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_dev.*
import kotlinx.android.synthetic.main.activity_diary_main.toolbar
import kotlinx.android.synthetic.main.layout_daily_symbol_s.view.*
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DailySymbolAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getLayoutLayoutInflater
import me.blog.korn123.easydiary.fragments.*
import me.blog.korn123.easydiary.helper.TransitionHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DashboardActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mDailySymbolAdapter: DailySymbolAdapter
    private var mDailySymbolList: ArrayList<Leisure> = arrayListOf()

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
            replace(R.id.summary, DashBoardSummaryFragment())
            commit()
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lifetime, DashBoardRankFragment().apply {
                val args = Bundle()
                args.putString(DashBoardRankFragment.MODE_FLAG, DashBoardRankFragment.MODE_LIFETIME)
                arguments = args
            })
            commit()
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lastMonth, DashBoardRankFragment().apply {
                val args = Bundle()
                args.putString(DashBoardRankFragment.MODE_FLAG, DashBoardRankFragment.MODE_LAST_MONTH)
                arguments = args
            })
            commit()
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lastWeek, DashBoardRankFragment().apply {
                val args = Bundle()
                args.putString(DashBoardRankFragment.MODE_FLAG, DashBoardRankFragment.MODE_LAST_WEEK)
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

        initializeDailySymbol()
        editSymbolFilter.setOnClickListener {
            TransitionHelper.startActivityWithTransition(this, Intent(this, SymbolFilterPickerActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        mDailySymbolAdapter.notifyDataSetChanged()
        selectedSymbolFlexBox.removeAllViews()
        config.selectedSymbols.split(",").map { sequence ->
            val symbolCard = getLayoutLayoutInflater().inflate(R.layout.layout_daily_symbol_s, null)
            FlavorUtils.initWeatherView(this, symbolCard.dailySymbol, sequence.toInt())
            selectedSymbolFlexBox.addView(symbolCard)
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/

    private fun initializeDailySymbol() {
        val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault())
        val dateFormat = SimpleDateFormat(DateUtils.DATE_PATTERN_DASH, Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = Date()
        for (num in 1..100) {
            mDailySymbolList.add(Leisure(dateFormat.format(cal.time), cal.get(Calendar.DAY_OF_WEEK), cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())!!, dayOfMonth.format(cal.time), cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!))
            cal.add(Calendar.DATE, -1)
        }
        mDailySymbolAdapter = DailySymbolAdapter(
                this,
                mDailySymbolList,
                null
        )
        leisureRecyclerView?.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@DashboardActivity, 1)
            addItemDecoration(SettingsScheduleFragment.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            adapter = mDailySymbolAdapter
        }
    }
}
