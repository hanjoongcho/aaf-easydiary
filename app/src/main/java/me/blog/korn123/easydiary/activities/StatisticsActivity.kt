package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.ChartBase
import me.blog.korn123.easydiary.databinding.ActivityStatisticsBinding
import me.blog.korn123.easydiary.extensions.applyFontToMenuItem
import me.blog.korn123.easydiary.fragments.BarChartFragment
import me.blog.korn123.easydiary.fragments.BarChartFragmentT2
import me.blog.korn123.easydiary.fragments.HorizontalBarChartFragment

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
            title = getString(R.string.statistics_creation_time)
            setDisplayHomeAsUpEnabled(true)    
        }

        supportFragmentManager.run {
            beginTransaction().run {
                replace(R.id.chartView, BarChartFragment())
                commit()
            }
            executePendingTransactions()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_statistics, menu)
        val targetItems = mutableListOf<MenuItem>()
        targetItems.add(menu.findItem(R.id.barChart))
        targetItems.add(menu.findItem(R.id.barChart2))
        targetItems.add(menu.findItem(R.id.barChart3))
        targetItems.map { item ->
            applyFontToMenuItem(item)
        }
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.barChart -> {
                supportActionBar?.title = getString(R.string.statistics_creation_time)
                supportFragmentManager.beginTransaction().run {
                    replace(R.id.chartView, BarChartFragment())
                    commit()
                }
            }
            R.id.barChart2 -> {
                supportActionBar?.title = getString(R.string.statistics_symbol_all)
                supportFragmentManager.beginTransaction().run {
                    replace(R.id.chartView, BarChartFragmentT2())
                    commit()
                }
            }
            R.id.barChart3 -> {
                supportActionBar?.title = getString(R.string.statistics_symbol_top_ten)
                supportFragmentManager.beginTransaction().run {
                    replace(R.id.chartView, HorizontalBarChartFragment())
                    commit()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
