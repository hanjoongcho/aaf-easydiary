package me.blog.korn123.easydiary.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarLineChartBase
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
import kotlinx.android.synthetic.main.fragment_barchart.*
import me.blog.korn123.commons.utils.ChartUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.chart.IValueFormatterExt
import me.blog.korn123.easydiary.chart.MyAxisValueFormatter
import me.blog.korn123.easydiary.chart.XYMarkerView
import me.blog.korn123.easydiary.extensions.scaledDrawable
import java.util.*

class HorizontalBarChartFragment : androidx.fragment.app.Fragment() {
    private val mContext: Context
        get() { return context!! }
    val mSequences = arrayListOf<Int>()

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

        val xAxisFormatter = AxisValueFormatter(context, barChart)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.typeface = mTfLight
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.valueFormatter = xAxisFormatter

        val custom = MyAxisValueFormatter(context)

        val leftAxis = barChart.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = barChart.axisRight
        rightAxis.setDrawGridLines(false)
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
            val title = bundle.getString(BarChartFragment.CHART_TITLE)
            if (title != null) {
                chartTitle.text = title
                chartTitle.visibility = View.VISIBLE
            }
        }

        setData()
        barChart.animateY(2000)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_horizontal_barchart, container, false)
    }

    private fun setData() {
        val sortedMap = ChartUtils.getSortedMapBySymbol(true)

        val barEntries = ArrayList<BarEntry>()
        var index = 1F
        val itemArray = arrayListOf<HashMap<String, Int>>()
        sortedMap.forEach { (key, value) ->
            if (index > 10) return@forEach
            itemArray.add( hashMapOf("key" to key, "value" to value) )
            mSequences.add(key)
            index++
        }
        itemArray.reverse()
        mSequences.reverse()
        itemArray.forEachIndexed { index, item ->
            val drawable: Drawable? = when (FlavorUtils.sequenceToSymbolResourceId(item["key"]!!) > 0) {
                true -> scaledDrawable(FlavorUtils.sequenceToSymbolResourceId(item["key"]!!), CommonUtils.dpToPixel(mContext,24F) , CommonUtils.dpToPixel(mContext,24F))
                false -> null
            }
            barEntries.add(BarEntry((index + 1F), item["value"]!!.toFloat(), drawable))
        }

        val barDataSet: BarDataSet
        barDataSet = BarDataSet(barEntries, getString(R.string.statistics_symbol_top_ten))
        val iValueFormatter = IValueFormatterExt(context)
        barDataSet.valueFormatter = iValueFormatter
        val colors = intArrayOf(
                Color.rgb(255, 102, 0), Color.rgb(245, 199, 0),
                Color.rgb(106, 150, 31), Color.rgb(179, 100, 53), Color.rgb(115, 130, 153)
        )
        barDataSet.setColors(*colors)
        barDataSet.setDrawIcons(true)
        barDataSet.setDrawValues(false)
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(barDataSet)

        val barData = BarData(dataSets)
        barData.setValueTextSize(10f)
//        barData.setValueTypeface(mTfLight)
        barData.barWidth = 0.9f
//        barChart.zoom((sortedMap.size / 6.0F), 0F, 0F, 0F)
        barChart.data = barData
    }

    inner class AxisValueFormatter(private var context: Context?, private val chart: BarLineChartBase<*>) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
            val symbolMap = FlavorUtils.getDiarySymbolMap(context!!)
            return symbolMap[mSequences[value.toInt() - 1]] ?: "None"
        }
    }
}