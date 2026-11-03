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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.StatisticsActivity
import me.blog.korn123.easydiary.chart.IValueFormatterExt
import me.blog.korn123.easydiary.chart.XYMarkerView
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.darkenColor
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.scaledDrawable
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.helper.ChartConstants
import me.blog.korn123.easydiary.helper.StatisticsConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.diary.DiaryViewModel
import me.blog.korn123.easydiary.utils.FontUtils
import me.blog.korn123.easydiary.views.FixedTextView

@AndroidEntryPoint
class SymbolHorizontalBarChartFragment : Fragment() {
    private lateinit var barChart: BarChart
    private lateinit var chartTitle: FixedTextView
    private lateinit var barChartProgressBar: ContentLoadingProgressBar
    private lateinit var symbolMap: HashMap<Int, String>
    private var coroutineJob: Job? = null
    private val typeface: Typeface?
        get() = FontUtils.getCommonTypeface(requireContext())
    val sequences = arrayListOf<Int>()
    private val diaryViewModel: DiaryViewModel by viewModels()

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
        barChart = view.findViewById(R.id.barChart)
        chartTitle = view.findViewById(R.id.chartTitle)
        barChartProgressBar = view.findViewById(R.id.barChartProgressBar)

        symbolMap = FlavorUtils.getDiarySymbolMap(requireContext())
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.description.isEnabled = false
        barChart.setMaxVisibleValueCount(60)
        barChart.setPinchZoom(false)

        val xAxisFormatter = AxisValueFormatter()
        barChart.xAxis.run {
            position = XAxis.XAxisPosition.BOTTOM
            typeface = this@SymbolHorizontalBarChartFragment.typeface
            textColor = requireContext().config.textColor
            setDrawGridLines(false)
            granularity = 1f
            labelCount = 7
            valueFormatter = xAxisFormatter
        }

        barChart.axisLeft.run {
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            axisMinimum = 0f
            typeface = this@SymbolHorizontalBarChartFragment.typeface
            textColor = requireContext().config.textColor
        }

        barChart.axisRight.run {
            setDrawGridLines(false)
            axisMinimum = 0f
        }

        barChart.legend.run {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            form = Legend.LegendForm.SQUARE
            formSize = 9f
            textSize = 11f
            xEntrySpace = 4f
            typeface = this@SymbolHorizontalBarChartFragment.typeface
            textColor = requireContext().config.textColor
        }

        val mv = XYMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = barChart
        barChart.marker = mv

        arguments?.let { bundle ->
            val title = bundle.getString(ChartConstants.CHART_TITLE)
            if (title != null) {
                chartTitle.text = title
                chartTitle.visibility = View.VISIBLE

                getView()?.findViewById<ImageView>(R.id.image_expend_chart)?.let {
                    it.visibility = View.VISIBLE
                    requireActivity().updateDrawableColorInnerCardView(it, config.textColor)
                    it.setOnClickListener { v ->
                        v.postDelayed({
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

        coroutineJob =
            lifecycleScope.launch(Dispatchers.IO) {
                val sortedMap = diaryViewModel.getSymbolUsedCountMap(true)
                val barEntries = ArrayList<BarEntry>()
                var index = 1F
                val itemArray = arrayListOf<HashMap<String, Int>>()
                sortedMap.forEach { (key, value) ->
                    if (index > 10) return@forEach
                    itemArray.add(hashMapOf("key" to key, "value" to value))
                    sequences.add(key)
                    index++
                }
                itemArray.reverse()
                sequences.reverse()

                withContext(Dispatchers.Main) {
                    if (itemArray.isNotEmpty()) {
                        val colorItems = arrayListOf<Int>()
                        val defaultColor = Color.rgb(209, 232, 255)
                        itemArray.forEachIndexed { itemIndex, item ->
                            colorItems.add(defaultColor.darkenColor(itemIndex * 2))
                            val drawable: Drawable? =
                                when (FlavorUtils.sequenceToSymbolResourceId(item["key"]!!) > 0) {
                                    true -> scaledDrawable(FlavorUtils.sequenceToSymbolResourceId(item["key"]!!), requireContext().dpToPixel(24F), requireContext().dpToPixel(24F))
                                    false -> null
                                }
                            barEntries.add(BarEntry((itemIndex + 1F), item["value"]!!.toFloat(), drawable))
                        }
                        val barDataSet = BarDataSet(barEntries, getString(R.string.statistics_symbol_top_ten))
                        val iValueFormatter = IValueFormatterExt(context)
                        barDataSet.valueFormatter = iValueFormatter
                        barDataSet.setColors(*colorItems.toIntArray())
                        barDataSet.setDrawIcons(true)
                        barDataSet.setDrawValues(false)
                        val dataSets = ArrayList<IBarDataSet>()
                        dataSets.add(barDataSet)

                        val barData = BarData(dataSets)
                        barData.setValueTextSize(10f)
                        barData.barWidth = 0.9f
                        barChart.data = barData

                        barChart.animateY(2000)
                    }
                    barChartProgressBar.visibility = View.GONE
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob?.run { if (isActive) cancel() }
    }

    inner class AxisValueFormatter : IAxisValueFormatter {
        override fun getFormattedValue(
            value: Float,
            axis: AxisBase?,
        ): String =
            when {
                value > 0 && value <= sequences.size -> symbolMap[sequences[value.toInt() - 1]] ?: "None"
                else -> "None"
            }
    }
}
