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
import me.blog.korn123.commons.utils.DateUtils.DATE_PATTERN_DASH
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.StatisticsActivity
import me.blog.korn123.easydiary.databinding.FragmentStockLineChartBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.helper.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class StockLineChartFragment : androidx.fragment.app.Fragment() {
    private lateinit var mBinding: FragmentStockLineChartBinding
    private lateinit var mLineChart: LineChart
    private val mTimeMillisMap = hashMapOf<Int, Long>()
    private var mCoroutineJob: Job? = null
    private val mDataSets = ArrayList<ILineDataSet>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentStockLineChartBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIXME: When ViewBinding is used, the MATCH_PARENT option declared in the layout does not work, so it is temporarily declared here.
        mBinding.root.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mBinding.root.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        mLineChart = mBinding.lineChart
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
        mLineChart.extraBottomOffset = 10F
        mLineChart.extraRightOffset = 10F
        mLineChart.xAxis.run {
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            labelRotationAngle = -45F
            granularity = 1f // only intervals of 1 day
            labelCount = 5
            valueFormatter = xAxisFormatter
        }

        val yAxisFormatter = WeightYAxisValueFormatter(context)
        mLineChart.axisLeft.run {
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            valueFormatter = yAxisFormatter
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 0f
            axisMinimum = 0f // this replaces setStartAtZero(true)
            labelCount = 8
            setDrawGridLines(false)
        }

        mLineChart.axisRight.run {
            isEnabled = false
//            setDrawGridLines(false)
//            typeface = FontUtils.getCommonTypeface(requireContext())
//            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
//            textColor = requireContext().config.textColor
//            setLabelCount(8, false)
//            valueFormatter = yAxisFormatter
//            spaceTop = 0f
//            axisMinimum = 0f // this replaces setStartAtZero(true)
//            labelCount = 8
        }

        mLineChart.legend.run {
//            isEnabled = false
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
//            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
//            setDrawInside(false)
            form = Legend.LegendForm.CIRCLE
//            formSize = 9f
//            xEntrySpace = 4f
            isWordWrapEnabled = true
//            xOffset = 5F
        }

        val mv = WeightMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = mLineChart // For bounds control
        mLineChart.marker = mv // Set the marker to the chart

        // determine title parameter
        arguments?.let { bundle ->
            val title = bundle.getString(CHART_TITLE)
            if (title != null) {
                mBinding.chartTitle.text = title
                mBinding.chartTitle.visibility = View.VISIBLE
                getView()?.findViewById<ImageView>(R.id.image_stock_symbol)?.let {
                    it.visibility = View.VISIBLE
                    FlavorUtils.initWeatherView(requireActivity(), it, DAILY_STOCK)
                }
                getView()?.findViewById<ImageView>(R.id.image_expend_chart)?.let {
                    it.visibility = View.VISIBLE
                    requireActivity().updateDrawableColorInnerCardView(it, config.textColor)
                    it.setOnClickListener { view ->
                        view.postDelayed( {
                            TransitionHelper.startActivityWithTransition(
                                requireActivity(),
                                Intent(
                                    requireActivity(),
                                    StatisticsActivity::class.java
                                ).putExtra(StatisticsActivity.CHART_MODE, StatisticsActivity.MODE_SINGLE_LINE_CHART_STOCK)
                            )
                        }, 300)
                    }
                }
            }
        }

        drawChart()
    }

    private fun drawChart() {
        mCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
            mDataSets.clear()
            setData()
            if (sumDataSetSize > 0) {
                withContext(Dispatchers.Main) {
//                    val lineDataSet = LineDataSet(barEntries, "Evaluated Price")
//                    val iValueFormatter = WeightIValueFormatter(context)
//                    lineDataSet.valueFormatter = iValueFormatter
//                    lineDataSet.setDrawIcons(false)
//                    lineDataSet.setDrawValues(true)
//                    lineDataSet.setDrawFilled(true)
//                    mDataSets.add(lineDataSet)
//                    val lineData = LineData(mDataSets)
//                    lineData.setValueTextSize(CHART_LABEL_FONT_SIZE_DEFAULT_DP)
//                    lineData.setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
//                    lineData.setValueTextColor(requireContext().config.textColor)
//                    val color = if (barEntries[0].y < barEntries[barEntries.size.minus(1)].y) Color.RED else Color.rgb(0, 0, 139)
//                    lineDataSet.circleColors = arrayListOf(color)
//                    lineDataSet.color = color
//                    lineDataSet.fillColor = color
                    val lineData = LineData(mDataSets)
                    lineData.setValueTextSize(10f)
                    lineData.setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                    lineData.setDrawValues(false)
                    mLineChart.data = lineData
                    mLineChart.animateY(600)
                }
            }

            withContext(Dispatchers.Main) {
                mBinding.barChartProgressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCoroutineJob?.run { if (isActive) cancel() }
    }

    var sumDataSetSize = 0
    private fun setData() {
        val krPrincipalEntries = arrayListOf<Entry>()
        val usPrincipalEntries = arrayListOf<Entry>()
        val totalPrincipalEntries = arrayListOf<Entry>()
        val krEvaluatedPriceEntries = arrayListOf<Entry>()
        val usEvaluatedPriceEntries = arrayListOf<Entry>()
        val totalEvaluatedPriceEntries = arrayListOf<Entry>()
        EasyDiaryDbHelper.getTemporaryInstance().let { realmInstance ->
            val listDiary = EasyDiaryDbHelper.findDiary(null, false, 0, 0, DAILY_STOCK, realmInstance = realmInstance)
            var index = 0
            var totalSum = 0F
            listDiary.reversed().forEach { diaryDto ->
                diaryDto.title?.let {
                    if (EasyDiaryUtils.isStockNumber(it)) {
                        try {
                            val amountArray = it.split(",")
                            val krEvaluatedPrice = amountArray[0].toFloat()
                            val usEvaluatedPrice = amountArray[1].toFloat()
                            val krPrincipal = if (amountArray.size > 2) amountArray[2].toFloat() else 4000000F
                            val usPrincipal = if (amountArray.size > 3) amountArray[3].toFloat() else 4000000F
                            val sum = krEvaluatedPrice.plus(usEvaluatedPrice)
                            totalSum += sum
                            krPrincipalEntries.add(Entry(index.toFloat(), krPrincipal))
                            krEvaluatedPriceEntries.add(Entry(index.toFloat(), krEvaluatedPrice))
                            usPrincipalEntries.add(Entry(index.toFloat(), usPrincipal))
                            usEvaluatedPriceEntries.add(Entry(index.toFloat(), usEvaluatedPrice))
                            totalPrincipalEntries.add(Entry(index.toFloat(), krPrincipal.plus(usPrincipal)))
                            totalEvaluatedPriceEntries.add(Entry(index.toFloat(), sum))
                            mTimeMillisMap[index] = diaryDto.currentTimeMillis
                            index++
                        } catch (e: Exception) { Log.i(AAF_TEST, e.message ?: "") }
                    }
                }
            }
            if (index > 0) {
                val average = totalSum.div(index)
//                mLineChart.axisLeft.axisMinimum = average.minus(5000000)
                mLineChart.axisLeft.axisMaximum = average.plus(3000000)
//                mLineChart.axisRight.axisMinimum = average.minus(5000000)
                mLineChart.axisRight.axisMaximum = average.plus(3000000)
                sumDataSetSize = totalEvaluatedPriceEntries.size
                val plusColor = Color.rgb(200, 0, 0)
                val minusColor = Color.rgb(0, 0, 204)

                val krPrincipalDataSet = LineDataSet(krPrincipalEntries, "KR/JP-P").apply {
                    Color.argb(100, 255, 102, 0).also {
                        color = it
                        setCircleColor(it)
                        setCircleColorHole(it)
                    }
                }
                val krEvaluatedPriceDataSet = LineDataSet(krEvaluatedPriceEntries, "KR/JP-EP").apply {
                    Color.argb(255, 255, 102, 0).also {
                        color = it
                        setCircleColor(it)
                        setCircleColorHole(it)
                    }
                }
                val usPrincipalDataSet = LineDataSet(usPrincipalEntries, "US-P").apply {
                    Color.argb(100, 102, 0, 120).also {
                        color = it
                        setCircleColor(it)
                        setCircleColorHole(it)
                    }
                }
                val usEvaluatedPriceDataSet = LineDataSet(usEvaluatedPriceEntries, "US-EP").apply {
                    Color.argb(255, 102, 0, 120).also {
                        color = it
                        setCircleColor(it)
                        setCircleColorHole(it)
                    }
                }
                val totalPrincipalDataSet = LineDataSet(totalPrincipalEntries, "Total-P").apply {
                    Color.argb(100, 0, 0, 0).also {
                        color = it
                        setCircleColor(it)
                        setCircleColorHole(it)
                    }
                }
                val totalEvaluatedPriceDataSet = LineDataSet(totalEvaluatedPriceEntries, "Total-EP").apply {
                    (if (totalEvaluatedPriceEntries[totalEvaluatedPriceEntries.size.minus(1)].y > totalPrincipalEntries[totalPrincipalEntries.size.minus(1)].y) plusColor else minusColor).also {
                        color = it
                        setCircleColor(it)
                        setCircleColorHole(it)
                    }
                }
                mDataSets.add(krPrincipalDataSet)
                mDataSets.add(krEvaluatedPriceDataSet)
                mDataSets.add(usPrincipalDataSet)
                mDataSets.add(usEvaluatedPriceDataSet)
                mDataSets.add(totalPrincipalDataSet)
                mDataSets.add(totalEvaluatedPriceDataSet)
            }
            realmInstance.close()
        }
    }

    private fun xAxisTimeMillisToDate(timeMillis: Long): String =
        if (timeMillis > 0) DateUtils.getDateStringFromTimeMillis(timeMillis, SimpleDateFormat.MEDIUM) else "N/A"

    private fun fillValueForward(averageInfo: ArrayList<Float>) {
        Log.i(AAF_TEST, "원본 ${averageInfo.joinToString(",")}")
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
        Log.i(AAF_TEST, "앞 ${averageInfo.joinToString(",")}")
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
        Log.i(AAF_TEST, "뒤 ${averageInfo.joinToString(",")}")
    }

    private fun getCurrencyFormat() = NumberFormat.getCurrencyInstance(Locale.KOREA)

    companion object {
        const val CHART_TITLE = "chartTitle"
    }

    inner class WeightXAxisValueFormatter(private var context: Context?) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            val timeMillis: Long = mTimeMillisMap[value.toInt()] ?: 0
            return xAxisTimeMillisToDate(timeMillis)
        }

    }

    inner class WeightYAxisValueFormatter(private var context: Context?) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return getCurrencyFormat().format(value)
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
            return getCurrencyFormat().format(value)
        }
    }

    inner class WeightMarkerView(context: Context, private val xAxisValueFormatter: IAxisValueFormatter) : MarkerView(context, R.layout.partial_custom_marker_view) {
        private val tvContent: TextView = findViewById(R.id.tvContent)

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            e?.let { entry ->
                tvContent.run {
                    text = "${xAxisValueFormatter.getFormattedValue(entry.x, mLineChart.xAxis)}: ${getCurrencyFormat().format(entry.y)}"
                    typeface = FontUtils.getCommonTypeface(context)
                    textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
                }
                super.refreshContent(entry, highlight)
            }
        }

        override fun getOffset(): MPPointF {
            return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }
    }
}