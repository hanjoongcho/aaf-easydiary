package me.blog.korn123.easydiary.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ContentLoadingProgressBar
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import io.github.aafactory.commons.utils.CommonUtils
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.DiaryCountingAxisValueFormatter
import me.blog.korn123.easydiary.chart.IValueFormatterExt
import me.blog.korn123.easydiary.chart.XYMarkerView
import me.blog.korn123.easydiary.extensions.scaledDrawable
import me.blog.korn123.easydiary.views.FixedTextView

class SymbolBarChartFragment : androidx.fragment.app.Fragment() {
    private lateinit var mBarChart: BarChart
    private lateinit var mChartTitle: FixedTextView
    private lateinit var mBarChartProgressBar: ContentLoadingProgressBar
    private lateinit var mSymbolMap: HashMap<Int, String>
    private val mSequences = arrayListOf<Int>()
    private val mTypeface: Typeface
        get() = FontUtils.getCommonTypeface(requireContext())!!
    private var mCoroutineJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_writing_barchart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBarChart = view.findViewById(R.id.barChart)
        mChartTitle = view.findViewById(R.id.chartTitle)
        mBarChartProgressBar = view.findViewById(R.id.barChartProgressBar)

        mSymbolMap = FlavorUtils.getDiarySymbolMap(requireContext())
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
        //barChart.zoom(3.5F, 0F, 0F, 0F)

        val xAxisFormatter = AxisValueFormatter()
        mBarChart.xAxis.run {
            position = XAxis.XAxisPosition.BOTTOM
            typeface = mTypeface
            labelRotationAngle = -45F
            setDrawGridLines(false)
            granularity = 1f // only intervals of 1 day
            labelCount = 7
            valueFormatter = xAxisFormatter
        }
        val diaryCountingAxisValueFormatter = DiaryCountingAxisValueFormatter(context)
        mBarChart.axisLeft.run {
            typeface = mTypeface
            setLabelCount(8, false)
            valueFormatter = diaryCountingAxisValueFormatter
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 15f
            axisMinimum = 0f // this replaces setStartAtZero(true)
        }

        mBarChart.axisRight.run {
            setDrawGridLines(false)
            typeface = mTypeface
            setLabelCount(8, false)
            valueFormatter = diaryCountingAxisValueFormatter
            spaceTop = 15f
            axisMinimum = 0f // this replaces setStartAtZero(true)
        }

        mBarChart.legend.run {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            form = Legend.LegendForm.SQUARE
            formSize = 9f
            textSize = 11f
            xEntrySpace = 4f
            typeface = mTypeface
        }

        val mv = XYMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = mBarChart // For bounds control
        mBarChart.marker = mv // Set the marker to the chart

        // determine title parameter
        arguments?.let { bundle ->
            val title = bundle.getString(WritingBarChartFragment.CHART_TITLE)
            if (title != null) {
                mChartTitle.text = title
                mChartTitle.visibility = View.VISIBLE
            }
        }

        mCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
            val sortedMap = EasyDiaryUtils.getSortedMapBySymbol(true)
            withContext(Dispatchers.Main) {
                val barEntries = ArrayList<BarEntry>()
                var index = 1.0F
                sortedMap.forEach { (key, value) ->
                    val drawable: Drawable? = when (FlavorUtils.sequenceToSymbolResourceId(key) > 0) {
                        true -> scaledDrawable(FlavorUtils.sequenceToSymbolResourceId(key), CommonUtils.dpToPixel(requireContext(),24F) ,CommonUtils.dpToPixel(requireContext(),24F))
                        false -> null
                    }
                    mSequences.add(key)
                    barEntries.add(BarEntry(index++, value.toFloat(), drawable))
                }
                if (barEntries.isNotEmpty()) {
                    val barDataSet = BarDataSet(barEntries, getString(R.string.statistics_symbol_all))
                    val iValueFormatter = IValueFormatterExt(context)
                    barDataSet.valueFormatter = iValueFormatter
                    val colors = intArrayOf(
                        Color.rgb(193, 37, 82), Color.rgb(255, 102, 0), Color.rgb(245, 199, 0),
                        Color.rgb(106, 150, 31), Color.rgb(179, 100, 53), Color.rgb(115, 130, 153))
                    barDataSet.setColors(*colors)
                    barDataSet.setDrawIcons(true)
                    barDataSet.setDrawValues(false)
                    val dataSets = ArrayList<IBarDataSet>()
                    dataSets.add(barDataSet)

                    val barData = BarData(dataSets)
                    barData.setValueTextSize(10f)
                    barData.setValueTypeface(mTypeface)
                    barData.barWidth = 0.9f
                    mBarChart.zoom((sortedMap.size / 6.0F), 0F, 0F, 0F)
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

    inner class AxisValueFormatter : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
            return when  {
                value > 0 && value <= mSequences.size -> mSymbolMap[mSequences[value.toInt() - 1]] ?: "None"
                else -> "None"
            }
        }
    }
}