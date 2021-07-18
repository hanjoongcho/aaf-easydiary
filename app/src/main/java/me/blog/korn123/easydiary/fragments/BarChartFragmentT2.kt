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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.ChartUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.IValueFormatterExt
import me.blog.korn123.easydiary.chart.MyAxisValueFormatter
import me.blog.korn123.easydiary.chart.XYMarkerView
import me.blog.korn123.easydiary.extensions.scaledDrawable
import me.blog.korn123.easydiary.views.FixedTextView
import java.util.*

class BarChartFragmentT2 : androidx.fragment.app.Fragment() {
    private lateinit var mBarChart: BarChart
    private lateinit var mChartTitle: FixedTextView
    private lateinit var mBarChartProgressBar: ContentLoadingProgressBar
    private lateinit var mSymbolMap: HashMap<Int, String>
    private val mSequences = arrayListOf<Int>()
    private val mTypeface: Typeface
        get() = FontUtils.getCommonTypeface(requireContext())!!

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

        val xAxis = mBarChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.typeface = mTypeface
        xAxis.labelRotationAngle = -45F
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.valueFormatter = xAxisFormatter
        val custom = MyAxisValueFormatter(context)

        val leftAxis = mBarChart.axisLeft
        leftAxis.typeface = mTypeface
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = custom
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = mBarChart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = mTypeface
        rightAxis.setLabelCount(8, false)
        rightAxis.valueFormatter = custom
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val legend = mBarChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.form = Legend.LegendForm.SQUARE
        legend.formSize = 9f
        legend.textSize = 11f
        legend.xEntrySpace = 4f
        legend.typeface = mTypeface

        val mv = XYMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = mBarChart // For bounds control
        mBarChart.marker = mv // Set the marker to the chart

        // determine title parameter
        arguments?.let { bundle ->
            val title = bundle.getString(BarChartFragment.CHART_TITLE)
            if (title != null) {
                mChartTitle.text = title
                mChartTitle.visibility = View.VISIBLE
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            setData()
            withContext(Dispatchers.Main) {
                mBarChart.animateY(2000)
                mBarChartProgressBar.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_barchart, container, false)
    }

    private fun setData() {
        val sortedMap = ChartUtils.getSortedMapBySymbol(true)

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
        
        val barDataSet: BarDataSet
        barDataSet = BarDataSet(barEntries, getString(R.string.statistics_symbol_all))
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