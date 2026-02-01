package me.blog.korn123.easydiary.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.StatisticsActivity
import me.blog.korn123.easydiary.chart.DiaryCountingAxisValueFormatter
import me.blog.korn123.easydiary.chart.IValueFormatterExt
import me.blog.korn123.easydiary.chart.XYMarkerView
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.ChartConstants
import me.blog.korn123.easydiary.helper.StatisticsConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.views.FixedTextView

class SymbolHorizontalBarChartFragment : androidx.fragment.app.Fragment() {
    private lateinit var mBarChart: BarChart
    private lateinit var mChartTitle: FixedTextView
    private lateinit var mBarChartProgressBar: ContentLoadingProgressBar
    private lateinit var mSymbolMap: HashMap<Int, String>
    private var mCoroutineJob: Job? = null
    private val mTypeface: Typeface
        get() = FontUtils.getCommonTypeface(requireContext())!!
    val mSequences = arrayListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_symbol_horizontal_barchart, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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
//        barChart.zoom(1.5F, 0F, 0F, 0F)

        val xAxisFormatter = AxisValueFormatter()
        val diaryCountingAxisValueFormatter = DiaryCountingAxisValueFormatter(context)
        mBarChart.xAxis.run {
            position = XAxis.XAxisPosition.BOTTOM
            typeface = mTypeface
            textColor = requireContext().config.textColor
            setDrawGridLines(false)
            granularity = 1f // only intervals of 1 day
            labelCount = 7
            valueFormatter = xAxisFormatter
        }

        mBarChart.axisLeft.run {
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            axisMinimum = 0f // this replaces setStartAtZero(true)
            typeface = mTypeface
            textColor = requireContext().config.textColor
        }

        mBarChart.axisRight.run {
            setDrawGridLines(false)
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
            textColor = requireContext().config.textColor
        }

        val mv = XYMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = mBarChart // For bounds control
        mBarChart.marker = mv // Set the marker to the chart

        // determine title parameter
        arguments?.let { bundle ->
            val title = bundle.getString(ChartConstants.CHART_TITLE)
            if (title != null) {
                mChartTitle.text = title
                mChartTitle.visibility = View.VISIBLE

                getView()?.findViewById<ImageView>(R.id.image_expend_chart)?.let {
                    it.visibility = View.VISIBLE
                    requireActivity().updateDrawableColorInnerCardView(it, config.textColor)
                    it.setOnClickListener { view ->
                        view.postDelayed({
                            TransitionHelper.startActivityWithTransition(
                                requireActivity(),
                                Intent(
                                    requireActivity(),
                                    StatisticsActivity::class.java,
                                ).putExtra(StatisticsConstants.CHART_MODE, StatisticsConstants.MODE_SINGLE_HORIZONTAL_BAR_CHART_SYMBOL),
                            )
                        }, 300)
                    }
                }
            }
        }

        mCoroutineJob =
            lifecycleScope.launch(Dispatchers.IO) {
                val sortedMap = EasyDiaryUtils.getSymbolUsedCountMap(true)
                val barEntries = ArrayList<BarEntry>()
                var index = 1F
                val itemArray = arrayListOf<HashMap<String, Int>>()
                sortedMap.forEach { (key, value) ->
                    if (index > 10) return@forEach
                    itemArray.add(hashMapOf("key" to key, "value" to value))
                    mSequences.add(key)
                    index++
                }
                itemArray.reverse()
                mSequences.reverse()

                withContext(Dispatchers.Main) {
                    if (itemArray.isNotEmpty()) {
                        val colorItems = arrayListOf<Int>()
                        val defaultColor = Color.rgb(209, 232, 255)
                        itemArray.forEachIndexed { index, item ->
                            colorItems.add(defaultColor.darkenColor(index * 2))
                            val drawable: Drawable? =
                                when (FlavorUtils.sequenceToSymbolResourceId(item["key"]!!) > 0) {
                                    true -> scaledDrawable(FlavorUtils.sequenceToSymbolResourceId(item["key"]!!), requireContext().dpToPixel(24F), requireContext().dpToPixel(24F))
                                    false -> null
                                }
                            barEntries.add(BarEntry((index + 1F), item["value"]!!.toFloat(), drawable))
                        }
                        val barDataSet = BarDataSet(barEntries, getString(R.string.statistics_symbol_top_ten))
                        val iValueFormatter = IValueFormatterExt(context)
                        barDataSet.valueFormatter = iValueFormatter
//                    val colors = intArrayOf(
//                        Color.rgb(255, 102, 0), Color.rgb(245, 199, 0),
//                        Color.rgb(106, 150, 31), Color.rgb(179, 100, 53), Color.rgb(115, 130, 153)
//                    )
                        barDataSet.setColors(*colorItems.toIntArray())
                        barDataSet.setDrawIcons(true)
                        barDataSet.setDrawValues(false)
                        val dataSets = ArrayList<IBarDataSet>()
                        dataSets.add(barDataSet)

                        val barData = BarData(dataSets)
                        barData.setValueTextSize(10f)
//        barData.setValueTypeface(mTfLight)
                        barData.barWidth = 0.9f
//        barChart.zoom((sortedMap.size / 6.0F), 0F, 0F, 0F)
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
        override fun getFormattedValue(
            value: Float,
            axis: AxisBase?,
        ): String =
            when {
                value > 0 && value <= mSequences.size -> mSymbolMap[mSequences[value.toInt() - 1]] ?: "None"
                else -> "None"
            }
    }
}
