package me.blog.korn123.easydiary.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.fragment_barchart.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.DayAxisValueFormatter
import me.blog.korn123.easydiary.chart.IValueFormatterExt
import me.blog.korn123.easydiary.chart.MyAxisValueFormatter
import me.blog.korn123.easydiary.chart.XYMarkerView
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import java.util.*

class BarChartFragment : androidx.fragment.app.Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        barChart.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false)

//        barChart.setDrawGridBackground(true)
        // mChart.setDrawYLabels(false);
//        barChart.zoom(1.5F, 0F, 0F, 0F)

        val xAxisFormatter = DayAxisValueFormatter(context, barChart)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.typeface = mTfLight
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.valueFormatter = xAxisFormatter

        val custom = MyAxisValueFormatter(context)

        val leftAxis = barChart.axisLeft
//        leftAxis.typeface = mTfLight
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = custom
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = barChart.axisRight
        rightAxis.setDrawGridLines(false)
//        rightAxis.typeface = mTfLight
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

        val mv = XYMarkerView(context!!, xAxisFormatter)
        mv.chartView = barChart // For bounds control
        barChart.marker = mv // Set the marker to the chart

        // determine title parameter
        arguments?.let { bundle ->
            val title = bundle.getString(CHART_TITLE)
            if (title != null) {
                chartTitle.text = title
                chartTitle.visibility = View.VISIBLE
            }
        }

        setData(6, 20f)
        barChart.animateY(2000)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_barchart, container, false)
    }

    private fun setData(count: Int, range: Float) {
        val listDiary = EasyDiaryDbHelper.readDiary(null)
        val map = hashMapOf<Int, Int>()
        listDiary.map { diaryDto ->
            val writeHour = DateUtils.timeMillisToDateTime(diaryDto.currentTimeMillis, "HH")
            val itemNumber = hourToItemNumber(Integer.parseInt(writeHour))
            if (map[itemNumber] == null) {
                map.put(itemNumber, 1)
            } else {
                map.put(itemNumber, (map[itemNumber] ?: 0) + 1)
            }
        }

        val barEntries = ArrayList<BarEntry>()
        for (i in 1..count) {
            var total = 0
            if (map[i] != null) total = map[i] ?: 0
            barEntries.add(BarEntry(i.toFloat(), total.toFloat()))
        }

        val barDataSet: BarDataSet

        barDataSet = BarDataSet(barEntries, getString(R.string.statistics_creation_time))
        val iValueFormatter = IValueFormatterExt(context)
        barDataSet.valueFormatter = iValueFormatter
        val colors = intArrayOf(
                Color.rgb(193, 37, 82), Color.rgb(255, 102, 0), Color.rgb(245, 199, 0),
                Color.rgb(106, 150, 31), Color.rgb(179, 100, 53), Color.rgb(115, 130, 153))
        barDataSet.setColors(*colors)
        barDataSet.setDrawIcons(false)
        barDataSet.setDrawValues(true)
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(barDataSet)

        val barData = BarData(dataSets)
        barData.setValueTextSize(10f)
//        barData.setValueTypeface(mTfLight)
        barData.barWidth = 0.9f

        barChart.data = barData
    }

    private fun hourToItemNumber(hour: Int): Int = when (hour) {
        in 0..3 -> 1
        in 4..7 -> 2
        in 8..11 -> 3
        in 12..15 -> 4
        in 16..19 -> 5
        in 20..23 -> 6
        else -> 0
    }

    companion object {
        const val CHART_TITLE = "chartTitle"
    }
}