package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.ChartBase
import me.blog.korn123.easydiary.databinding.ActivityStatisticsBinding
import me.blog.korn123.easydiary.extensions.applyFontToMenuItem
import me.blog.korn123.easydiary.fragments.*
import me.blog.korn123.easydiary.helper.StatisticsConstants

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class StatisticsActivity : ChartBase() {
    private lateinit var mBinding: ActivityStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            if (isSingleChart()) setHomeAsUpIndicator(R.drawable.ic_cross)
            title =
                when (intent.getStringExtra(StatisticsConstants.CHART_MODE)) {
                    StatisticsConstants.MODE_SINGLE_LINE_CHART_WEIGHT -> "Weight"
                    StatisticsConstants.MODE_SINGLE_LINE_CHART_STOCK -> "Stock"
                    StatisticsConstants.MODE_SINGLE_BAR_CHART_SYMBOL -> getString(R.string.statistics_symbol_all)
                    StatisticsConstants.MODE_SINGLE_HORIZONTAL_BAR_CHART_SYMBOL -> getString(R.string.statistics_symbol_top_ten)
                    else -> getString(R.string.statistics_creation_time)
                }
        }

        val defaultChart =
            when (intent.getStringExtra(StatisticsConstants.CHART_MODE)) {
                StatisticsConstants.MODE_SINGLE_LINE_CHART_WEIGHT -> WeightLineChartFragment()
                StatisticsConstants.MODE_SINGLE_LINE_CHART_STOCK -> StockLineChartFragment()
                StatisticsConstants.MODE_SINGLE_BAR_CHART_SYMBOL -> SymbolBarChartFragment()
                StatisticsConstants.MODE_SINGLE_HORIZONTAL_BAR_CHART_SYMBOL -> SymbolHorizontalBarChartFragment()
                else -> WritingBarChartFragment()
            }
        supportFragmentManager.run {
            beginTransaction().run {
                replace(R.id.chartView, defaultChart)
                commit()
            }
            executePendingTransactions()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!isSingleChart()) {
            menuInflater.inflate(R.menu.activity_statistics, menu)
            val targetItems = mutableListOf<MenuItem>()
            targetItems.add(menu.findItem(R.id.barChart))
            targetItems.add(menu.findItem(R.id.barChart2))
            targetItems.add(menu.findItem(R.id.barChart3))
            targetItems.map { item ->
                applyFontToMenuItem(item)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

            R.id.barChart -> {
                supportActionBar?.title = getString(R.string.statistics_creation_time)
                supportFragmentManager.beginTransaction().run {
                    replace(R.id.chartView, WritingBarChartFragment())
                    commit()
                }
            }

            R.id.barChart2 -> {
                supportActionBar?.title = getString(R.string.statistics_symbol_all)
                supportFragmentManager.beginTransaction().run {
                    replace(R.id.chartView, SymbolBarChartFragment())
                    commit()
                }
            }

            R.id.barChart3 -> {
                supportActionBar?.title = getString(R.string.statistics_symbol_top_ten)
                supportFragmentManager.beginTransaction().run {
                    replace(R.id.chartView, SymbolHorizontalBarChartFragment())
                    commit()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isSingleChart(): Boolean = intent.getStringExtra(StatisticsConstants.CHART_MODE) != null
}
