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
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.StatisticsActivity
import me.blog.korn123.easydiary.databinding.FragmentStockLineChartBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.CHART_LABEL_FONT_SIZE_DEFAULT_DP
import me.blog.korn123.easydiary.helper.DAILY_STOCK
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.TransitionHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class StockLineChartFragment : androidx.fragment.app.Fragment() {
    private lateinit var mBinding: FragmentStockLineChartBinding
    private lateinit var mLineChart: LineChart
    private lateinit var mKospiChart: LineChart
    private val mTimeMillisMap = hashMapOf<Int, Long>()
    private var mCoroutineJob: Job? = null
    private val mDataSets = ArrayList<ILineDataSet>()
    private val mKospiDataSets = ArrayList<ILineDataSet>()
    private var mTotalDataSetCnt = 0
    private var mChartMode = "A"

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

        mKospiChart = mBinding.chartKospi
        mKospiChart.description.isEnabled = false
//        mKospiChart.extraBottomOffset = 30F

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mLineChart.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        mLineChart.setPinchZoom(false)

//        barChart.setDrawGridBackground(true)
        // mChart.setDrawYLabels(false);
//        barChart.zoom(1.5F, 0F, 0F, 0F)

        mLineChart.extraBottomOffset = 10F
        mLineChart.extraRightOffset = 10F
        mLineChart.xAxis.run {
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            labelRotationAngle = -65F
            granularity = 1f // only intervals of 1 day
            labelCount = 5
            valueFormatter = StockXAxisValueFormatter(context, SimpleDateFormat.SHORT)
        }
        mKospiChart.axisLeft.isEnabled = false
        mKospiChart.axisRight.isEnabled = false
        mKospiChart.xAxis.isEnabled = false
        mKospiChart.legend.isEnabled = false

        val yAxisFormatter = StockYAxisValueFormatter(context)
        mLineChart.axisLeft.run {
            isEnabled = false
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            valueFormatter = yAxisFormatter
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 0f
            axisMinimum = 0f // this replaces setStartAtZero(true)
            labelCount = 8
            setDrawGridLines(true)
        }

        mLineChart.axisRight.run {
            isEnabled = false
            setDrawGridLines(false)
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            valueFormatter = yAxisFormatter
            spaceTop = 0f
            axisMinimum = 0f // this replaces setStartAtZero(true)
            labelCount = 8
        }

        mLineChart.legend.run {
            isEnabled = false
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

        StockMarkerView(requireContext(), StockXAxisValueFormatter(context, SimpleDateFormat.FULL)).run {
            chartView = mLineChart   // For bounds control
            mLineChart.marker = this // Set the marker to the chart
        }

        KospiMarkerView(requireContext(), StockXAxisValueFormatter(context, SimpleDateFormat.FULL)).run {
            chartView = mKospiChart
            mKospiChart.marker = this
        }

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

        mBinding.run {
            radioGroupChartOption.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_button_option_a -> {
                        mChartMode = "A"
                        drawChart()
                    }
                    R.id.radio_button_option_b -> {
                        mChartMode = "B"
                        drawChart()
                    }
                    R.id.radio_button_option_c -> {
                        mChartMode = "C"
                        drawChart()
                    }
                }
            }
        }

        drawChart()
    }

    private fun drawChart() {
        mKospiChart.visibility = if (mChartMode === "A") View.VISIBLE else View.GONE
        mCoroutineJob?.run { if (isActive) cancel() }
        mCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
            mLineChart.highlightValue(null)
            mDataSets.clear()
            mKospiChart.clear()
            setData()
            if (mTotalDataSetCnt > 0) {
                withContext(Dispatchers.Main) {
                    val lineData = LineData(mDataSets)
                    lineData.setValueTextSize(10f)
                    lineData.setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                    lineData.setDrawValues(false)
                    mLineChart.data = lineData
                    mLineChart.animateY(600)

                    val kospiData = LineData(mKospiDataSets)
                    kospiData.setValueTextSize(10f)
                    kospiData.setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                    kospiData.setDrawValues(false)
                    mKospiChart.data = kospiData
                    mKospiChart.animateY(600)
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

    private fun setData() {
        val dark = Color.rgb(26, 26, 26)
        val plusColor = Color.rgb(204, 31, 8)
        val minusColor = Color.rgb(6, 57, 112)
        val colorPrincipal = Color.argb(255, 135, 62, 35)

        val krPrincipalEntries = arrayListOf<Entry>()
        val krEvaluatedPriceEntries = arrayListOf<Entry>()
        val krTradingProfitEntries = arrayListOf<Entry>()
        val krColors = arrayListOf<Int>()

        val usPrincipalEntries = arrayListOf<Entry>()
        val usEvaluatedPriceEntries = arrayListOf<Entry>()
        val usTradingProfitEntries = arrayListOf<Entry>()
        val usColors = arrayListOf<Int>()

        val totalPrincipalEntries = arrayListOf<Entry>()
        val totalEvaluatedPriceEntries = arrayListOf<Entry>()
        val totalTradingProfitEntries = arrayListOf<Entry>()
        val totalColors = arrayListOf<Int>()

        val kospiEntries = arrayListOf<Entry>()

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
                            var diff = krEvaluatedPrice.minus(krPrincipal)
                            krPrincipalEntries.add(Entry(index.toFloat(), krPrincipal))
                            krEvaluatedPriceEntries.add(Entry(index.toFloat(), krEvaluatedPrice))
                            krTradingProfitEntries.add(Entry(index.toFloat(), diff))
                            if (diff >= 0) krColors.add(plusColor) else krColors.add(minusColor)

                            usPrincipalEntries.add(Entry(index.toFloat(), usPrincipal))
                            usEvaluatedPriceEntries.add(Entry(index.toFloat(), usEvaluatedPrice))
                            diff = usEvaluatedPrice.minus(usPrincipal)
                            if (diff >= 0) usColors.add(plusColor) else usColors.add(minusColor)
                            usTradingProfitEntries.add(Entry(index.toFloat(), diff))

                            totalPrincipalEntries.add(Entry(index.toFloat(), krPrincipal.plus(usPrincipal)))
                            totalEvaluatedPriceEntries.add(Entry(index.toFloat(), sum))
                            diff = krEvaluatedPrice.plus(usEvaluatedPrice).minus(krPrincipal.plus(usPrincipal))
                            if (diff >= 0) totalColors.add(plusColor) else totalColors.add(minusColor)
                            totalTradingProfitEntries.add(Entry(index.toFloat(), diff))

                            if (amountArray.size > 4) kospiEntries.add(Entry(index.toFloat(), amountArray[4].toFloat()))

                            mTimeMillisMap[index] = diaryDto.currentTimeMillis
                            index++
                        } catch (e: Exception) { Log.i(AAF_TEST, e.message ?: "") }
                    }
                }
            }
            if (index > 0) {
                mTotalDataSetCnt = totalEvaluatedPriceEntries.size

                val krPrincipalDataSet = LineDataSet(krPrincipalEntries, "KR/JP Principal").apply {
                    color = colorPrincipal
                    setCircleColor(colorPrincipal)
//                    setCircleColorHole(colorPrincipal)
                }
                val krEvaluatedPriceDataSet = LineDataSet(krEvaluatedPriceEntries, "KR/JP Evaluated Price").apply {
                    setDrawCircles(true)
                    colors = krColors
                    circleColors = krColors
                }
                val krTradingProfitPositiveDataSet = LineDataSet(krTradingProfitEntries, "KR/JP Trading Profit").apply {
                    setDrawFilled(true)
                    setDrawCircles(true)
                    setDrawCircleHole(true)
                    colors = krColors
                    circleColors = krColors
                    setCircleColorHole(requireContext().config.primaryColor)
                    fillColor = requireContext().config.primaryColor
                }

                val usPrincipalDataSet = LineDataSet(usPrincipalEntries, "US Principal").apply {
                    color = colorPrincipal
                    setCircleColor(colorPrincipal)
//                    setCircleColorHole(colorPrincipal)
                }
                val usEvaluatedPriceDataSet = LineDataSet(usEvaluatedPriceEntries, "US Evaluated Price").apply {
                    setDrawCircles(true)
                    colors = usColors
                    circleColors = usColors
                }
                val usTradingProfitDataSet = LineDataSet(usTradingProfitEntries, "US Trading Profit").apply {
                    setDrawFilled(true)
                    setDrawCircles(true)
                    setDrawCircleHole(true)
                    colors = usColors
                    circleColors = usColors
                    setCircleColorHole(requireContext().config.primaryColor)
                    fillColor = requireContext().config.primaryColor
                }

                val totalPrincipalDataSet = LineDataSet(totalPrincipalEntries, "Total Principal").apply {
                    color = colorPrincipal
                    setCircleColor(colorPrincipal)
                }
                val totalEvaluatedPriceDataSet = LineDataSet(totalEvaluatedPriceEntries, "Total Evaluated Price").apply {
                    setDrawCircles(true)
                    colors = totalColors
                    circleColors = totalColors
                }
                val totalTradingProfitDataSet = LineDataSet(totalTradingProfitEntries, "Total Trading Profit").apply {
                    setDrawFilled(true)
                    setDrawCircles(true)
                    setDrawCircleHole(true)
                    colors = totalColors
                    circleColors = totalColors
                    setCircleColorHole(requireContext().config.primaryColor)
                    fillColor = requireContext().config.primaryColor
                }

                val kospiDataSet = LineDataSet(kospiEntries, "KOSPI").apply {
                    color = dark
                    setCircleColor(dark)
                }

                when (mChartMode) {
                    "A" -> {
                        mDataSets.add(krPrincipalDataSet)
                        mDataSets.add(krEvaluatedPriceDataSet)
                        mDataSets.add(krTradingProfitPositiveDataSet)
                        mKospiDataSets.add(kospiDataSet)
                        mLineChart.axisLeft.axisMinimum = krTradingProfitPositiveDataSet.yMin.minus(100000)
                        mLineChart.axisRight.axisMinimum = krTradingProfitPositiveDataSet.yMin.minus(100000)
                        mLineChart.axisLeft.axisMaximum = krPrincipalDataSet.yMax.plus(2000000)
                        mLineChart.axisRight.axisMaximum = krPrincipalDataSet.yMax.plus(2000000)
                    }
                    "B" -> {
                        mDataSets.add(usPrincipalDataSet)
                        mDataSets.add(usEvaluatedPriceDataSet)
                        mDataSets.add(usTradingProfitDataSet)
                        mLineChart.axisLeft.axisMinimum = usTradingProfitDataSet.yMin.minus(100000)
                        mLineChart.axisRight.axisMinimum = usTradingProfitDataSet.yMin.minus(100000)
                        mLineChart.axisLeft.axisMaximum = usPrincipalDataSet.yMax.plus(2000000)
                        mLineChart.axisRight.axisMaximum = usPrincipalDataSet.yMax.plus(2000000)
                    }
                    "C" -> {
                        mDataSets.add(totalPrincipalDataSet)
                        mDataSets.add(totalEvaluatedPriceDataSet)
                        mDataSets.add(totalTradingProfitDataSet)
                        mLineChart.axisLeft.axisMinimum = totalTradingProfitDataSet.yMin.minus(100000)
                        mLineChart.axisRight.axisMinimum = totalTradingProfitDataSet.yMin.minus(100000)
                        mLineChart.axisLeft.axisMaximum = totalPrincipalDataSet.yMax.plus(2000000)
                        mLineChart.axisRight.axisMaximum = totalPrincipalDataSet.yMax.plus(2000000)
                    }
                }
            }
            realmInstance.close()
        }
    }

    private fun xAxisTimeMillisToDate(timeMillis: Long, dateFormat: Int = SimpleDateFormat.LONG): String =
        if (timeMillis > 0) DateUtils.getDateStringFromTimeMillis(timeMillis, dateFormat) else "N/A"

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

    inner class StockXAxisValueFormatter(private var context: Context?, private val dateFormat: Int) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            val timeMillis: Long = mTimeMillisMap[value.toInt()] ?: 0
            return xAxisTimeMillisToDate(timeMillis, dateFormat)
        }

    }

    inner class StockYAxisValueFormatter(private var context: Context?) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return getCurrencyFormat().format(value)
        }
    }

    inner class StockMarkerView(context: Context, private val xAxisValueFormatter: IAxisValueFormatter) : MarkerView(context, R.layout.partial_marker_view_stock) {
        private val textLabelX: TextView = findViewById(R.id.textLabelX)
        private val textLabelY: TextView = findViewById(R.id.textLabelY)

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            e?.let { entry ->
                textLabelX.run {
                    text = xAxisValueFormatter.getFormattedValue(entry.x, mLineChart.xAxis)
                    typeface = FontUtils.getCommonTypeface(context)
                    textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
                }
                textLabelY.run {
                    text = getCurrencyFormat().format(entry.y)
                    typeface = FontUtils.getCommonTypeface(context)
                    textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
                }
                super.refreshContent(entry, highlight)
            }
        }

        override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
            return if (mLineChart.width.div(2) > posX) MPPointF(10F, 10F) else MPPointF(width.plus(10F).unaryMinus(), 10F)
        }

    }

    inner class KospiMarkerView(context: Context, private val xAxisValueFormatter: IAxisValueFormatter) : MarkerView(context, R.layout.partial_marker_view_stock) {
        private val textLabelX: TextView = findViewById(R.id.textLabelX)
        private val textLabelY: TextView = findViewById(R.id.textLabelY)

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            e?.let { entry ->
                textLabelX.run {
                    text = xAxisValueFormatter.getFormattedValue(entry.x, mLineChart.xAxis)
                    typeface = FontUtils.getCommonTypeface(context)
                    textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
                }
                textLabelY.run {
                    text = entry.y.toString()
                    typeface = FontUtils.getCommonTypeface(context)
                    textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
                }
                super.refreshContent(entry, highlight)
            }
        }

        override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
            val pointX = if (mKospiChart.width.div(2) > posX) 10F else width.plus(10F).unaryMinus()
            val pointY = if (mKospiChart.height.div(2) > posY) 10F else height.plus(10F).unaryMinus()
            return MPPointF(pointX, pointY)
        }
    }
}