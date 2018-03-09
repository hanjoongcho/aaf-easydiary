package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.ViewGroup
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.activity_barchart.*
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class BarChartActivity : ChartBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barchart)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.bar_chart_title)
            setDisplayHomeAsUpEnabled(true)    
        }

        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        barChart.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false)

        barChart.setDrawGridBackground(false)
        // mChart.setDrawYLabels(false);

        val xAxisFormatter = DayAxisValueFormatter(this, barChart)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = mTfLight
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.valueFormatter = xAxisFormatter

        val custom = MyAxisValueFormatter(this)

        val leftAxis = barChart.axisLeft
        leftAxis.typeface = mTfLight
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = custom
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = barChart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = mTfLight
        rightAxis.setLabelCount(8, false)
        rightAxis.valueFormatter = custom
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val l = barChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.form = Legend.LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });

        val mv = XYMarkerView(this, xAxisFormatter)
        mv.chartView = barChart // For bounds control
        barChart.marker = mv // Set the marker to the chart

        setData(6, 20f)
        setFontsStyle()
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<ViewGroup>(android.R.id.content))
    }

    private fun setData(count: Int, range: Float) {

        val listDiary = EasyDiaryDbHelper.readDiary(null)
        val map = hashMapOf<Int, Int>()
        listDiary?.map { diaryDto ->
            val writeHour = DateUtils.timeMillisToDateTime(diaryDto.currentTimeMillis, "HH")
            val itemNumber = hourToItemNumber(Integer.parseInt(writeHour))
            if (map[itemNumber] == null) {
                map.put(itemNumber, 1)
            } else {
                map.put(itemNumber, (map[itemNumber] ?: 0) + 1)
            }
        }

        val yVals1 = ArrayList<BarEntry>()
        for (i in 1..count) {
            var total = 0
            if (map[i] != null) total = map[i] ?: 0
            yVals1.add(BarEntry(i.toFloat(), total.toFloat()))
        }

        val set1: BarDataSet

        set1 = BarDataSet(yVals1, getString(R.string.bar_chart_status))
        val iValueFormatter = IValueFormatterExt(this)
        set1.valueFormatter = iValueFormatter
        //            set1.setDrawIcons(false);

        set1.setColors(*ColorTemplate.MATERIAL_COLORS)

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(set1)

        val data = BarData(dataSets)
        data.setValueTextSize(10f)
        data.setValueTypeface(mTfLight)
        data.barWidth = 0.9f

        barChart.data = data
    }

    fun itemNumberToRange(itemNumber: Int): String = when (itemNumber) {
        1 -> getString(R.string.range_a)
        2 -> getString(R.string.range_b)
        3 -> getString(R.string.range_c)
        4 -> getString(R.string.range_d)
        5 -> getString(R.string.range_e)
        6 -> getString(R.string.range_f)
        else -> getString(R.string.range_g)
    }

    companion object {
        fun hourToItemNumber(hour: Int): Int {
            var itemNumber = when (hour) {
                in 0..3 -> 1
                in 4..7 -> 2
                in 8..11 -> 3
                in 12..15 -> 4
                in 16..19 -> 5
                in 20..23 -> 6
                else -> 0
            }
            return itemNumber
        }
    }
}
