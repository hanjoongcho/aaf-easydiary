package me.blog.korn123.easydiary.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.ViewPortHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.StatisticsActivity
import me.blog.korn123.easydiary.databinding.FragmentWeightLineChartBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.ChartConstants
import me.blog.korn123.easydiary.helper.DAILY_SCALE
import me.blog.korn123.easydiary.helper.StatisticsConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.diary.DiaryViewModel
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import java.text.SimpleDateFormat
import kotlin.random.Random
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

@AndroidEntryPoint
class WeightLineChartFragment : Fragment() {
    private lateinit var binding: FragmentWeightLineChartBinding
    private lateinit var lineChart: LineChart
    private val timeMillisMap = hashMapOf<Int, Long>()
    private var coroutineJob: Job? = null
    private var chartMode = "A"
    private val dataSets = ArrayList<ILineDataSet>()
    private val diaryViewModel: DiaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWeightLineChartBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.root.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        lineChart = binding.lineChart
        lineChart.description.isEnabled = false
        lineChart.setMaxVisibleValueCount(10)
        lineChart.setPinchZoom(false)

        val xAxisFormatter = WeightXAxisValueFormatter(context)
        lineChart.xAxis.run {
            position = XAxis.XAxisPosition.BOTTOM
            typeface = FontUtils.getCommonTypeface(requireContext())
            textColor = requireContext().config.textColor
            labelRotationAngle = -45F
            setDrawGridLines(false)
            granularity = 1f
            labelCount = 7
            valueFormatter = xAxisFormatter
        }

        val yAxisFormatter = WeightYAxisValueFormatter(context)
        lineChart.axisLeft.run {
            typeface = FontUtils.getCommonTypeface(requireContext())
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            valueFormatter = yAxisFormatter
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 0f
            axisMinimum = 0f
            labelCount = 8
        }

        lineChart.axisRight.run {
            setDrawGridLines(false)
            typeface = FontUtils.getCommonTypeface(requireContext())
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            valueFormatter = yAxisFormatter
            spaceTop = 0f
            axisMinimum = 0f
            labelCount = 8
        }

        lineChart.legend.run {
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

        val mv = WeightMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = lineChart
        lineChart.marker = mv

        arguments?.let { bundle ->
            val title = bundle.getString(ChartConstants.CHART_TITLE)
            if (title != null) {
                binding.chartTitle.text = title
                binding.chartTitle.visibility = View.VISIBLE
                getView()?.findViewById<ImageView>(R.id.image_weight_symbol)?.let {
                    it.visibility = View.VISIBLE
                    FlavorUtils.initWeatherView(requireActivity(), it, DAILY_SCALE)
                }
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
                                ).putExtra(StatisticsConstants.CHART_MODE, StatisticsConstants.MODE_SINGLE_LINE_CHART_WEIGHT),
                            )
                        }, 300)
                    }
                }
            }
        }

        drawChart()

        binding.run {
            radioGroupChartOption.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_button_option_a -> {
                        chartMode = "A"
                        drawChart()
                    }

                    R.id.radio_button_option_b -> {
                        chartMode = "B"
                        drawChart()
                    }
                }
            }
            checkOptionsFill.setTextColor(requireContext().config.textColor)
            checkOptionsFill.setOnCheckedChangeListener { _, isChecked ->
                dataSets.forEach {
                    it.setDrawFilled(isChecked)
                }
                lineChart.invalidate()
            }
        }
    }

    private fun drawChart() {
        coroutineJob =
            lifecycleScope.launch(Dispatchers.IO) {
                dataSets.clear()
                if (chartMode == "A") {
                    val barEntries = setData()
                    if (barEntries.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            val lineDataSet = LineDataSet(barEntries, "Weight")
                            val iValueFormatter = WeightIValueFormatter(context)
                            lineDataSet.valueFormatter = iValueFormatter
                            lineDataSet.setDrawIcons(false)
                            lineDataSet.setDrawValues(true)
                            lineDataSet.setDrawFilled(true)
                            dataSets.add(lineDataSet)
                            val lineData = LineData(dataSets)
                            lineData.setValueTextSize(10f)
                            lineData.setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                            requireContext().config.primaryColor.also { color ->
                                lineDataSet.circleColors = arrayListOf(color)
                                lineDataSet.color = color
                                lineDataSet.fillColor = color
                            }
                            lineChart.data = lineData
                            lineChart.animateY(600)
                            binding.barChartProgressBar.visibility = View.GONE
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.barChartProgressBar.visibility = View.GONE
                        }
                    }
                } else {
                    val listDiary =
                        diaryViewModel.findDiary(
                            null,
                            false,
                            0,
                            0,
                            DAILY_SCALE,
                        )

                    var sumWeight = 0F
                    val filteredItems = arrayListOf<DiaryDomain>()
                    listDiary.reversed().forEach { diary ->
                        diary.title?.let {
                            if (EasyDiaryUtils.isContainNumber(it)) {
                                val weight = EasyDiaryUtils.findNumber(it)
                                sumWeight += weight
                                filteredItems.add(diary)
                            }
                        }
                    }

                    val yearlyMap = filteredItems.groupBy { item -> item.dateString!!.substring(0, 4) }
                    val iterator = yearlyMap.iterator()
                    var itemIndex = yearlyMap.count()
                    while (iterator.hasNext()) {
                        val element = iterator.next()
                        val barEntries = ArrayList<Entry>()

                        val monthMap = element.value.groupBy { it.dateString!!.substring(5, 7) }

                        fun monthlyWeight(key: String): Float =
                            monthMap[key]?.let { monthlyItems ->
                                var average = 0F
                                var sum = 0F
                                monthlyItems.map { sum += EasyDiaryUtils.findNumber(it.title) }
                                average = sum.div(monthlyItems.size)
                                average
                            } ?: 0F

                        val averageInfo =
                            arrayListOf<Float>().apply {
                                for (i in 1..12) {
                                    add(monthlyWeight("$i".padStart(2, '0')))
                                }
                            }
                        for (i in 1..12) {
                            if (averageInfo[i.minus(1)] > 0f) barEntries.add(Entry(i.toFloat(), averageInfo[i.minus(1)]))
                        }
                        val lineDataSet = LineDataSet(barEntries, element.key)
                        val iValueFormatter = WeightIValueFormatter(context)
                        lineDataSet.valueFormatter = iValueFormatter
                        lineDataSet.setDrawIcons(false)
                        lineDataSet.setDrawValues(true)
                        lineDataSet.setDrawFilled(true)
                        Color
                            .argb(
                                50,
                                Random.nextInt(0, 255),
                                Random.nextInt(0, 255),
                                Random.nextInt(0, 255),
                            ).also {
                                var color = it
                                if (itemIndex == 1) {
                                    color = requireContext().config.primaryColor
                                    lineDataSet.setCircleColorHole(color)
                                }
                                lineDataSet.circleColors = arrayListOf(color)
                                lineDataSet.color = color
                                lineDataSet.fillColor = color
                            }
                        dataSets.add(lineDataSet)
                        itemIndex--
                    }
                    withContext(Dispatchers.Main) {
                        if (sumWeight > 0) {
                            val average = sumWeight.div(filteredItems.size)
                            lineChart.axisLeft.axisMinimum = average.minus(10)
                            lineChart.axisLeft.axisMaximum = average.plus(10)
                            lineChart.axisRight.axisMinimum = average.minus(10)
                            lineChart.axisRight.axisMaximum = average.plus(10)
                        }

                        val lineData = LineData(dataSets)
                        lineData.setValueTextSize(10f)
                        lineData.setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                        lineChart.data = lineData
                        lineChart.animateY(600)
                        binding.barChartProgressBar.visibility = View.GONE
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob?.run { if (isActive) cancel() }
    }

    private suspend fun setData(): ArrayList<Entry> {
        val barEntries = ArrayList<Entry>()
        val listDiary = diaryViewModel.findDiary(null, false, 0, 0, DAILY_SCALE)
        var index = 0
        var sumWeight = 0F
        listDiary.reversed().forEach { diaryDto ->
            diaryDto.title?.let {
                if (EasyDiaryUtils.isContainNumber(it)) {
                    val weight = EasyDiaryUtils.findNumber(it)
                    sumWeight += weight
                    barEntries.add(Entry(index.toFloat(), weight))
                    timeMillisMap[index] = diaryDto.currentTimeMillis
                    index++
                }
            }
        }
        if (index > 0) {
            val average = sumWeight.div(index)
            lineChart.axisLeft.axisMinimum = average.minus(10)
            lineChart.axisLeft.axisMaximum = average.plus(10)
            lineChart.axisRight.axisMinimum = average.minus(10)
            lineChart.axisRight.axisMaximum = average.plus(10)
        }
        return barEntries
    }

    private fun xAxisTimeMillisToDate(timeMillis: Long): String = if (timeMillis > 0) DateUtils.getDateStringFromTimeMillis(timeMillis, SimpleDateFormat.MEDIUM) else "N/A"

    private fun fillValueForward(averageInfo: ArrayList<Float>) {
        Log.i(AAF_TEST, "Original ${averageInfo.joinToString(",")}")
        averageInfo.forEachIndexed { index, fl ->
            if (fl == 0f) {
                up@ for (seq in index..averageInfo.size.minus(1)) {
                    if (averageInfo[seq] > 0F) {
                        averageInfo[index] = averageInfo[seq]
                        break@up
                    }
                }
            }
        }
        Log.i(AAF_TEST, "Forward ${averageInfo.joinToString(",")}")
    }

    private fun fillValueBackward(averageInfo: ArrayList<Float>) {
        averageInfo.forEachIndexed { index, fl ->
            if (fl == 0f) {
                down@ for (seq in index.minus(1) downTo 0) {
                    if (averageInfo[seq] > 0F) {
                        averageInfo[index] = averageInfo[seq]
                        break@down
                    }
                }
            }
        }
        Log.i(AAF_TEST, "Backward ${averageInfo.joinToString(",")}")
    }

    inner class WeightXAxisValueFormatter(
        private var context: Context?,
    ) : IAxisValueFormatter {
        override fun getFormattedValue(
            value: Float,
            axis: AxisBase,
        ): String {
            val label =
                when (chartMode == "A") {
                    true -> {
                        val timeMillis: Long = timeMillisMap[value.toInt()] ?: 0
                        xAxisTimeMillisToDate(timeMillis)
                    }

                    false -> {
                        value.toInt().toString().padStart(2, '0')
                    }
                }
            return label
        }
    }

    inner class WeightYAxisValueFormatter(
        private var context: Context?,
    ) : IAxisValueFormatter {
        override fun getFormattedValue(
            value: Float,
            axis: AxisBase,
        ): String = "${value}kg"
    }

    inner class WeightIValueFormatter(
        private var context: Context?,
    ) : IValueFormatter {
        override fun getFormattedValue(
            value: Float,
            entry: Entry,
            dataSetIndex: Int,
            viewPortHandler: ViewPortHandler,
        ): String = "${value}kg"
    }

    inner class WeightMarkerView(
        context: Context,
        private val xAxisValueFormatter: IAxisValueFormatter,
    ) : MarkerView(context, R.layout.partial_custom_marker_view) {
        private val tvContent: TextView = findViewById(R.id.tvContent)

        override fun refreshContent(
            e: Entry?,
            highlight: Highlight?,
        ) {
            e?.let { entry ->

                tvContent.run {
                    text = "${xAxisValueFormatter.getFormattedValue(entry.x, lineChart.xAxis)}: ${entry.y}kg"
                    typeface = FontUtils.getCommonTypeface(context)
                }
                super.refreshContent(entry, highlight)
            }
        }

        override fun getOffset(): MPPointF = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}
