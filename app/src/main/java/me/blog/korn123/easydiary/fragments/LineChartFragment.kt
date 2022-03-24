package me.blog.korn123.easydiary.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.ContentLoadingProgressBar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.DAILY_SCALE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.views.FixedTextView
import java.text.SimpleDateFormat

class LineChartFragment : androidx.fragment.app.Fragment() {
    private lateinit var mLineChart: LineChart
    private lateinit var mChartTitle: FixedTextView
    private lateinit var mBarChartProgressBar: ContentLoadingProgressBar
    private var mCoroutineJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_line_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLineChart = view.findViewById(R.id.lineChart)
        mChartTitle = view.findViewById(R.id.chartTitle)
        mBarChartProgressBar = view.findViewById(R.id.barChartProgressBar)
        mLineChart.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mLineChart.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        mLineChart.setPinchZoom(false)

//        barChart.setDrawGridBackground(true)
        // mChart.setDrawYLabels(false);
//        barChart.zoom(1.5F, 0F, 0F, 0F)

        val xAxisFormatter = WeightXAxisValueFormatter(context)

        val xAxis = mLineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = FontUtils.getCommonTypeface(requireContext())
        xAxis.labelRotationAngle = -45F
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.valueFormatter = xAxisFormatter

        val custom = WeightYAxisValueFormatter(context)

        val leftAxis = mLineChart.axisLeft
        leftAxis.typeface = FontUtils.getCommonTypeface(requireContext())
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = custom
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = mLineChart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = FontUtils.getCommonTypeface(requireContext())
        rightAxis.setLabelCount(8, false)
        rightAxis.valueFormatter = custom
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val l = mLineChart.legend
        l.typeface = FontUtils.getCommonTypeface(requireContext())
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.form = Legend.LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f

        val mv = WeightMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = mLineChart // For bounds control
        mLineChart.marker = mv // Set the marker to the chart

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
            if (barEntries.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val lineDataSet = LineDataSet(barEntries, "Weight")
                    val iValueFormatter = WeightIValueFormatter(context)
                    lineDataSet.valueFormatter = iValueFormatter
//                    val colors = intArrayOf(
//                        Color.rgb(193, 37, 82), Color.rgb(255, 102, 0), Color.rgb(245, 199, 0),
//                        Color.rgb(106, 150, 31), Color.rgb(179, 100, 53), Color.rgb(115, 130, 153))
//                    lineDataSet.setColors(*colors)
                    lineDataSet.setDrawIcons(false)
                    lineDataSet.setDrawValues(true)
                    val dataSets = ArrayList<ILineDataSet>()
                    dataSets.add(lineDataSet)
                    val lineData = LineData(dataSets)
                    lineData.setValueTextSize(10f)
                    lineData.setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                    mLineChart.data = lineData
                    mLineChart.animateY(2000)
                    mBarChartProgressBar.visibility = View.GONE
                }
            } else {
                withContext(Dispatchers.Main) {
                    mBarChartProgressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCoroutineJob?.run { if (isActive) cancel() }
    }

    val mTimeMillisMap = hashMapOf<Int, Long>()
    private fun setData(count: Int = 6, range: Float = 20F): ArrayList<Entry> {
        val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
        val listDiary = EasyDiaryDbHelper.findDiary(null, false, 0, 0, DAILY_SCALE, realmInstance = realmInstance)
        realmInstance.close()
        val barEntries = ArrayList<Entry>()
        var index = 0
        listDiary.reversed().forEach { diaryDto ->
            diaryDto.title?.let {
                if (EasyDiaryUtils.isContainNumber(it)) {
                    barEntries.add(Entry(index.toFloat(), EasyDiaryUtils.findNumber(it)))
                    mTimeMillisMap[index] = diaryDto.currentTimeMillis
                    index++
                }
            }
        }
        return barEntries
    }

    companion object {
        const val CHART_TITLE = "chartTitle"
    }

    inner class WeightXAxisValueFormatter(private var context: Context?) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            val timeMillis: Long = mTimeMillisMap[value.toInt()] ?: 0
            return if (timeMillis > 0) DateUtils.getDateStringFromTimeMillis(timeMillis, SimpleDateFormat.SHORT) else "N/A$value"
        }
    }

    inner class WeightYAxisValueFormatter(private var context: Context?) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return "${value}kg"
        }
    }

    inner class WeightIValueFormatter(private var context: Context?) : IValueFormatter {

        /**
         * Called when a value (from labels inside the chart) is formatted
         * before being drawn. For performance reasons, avoid excessive calculations
         * and memory allocations inside this method.
         *
         * @param value           the value to be formatted
         * @param entry           the entry the value belongs to - in e.g. BarChart, this is of class BarEntry
         * @param dataSetIndex    the index of the DataSet the entry in focus belongs to
         * @param viewPortHandler provides information about the current chart state (scale, translation, ...)
         * @return the formatted label ready for being drawn
         */
        override fun getFormattedValue(value: Float, entry: Entry, dataSetIndex: Int, viewPortHandler: ViewPortHandler): String {
            return "${value}kg"
        }
    }

    inner class WeightMarkerView(context: Context, private val xAxisValueFormatter: IAxisValueFormatter) : MarkerView(context, R.layout.partial_custom_marker_view) {
        private val tvContent: TextView = findViewById(R.id.tvContent)

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            e?.let { entry ->
                tvContent.run {
                    text = "${entry.y}kg"
                    typeface = FontUtils.getCommonTypeface(context)
                }
                super.refreshContent(entry, highlight)
            }
        }

        override fun getOffset(): MPPointF {
            return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }
    }
}