package me.blog.korn123.easydiary.activities

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
import me.blog.korn123.easydiary.extensions.EMPTY
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.fragments.StockLineChartFragment
import me.blog.korn123.easydiary.fragments.SymbolBarChartFragment
import me.blog.korn123.easydiary.fragments.SymbolHorizontalBarChartFragment
import me.blog.korn123.easydiary.fragments.TagHorizontalBarChartFragment
import me.blog.korn123.easydiary.fragments.WeightLineChartFragment
import me.blog.korn123.easydiary.fragments.WritingBarChartFragment
import me.blog.korn123.easydiary.helper.StatisticsConstants
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

                    R.id.barChart4 -> {
                        switchChart(TagHorizontalBarChartFragment(), R.string.statistics_tag_top_ten)
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
            barChart4.setOnClickListener(customItemClickListener)
        }
    }

    private fun openCustomOptionMenu() {
        FontUtils.setFontsTypeface(this@StatisticsActivity, null, popupMenuBinding.root, true)
        // Bound the menu to the space left on screen, so the fourth chart of the menu scrolls into view in
        // landscape instead of being cut off below the screen.
        val popupMenuAnchor = findViewById<View>(R.id.toolbar)
        popupMenuBinding.menuScroll.maxContentHeight = EasyDiaryUtils.optionMenuMaxHeight(popupMenuAnchor)

        popupWindow =
            EasyDiaryUtils.openCustomOptionMenu(
                popupMenuBinding.root,
                popupMenuAnchor,
            )
        popupMenuBinding.run {
            updateDrawableColorInnerCardView(imgBarChart)
            updateDrawableColorInnerCardView(imgBarChart2)
            updateDrawableColorInnerCardView(imgBarChart3)
            updateDrawableColorInnerCardView(imgBarChart4)
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
                    StatisticsConstants.MODE_SINGLE_BAR_CHART_WRITING -> getString(R.string.statistics_creation_time)
                    StatisticsConstants.MODE_SINGLE_HORIZONTAL_BAR_CHART_SYMBOL -> getString(R.string.statistics_symbol_top_ten)
                    StatisticsConstants.MODE_SINGLE_HORIZONTAL_BAR_CHART_TAG -> getString(R.string.statistics_tag_top_ten)
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
                StatisticsConstants.MODE_SINGLE_BAR_CHART_WRITING -> WritingBarChartFragment()
                StatisticsConstants.MODE_SINGLE_HORIZONTAL_BAR_CHART_SYMBOL -> SymbolHorizontalBarChartFragment()
                StatisticsConstants.MODE_SINGLE_HORIZONTAL_BAR_CHART_TAG -> TagHorizontalBarChartFragment()
                else -> WritingBarChartFragment()
            }
        supportFragmentManager.run {
            beginTransaction().run {
                replace(R.id.chartView, defaultChart)
                commit()
            }
            executePendingTransactions()
        }
        applyUIConfig()
    }

    private fun switchChart(
        fragment: androidx.fragment.app.Fragment,
        titleResId: Int,
    ) {
        supportActionBar?.subtitle = getString(titleResId)
        supportFragmentManager.run {
            beginTransaction().run {
                replace(R.id.chartView, fragment)
                commit()
            }
            executePendingTransactions()
        }
        applyUIConfig()
    }

    private fun applyUIConfig() {
        getMainViewGroup()?.let {
            initTextSize(it)
            updateTextColors(it)
            updateAppViews(it)
            updateCardViewPolicy(it)
        }
        FontUtils.setFontsTypeface(
            applicationContext,
            null,
            findViewById(android.R.id.content),
            customLineSpacing,
        )
    }

    private fun isSingleChart(): Boolean = intent.getStringExtra(StatisticsConstants.CHART_MODE) != null
}
