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
import me.blog.korn123.easydiary.chart.DiaryCountingAxisValueFormatter
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
class SymbolBarChartFragment : Fragment() {
    private lateinit var barChart: BarChart
    private lateinit var chartTitle: FixedTextView
    private lateinit var barChartProgressBar: ContentLoadingProgressBar
    private lateinit var symbolMap: HashMap<Int, String>
    private val sequences = arrayListOf<Int>()
    private val typeface: Typeface?
        get() = FontUtils.getCommonTypeface(requireContext())
    private var coroutineJob: Job? = null
    private val diaryViewModel: DiaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_writing_barchart, container, false)

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
            typeface = this@SymbolBarChartFragment.typeface
            textColor = requireContext().config.textColor
            labelRotationAngle = -45F
            setDrawGridLines(false)
            granularity = 1f
            labelCount = 7
            valueFormatter = xAxisFormatter
        }

        barChart.axisLeft.run {
            typeface = this@SymbolBarChartFragment.typeface
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            valueFormatter = DiaryCountingAxisValueFormatter(context)
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 15f
            axisMinimum = 0f
        }

        barChart.axisRight.run {
            setDrawGridLines(false)
            typeface = this@SymbolBarChartFragment.typeface
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            valueFormatter = DiaryCountingAxisValueFormatter(context)
            spaceTop = 15f
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
            typeface = this@SymbolBarChartFragment.typeface
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
                                ).putExtra(StatisticsConstants.CHART_MODE, StatisticsConstants.MODE_SINGLE_BAR_CHART_SYMBOL),
                            )
                        }, 300)
                    }
                }
            }
        }

        coroutineJob =
            lifecycleScope.launch(Dispatchers.IO) {
                val sortedMap = diaryViewModel.getSymbolUsedCountMap(true)
                withContext(Dispatchers.Main) {
                    val barEntries = ArrayList<BarEntry>()
                    var index = 1.0F
                    sortedMap.forEach { (key, value) ->
                        val drawable: Drawable? =
                            when (FlavorUtils.sequenceToSymbolResourceId(key) > 0) {
                                true -> scaledDrawable(FlavorUtils.sequenceToSymbolResourceId(key), requireContext().dpToPixel(20F), requireContext().dpToPixel(20F))
                                false -> null
                            }
                        sequences.add(key)
                        barEntries.add(BarEntry(index++, value.toFloat(), drawable))
                    }
                    if (barEntries.isNotEmpty()) {
                        val barDataSet = BarDataSet(barEntries, getString(R.string.statistics_symbol_all))
                        val iValueFormatter = IValueFormatterExt(context)
                        barDataSet.valueFormatter = iValueFormatter
                        val colorItems = arrayListOf<Int>()
                        val defaultColor = Color.rgb(209, 232, 255)
                        for (i in 0..4) {
                            colorItems.add(defaultColor.darkenColor(i * 2))
                        }
                        barDataSet.setColors(*colorItems.reversed().toIntArray())
                        barDataSet.setDrawIcons(true)
                        barDataSet.setDrawValues(false)
                        val dataSets = ArrayList<IBarDataSet>()
                        dataSets.add(barDataSet)

                        val barData = BarData(dataSets)
                        barData.setValueTextSize(10f)
                        barData.setValueTypeface(this@SymbolBarChartFragment.typeface)
                        barData.setValueTextColor(requireContext().config.textColor)
                        barData.barWidth = 0.9f
                        barChart.zoom((sortedMap.size / 8.5F), 0F, 0F, 0F)
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
