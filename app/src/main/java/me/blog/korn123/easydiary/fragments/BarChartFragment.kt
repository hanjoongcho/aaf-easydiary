package me.blog.korn123.easydiary.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ContentLoadingProgressBar
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import me.blog.korn123.commons.utils.DateUtils
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.DayAxisValueFormatter
import me.blog.korn123.easydiary.chart.IValueFormatterExt
import me.blog.korn123.easydiary.chart.MyAxisValueFormatter
import me.blog.korn123.easydiary.chart.XYMarkerView
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.views.FixedTextView

class BarChartFragment : androidx.fragment.app.Fragment() {
    private lateinit var mBarChart: BarChart
    private lateinit var mChartTitle: FixedTextView
    private lateinit var mBarChartProgressBar: ContentLoadingProgressBar
    private var mCoroutineJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_barchart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBarChart = view.findViewById(R.id.barChart)
        mChartTitle = view.findViewById(R.id.chartTitle)
        mBarChartProgressBar = view.findViewById(R.id.barChartProgressBar)

        mBarChart.setDrawBarShadow(false)
        mBarChart.setDrawValueAboveBar(true)
        mBarChart.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mBarChart.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        mBarChart.setPinchZoom(false)

//        barChart.setDrawGridBackground(true)
        // mChart.setDrawYLabels(false);
//        barChart.zoom(1.5F, 0F, 0F, 0F)

        val xAxisFormatter = DayAxisValueFormatter(context, mBarChart)

        val xAxis = mBarChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = FontUtils.getCommonTypeface(requireContext())
        xAxis.labelRotationAngle = -45F
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.valueFormatter = xAxisFormatter

        val custom = MyAxisValueFormatter(context)

        val leftAxis = mBarChart.axisLeft
        leftAxis.typeface = FontUtils.getCommonTypeface(requireContext())
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = custom
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = mBarChart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = FontUtils.getCommonTypeface(requireContext())
        rightAxis.setLabelCount(8, false)
        rightAxis.valueFormatter = custom
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val l = mBarChart.legend
        l.typeface = FontUtils.getCommonTypeface(requireContext())
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.form = Legend.LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f

        val mv = XYMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = mBarChart // For bounds control
        mBarChart.marker = mv // Set the marker to the chart

        // determine title parameter
        arguments?.let { bundle ->
            val title = bundle.getString(CHART_TITLE)
            if (title != null) {
                mChartTitle.text = title
                mChartTitle.visibility = View.VISIBLE
            }
        }

        mCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
            val barEntries = setData()
            withContext(Dispatchers.Main) {
                if (barEntries.isNotEmpty()) {
                    val barDataSet = BarDataSet(barEntries, getString(R.string.statistics_creation_time))
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
                    barData.setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                    barData.barWidth = 0.9f
                    mBarChart.data = barData
                    mBarChart.animateY(2000)
                }
                mBarChartProgressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCoroutineJob?.run { if (isActive) cancel() }
    }

    private fun setData(count: Int = 6, range: Float = 20F): ArrayList<BarEntry> {
        val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
        val listDiary = EasyDiaryDbHelper.findDiary(null, realmInstance = realmInstance)
        realmInstance.close()
        val barEntries = ArrayList<BarEntry>()
        if (listDiary.isNotEmpty()) {
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
            for (i in 1..count) {
                var total = 0
                if (map[i] != null) total = map[i] ?: 0
                barEntries.add(BarEntry(i.toFloat(), total.toFloat()))
            }
        }
        return barEntries
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