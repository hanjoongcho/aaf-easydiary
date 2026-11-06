package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupWindow
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.ChartBase
import me.blog.korn123.easydiary.databinding.ActivityStatisticsBinding
import me.blog.korn123.easydiary.databinding.PopupMenuStatisticsBinding
import me.blog.korn123.easydiary.enums.GridSpanMode
import me.blog.korn123.easydiary.extensions.EMPTY
import me.blog.korn123.easydiary.extensions.applyFontToMenuItem
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.confirmPermission
import me.blog.korn123.easydiary.extensions.openGridSettingDialog
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.fragments.*
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE
import me.blog.korn123.easydiary.helper.StatisticsConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class StatisticsActivity : ChartBase() {
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var popupMenuBinding: PopupMenuStatisticsBinding
    private var popupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        popupMenuBinding = PopupMenuStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setupActionBar()
        displayDefaultChart()
        setupPopupMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!isSingleChart()) {
            menuInflater.inflate(R.menu.activity_statistics, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.popupMenu -> handlePopupMenuSelected()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handlePopupMenuSelected() {
        openCustomOptionMenu()
    }

    private fun setupPopupMenu() {
        val customItemClickListener =
            View.OnClickListener {
                when (it.id) {
                    R.id.barChart -> {
                        switchChart(WritingBarChartFragment(), R.string.statistics_creation_time)
                    }

                    R.id.barChart2 -> {
                        switchChart(SymbolBarChartFragment(), R.string.statistics_symbol_all)
                    }

                    R.id.barChart3 -> {
                        switchChart(SymbolHorizontalBarChartFragment(), R.string.statistics_symbol_top_ten)
                    }
                }
                Handler(Looper.getMainLooper()).post { popupWindow?.dismiss() }
            }

        popupMenuBinding.run {
            updateAppViews(this.root)
            updateTextColors(this.root)
            FontUtils.setFontsTypeface(this@StatisticsActivity, null, this.root, true)
            barChart.setOnClickListener(customItemClickListener)
            barChart2.setOnClickListener(customItemClickListener)
            barChart3.setOnClickListener(customItemClickListener)
        }
    }

    private fun openCustomOptionMenu() {
        FontUtils.setFontsTypeface(this@StatisticsActivity, null, popupMenuBinding.root, true)

        popupWindow =
            EasyDiaryUtils.openCustomOptionMenu(
                popupMenuBinding.root,
                findViewById(R.id.toolbar),
            )
        popupMenuBinding.run {
            updateDrawableColorInnerCardView(imgBarChart)
            updateDrawableColorInnerCardView(imgBarChart2)
            updateDrawableColorInnerCardView(imgBarChart3)
        }
    }

    private fun setupActionBar() {
        supportActionBar?.run {
            title = String.EMPTY
            setDisplayHomeAsUpEnabled(true)
            if (isSingleChart()) setHomeAsUpIndicator(R.drawable.ic_cross)
            subtitle =
                when (intent.getStringExtra(StatisticsConstants.CHART_MODE)) {
                    StatisticsConstants.MODE_SINGLE_LINE_CHART_WEIGHT -> "Weight"
                    StatisticsConstants.MODE_SINGLE_LINE_CHART_STOCK -> "Stock"
                    StatisticsConstants.MODE_SINGLE_BAR_CHART_SYMBOL -> getString(R.string.statistics_symbol_all)
                    StatisticsConstants.MODE_SINGLE_HORIZONTAL_BAR_CHART_SYMBOL -> getString(R.string.statistics_symbol_top_ten)
                    else -> getString(R.string.statistics_creation_time)
                }
        }
    }

    private fun displayDefaultChart() {
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

    private fun switchChart(
        fragment: androidx.fragment.app.Fragment,
        titleResId: Int,
    ) {
        supportActionBar?.subtitle = getString(titleResId)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.chartView, fragment)
            commit()
        }
    }

    private fun isSingleChart(): Boolean = intent.getStringExtra(StatisticsConstants.CHART_MODE) != null
}
