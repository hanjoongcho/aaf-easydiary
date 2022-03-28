package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.ChartBase
import me.blog.korn123.easydiary.databinding.ActivityStatisticsBinding
import me.blog.korn123.easydiary.extensions.applyFontToMenuItem
import me.blog.korn123.easydiary.fragments.WritingBarChartFragment
import me.blog.korn123.easydiary.fragments.SymbolBarChartFragment
import me.blog.korn123.easydiary.fragments.SymbolHorizontalBarChartFragment
import me.blog.korn123.easydiary.fragments.WeightLineChartFragment

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
            title = if (isSingleChart()) "" else getString(R.string.statistics_creation_time)
            setDisplayHomeAsUpEnabled(true)
            if (isSingleChart()) setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        val defaultChart = when (intent.getStringExtra(CHART_MODE)) {
            MODE_SINGLE_LINE_CHART_WEIGHT -> WeightLineChartFragment()
            MODE_SINGLE_BAR_CHART_SYMBOL -> SymbolBarChartFragment()
            else -> WritingBarChartFragment()
        }
            if (isSingleChart()) WeightLineChartFragment() else
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
            android.R.id.home -> finish()
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

    private fun isSingleChart(): Boolean = intent.getStringExtra(CHART_MODE) != null

    companion object {
        const val CHART_MODE = "chart_mode"
        const val MODE_SINGLE_LINE_CHART_WEIGHT = "mode_single_line_chart_weight"
        const val MODE_SINGLE_BAR_CHART_SYMBOL = "mode_single_bar_chart_symbol"
    }
}
