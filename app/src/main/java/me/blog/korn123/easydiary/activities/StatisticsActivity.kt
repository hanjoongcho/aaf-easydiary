package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_barchart.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.ChartBase
import me.blog.korn123.easydiary.extensions.applyFontToMenuItem
import me.blog.korn123.easydiary.fragments.BarChartFragment
import me.blog.korn123.easydiary.fragments.BarChartFragmentT2
import me.blog.korn123.easydiary.fragments.HorizontalBarChartFragment

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class StatisticsActivity : ChartBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barchart)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.statistics_creation_time)
            setDisplayHomeAsUpEnabled(true)    
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.chartView, BarChartFragment())
        fragmentTransaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_chart, menu)
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
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.chartView, BarChartFragment())
                fragmentTransaction.commit()
            }
            R.id.barChart2 -> {
                supportActionBar?.title = getString(R.string.statistics_symbol_all)
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.chartView, BarChartFragmentT2())
                fragmentTransaction.commit()
            }
            R.id.barChart3 -> {
                supportActionBar?.title = getString(R.string.statistics_symbol_top_ten)
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.chartView, HorizontalBarChartFragment())
                fragmentTransaction.commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
