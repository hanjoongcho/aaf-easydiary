package me.blog.korn123.easydiary.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
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
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.StatisticsActivity
import me.blog.korn123.easydiary.chart.DiaryCountingAxisValueFormatter
import me.blog.korn123.easydiary.chart.IValueFormatterExt
import me.blog.korn123.easydiary.chart.XYMarkerView
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.views.FixedTextView

class WritingBarChartFragment : androidx.fragment.app.Fragment() {
    private lateinit var mBarChart: BarChart
    private lateinit var mChartTitle: FixedTextView
    private lateinit var mBarChartProgressBar: ContentLoadingProgressBar
    private var mCoroutineJob: Job? = null

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
        mBarChart.xAxis.run {
            position = XAxis.XAxisPosition.BOTTOM
            typeface = FontUtils.getCommonTypeface(requireContext())
            textColor = requireContext().config.textColor
            labelRotationAngle = -45F
            setDrawGridLines(false)
            granularity = 1f // only intervals of 1 day
            labelCount = 7
            valueFormatter = xAxisFormatter
        }

        val diaryCountingAxisValueFormatter = DiaryCountingAxisValueFormatter(context)
        mBarChart.axisLeft.run {
            typeface = FontUtils.getCommonTypeface(requireContext())
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            valueFormatter = diaryCountingAxisValueFormatter
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 15f
            axisMinimum = 0f // this replaces setStartAtZero(true)
        }

        mBarChart.axisRight.run {
            setDrawGridLines(false)
            typeface = FontUtils.getCommonTypeface(requireContext())
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            valueFormatter = diaryCountingAxisValueFormatter
            spaceTop = 15f
            axisMinimum = 0f // this replaces setStartAtZero(true)
        }

        mBarChart.legend.run {
            typeface = FontUtils.getCommonTypeface(requireContext())
            textColor = requireContext().config.textColor
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            form = Legend.LegendForm.SQUARE
            formSize = 9f
            textSize = 11f
            xEntrySpace = 4f
        }

        val mv = XYMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = mBarChart // For bounds control
        mBarChart.marker = mv // Set the marker to the chart

        // determine title parameter
        arguments?.let { bundle ->
            val title = bundle.getString(CHART_TITLE)
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
                                ),
                            )
                        }, 300)
                    }
                }
            }
        }

        mCoroutineJob =
            lifecycleScope.launch(Dispatchers.IO) {
                val barEntries = setData()
                withContext(Dispatchers.Main) {
                    if (barEntries.isNotEmpty()) {
                        val barDataSet = BarDataSet(barEntries, getString(R.string.statistics_creation_time))
                        val iValueFormatter = IValueFormatterExt(context)
                        barDataSet.valueFormatter = iValueFormatter
                        val colors =
                            intArrayOf(
                                Color.rgb(0, 19, 26),
                                Color.rgb(0, 71, 96),
                                Color.rgb(0, 117, 158),
                                Color.rgb(0, 176, 240),
                                Color.rgb(0, 161, 218),
                                Color.rgb(0, 117, 158),
                                Color.rgb(0, 71, 96),
                                Color.rgb(0, 19, 26),
                            )
                        barDataSet.setColors(*colors)
                        barDataSet.setDrawIcons(false)
                        barDataSet.setDrawValues(true)
                        val dataSets = ArrayList<IBarDataSet>()
                        dataSets.add(barDataSet)
                        val barData = BarData(dataSets)
                        barData.setValueTextSize(10f)
                        barData.setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                        barData.setValueTextColor(requireContext().config.textColor)
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

    private fun setData(count: Int = 8): ArrayList<BarEntry> {
        val barEntries = ArrayList<BarEntry>()
        EasyDiaryDbHelper.getTemporaryInstance().let { realmInstance ->
            val listDiary = EasyDiaryDbHelper.findDiary(null, realmInstance = realmInstance)
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
            realmInstance.close()
        }
        return barEntries
    }

    private fun hourToItemNumber(hour: Int): Int =
        when (hour) {
            in 1..3 -> 1
            in 4..6 -> 2
            in 7..9 -> 3
            in 10..12 -> 4
            in 13..15 -> 5
            in 16..18 -> 6
            in 19..21 -> 7
            else -> 8
        }

    companion object {
        const val CHART_TITLE = "chartTitle"
    }

    class DayAxisValueFormatter(
        private var context: Context?,
        private val chart: BarLineChartBase<*>,
    ) : IAxisValueFormatter {
        override fun getFormattedValue(
            value: Float,
            axis: AxisBase?,
        ): String =
            when (value.toInt()) {
                1 -> context!!.getString(R.string.range_a)
                2 -> context!!.getString(R.string.range_b)
                3 -> context!!.getString(R.string.range_c)
                4 -> context!!.getString(R.string.range_d)
                5 -> context!!.getString(R.string.range_e)
                6 -> context!!.getString(R.string.range_f)
                7 -> context!!.getString(R.string.range_g)
                else -> context!!.getString(R.string.range_h)
            }
    }
}
