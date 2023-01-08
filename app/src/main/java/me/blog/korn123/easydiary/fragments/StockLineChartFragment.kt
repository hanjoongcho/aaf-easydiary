package me.blog.korn123.easydiary.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
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
import me.blog.korn123.easydiary.extensions.isLandScape
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
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentStockLineChartBinding
    private lateinit var mCombineChart: CombinedChart
    private lateinit var mKospiChart: LineChart
    private lateinit var mKrEvaluatedPriceDataSet: LineDataSet
    private val mTimeMillisMap = hashMapOf<Int, Long>()
    private var mCoroutineJob: Job? = null
    private val mStockLineDataSets = ArrayList<ILineDataSet>()
    private val mStockBarDataSets = ArrayList<IBarDataSet>()
    private val mKospiDataSets = ArrayList<ILineDataSet>()
    private var mTotalDataSetCnt = 0
    private var mChartMode = "A"
    private val mColorPlus = Color.rgb(204, 31, 8)
    private val mColorMinus = Color.rgb(6, 57, 112)


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentStockLineChartBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIXME: When ViewBinding is used, the MATCH_PARENT option declared in the layout does not work, so it is temporarily declared here.
        mBinding.root.layoutParams.run {
            height = ViewGroup.LayoutParams.MATCH_PARENT
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        mKrEvaluatedPriceDataSet = LineDataSet(null, "KR/JP Evaluated Price").apply {
            setDefaultLineChartColor(this)
            isVisible = mBinding.checkEvaluatePrice.isChecked
        }

        // Default setting Combine chart
        mCombineChart = mBinding.lineChart.apply {
            setMaxVisibleValueCount(60) // if more than 60 entries are displayed in the chart, no values will be drawn
            setPinchZoom(false) // scaling can now only be done on x- and y-axis separately
            description.isEnabled = false
            extraBottomOffset = 10f

            initializeXAxis(xAxis, StockXAxisValueFormatter(context, SimpleDateFormat.SHORT))
            initializeCombineChartYAxis(axisLeft, true, StockYAxisValueFormatter(context))
            initializeCombineChartYAxis(axisRight, false)
            initializeCombineChartLegend(legend, false)
            marker = StockMarkerView(requireContext(), StockXAxisValueFormatter(context, SimpleDateFormat.FULL)).also { it.chartView = this }
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    Log.i(AAF_TEST, h.toString())
                    h?.run {
                        // Toggle marker when data y value zero
                        mCombineChart.setDrawMarkers(e?.y != 0f && mBinding.checkMarker.isChecked)

                        // Sync Marker
                        if (mKospiChart.visibility == View.VISIBLE && mBinding.checkSyncMarker.isChecked) mKospiChart.highlightValue(Highlight(x, y, 0))
                    }
                }
                override fun onNothingSelected() {}
            })
        }

        // Default setting KOSPI chart
        mKospiChart = mBinding.chartKospi.apply {
            description.isEnabled = false
            legend.isEnabled = false
            extraBottomOffset = 10f

            initializeXAxis(xAxis, StockXAxisValueFormatter(context, SimpleDateFormat.SHORT))
            initializeCombineChartYAxis(axisLeft, true, null)
            initializeCombineChartYAxis(axisRight, false)
            marker = KospiMarkerView(requireContext(), StockXAxisValueFormatter(context, SimpleDateFormat.FULL)).also { it.chartView = this }
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    h?.run {
                        if (mBinding.checkSyncMarker.isChecked) mCombineChart.highlightValue(Highlight(x, 1, 0).apply { dataIndex = 0 })
                    }
                }

                override fun onNothingSelected() {}
            })
        }

        setupTitle() // determine title parameter
        setupChartOptions()
        drawChart()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCoroutineJob?.run { if (isActive) cancel() }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun setupTitle() {
        arguments?.let { bundle ->
            val title = bundle.getString(CHART_TITLE)
            if (title != null) {
                mBinding.run {
                    chartTitle.text = title
                    chartTitle.visibility = View.VISIBLE
                    imageStockSymbol.run {
                        visibility = View.VISIBLE
                        FlavorUtils.initWeatherView(requireActivity(), this, DAILY_STOCK)
                    }
                    imageExpendChart.run {
                        visibility = View.VISIBLE
                        requireActivity().updateDrawableColorInnerCardView(this, config.textColor)
                        setOnClickListener { view ->
                            view.postDelayed({
                                TransitionHelper.startActivityWithTransition(
                                    requireActivity(), Intent(
                                        requireActivity(), StatisticsActivity::class.java
                                    ).putExtra(
                                        StatisticsActivity.CHART_MODE,
                                        StatisticsActivity.MODE_SINGLE_LINE_CHART_STOCK
                                    )
                                )
                            }, 300)
                        }
                    }
                }
            }
        }
    }

    private fun setupChartOptions() {
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

            checkSyncMarker.setOnCheckedChangeListener { _, isChecked ->
                mCombineChart.run {
                    //                    xAxis.isEnabled = isChecked
                    //                    invalidate()
                }
                mKospiChart.run {
                    //                    xAxis.isEnabled = isChecked
                    //                    invalidate()
                }
                //                drawChart()
            }

            checkMarker.setOnCheckedChangeListener { _, isChecked ->
                mCombineChart.run {
                    setDrawMarkers(isChecked)
                    invalidate()
                }
                mKospiChart.run {
                    setDrawMarkers(isChecked)
                    invalidate()
                }
            }

            checkDrawCircle.setOnCheckedChangeListener { _, isChecked ->
                mCombineChart.run {
//                    combinedData.lineData.dataSets.forEach {
//                        if (it is LineDataSet) it.setDrawCircles(isChecked)
//                    }
                    combinedData.lineData.dataSets[0]?.also {
                        if (it is LineDataSet) it.setDrawCircles(isChecked)
                    }
                    invalidate()
                }
                mKospiChart.run {
                    lineData.dataSets[0]?.also {
                        if (it is LineDataSet) it.setDrawCircles(isChecked)
                    }
                    invalidate()
                }
            }

            checkEvaluatePrice.setOnCheckedChangeListener { _, isChecked ->
                mKrEvaluatedPriceDataSet.isVisible = isChecked
                mCombineChart.invalidate()
            }
        }
    }

    private fun drawChart() {
        mKospiChart.visibility = if (mChartMode === "A") View.VISIBLE else View.GONE
        mCoroutineJob?.run { if (isActive) cancel() }
        mCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
            clearChart()
            setData()
            if (mTotalDataSetCnt > 0) {
                withContext(Dispatchers.Main) {
                    mCombineChart.run {
                        data = CombinedData().apply {
                            setData(LineData(mStockLineDataSets).apply { setDrawValues(false) })
                            setData(BarData(mStockBarDataSets).apply { setDrawValues(false) })
                        }
                        animateY(600)
                    }
                    mKospiChart.run {
                        data = LineData(mKospiDataSets).apply {
                            setValueTextSize(10f)
                            setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                            setDrawValues(false)
                        }
                        animateY(600)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                mBinding.barChartProgressBar.visibility = View.GONE
            }
        }
    }

    private fun clearChart() {
        mCombineChart.clear()
        mKospiChart.clear()
        mCombineChart.highlightValue(null)
        mStockLineDataSets.clear()
        mStockBarDataSets.clear()
        mKospiDataSets.clear()
    }

    private fun setData() {
        val krPrincipalEntries = arrayListOf<BarEntry>()
        val krEvaluatedPriceEntries = arrayListOf<Entry>()
        val krTradingProfitEntries = arrayListOf<Entry>()
        val krTradingProfitPositiveEntries = arrayListOf<Entry>()
        val krTradingProfitNegativeEntries = arrayListOf<Entry>()
        val krColors = arrayListOf<Int>()

        val usPrincipalEntries = arrayListOf<BarEntry>()
        val usEvaluatedPriceEntries = arrayListOf<Entry>()
        val usTradingProfitEntries = arrayListOf<Entry>()
        val usTradingProfitPositiveEntries = arrayListOf<Entry>()
        val usTradingProfitNegativeEntries = arrayListOf<Entry>()
        val usColors = arrayListOf<Int>()

        val totalPrincipalEntries = arrayListOf<BarEntry>()
        val totalEvaluatedPriceEntries = arrayListOf<Entry>()
        val totalTradingProfitEntries = arrayListOf<Entry>()
        val totalTradingProfitPositiveEntries = arrayListOf<Entry>()
        val totalTradingProfitNegativeEntries = arrayListOf<Entry>()
        val totalColors = arrayListOf<Int>()

        val kospiEntries = arrayListOf<Entry>()

        EasyDiaryDbHelper.getTemporaryInstance().let { realmInstance ->
            val listDiary = EasyDiaryDbHelper.findDiary(
                null, false, 0, 0, DAILY_STOCK, realmInstance = realmInstance
            )
            var index = 0
            var totalSum = 0F
            listDiary.reversed().forEach { diaryDto ->
                diaryDto.title?.let {
                    if (EasyDiaryUtils.isStockNumber(it)) {
                        try {
                            val amountArray = it.split(",")
                            if (amountArray.size != 5) return@forEach
                            val krEvaluatedPrice = amountArray[0].toFloat()
                            val usEvaluatedPrice = amountArray[1].toFloat()
                            val krPrincipal = amountArray[2].toFloat()
                            val usPrincipal = amountArray[3].toFloat()
                            val sum = krEvaluatedPrice.plus(usEvaluatedPrice)
                            totalSum += sum
                            var diff = krEvaluatedPrice.minus(krPrincipal)
                            krPrincipalEntries.add(BarEntry(index.toFloat(), krPrincipal))
                            krEvaluatedPriceEntries.add(Entry(index.toFloat(), krEvaluatedPrice))
                            krTradingProfitEntries.add(Entry(index.toFloat(), diff))
                            if (diff >= 0) krColors.add(mColorPlus) else krColors.add(mColorMinus)

                            usPrincipalEntries.add(BarEntry(index.toFloat(), usPrincipal))
                            usEvaluatedPriceEntries.add(Entry(index.toFloat(), usEvaluatedPrice))
                            diff = usEvaluatedPrice.minus(usPrincipal)
                            if (diff >= 0) usColors.add(mColorPlus) else usColors.add(mColorMinus)
                            usTradingProfitEntries.add(Entry(index.toFloat(), diff))

                            totalPrincipalEntries.add(
                                BarEntry(
                                    index.toFloat(), krPrincipal.plus(usPrincipal)
                                )
                            )
                            totalEvaluatedPriceEntries.add(Entry(index.toFloat(), sum))
                            diff = krEvaluatedPrice.plus(usEvaluatedPrice)
                                .minus(krPrincipal.plus(usPrincipal))
                            if (diff >= 0) totalColors.add(mColorPlus) else totalColors.add(
                                mColorMinus
                            )
                            totalTradingProfitEntries.add(Entry(index.toFloat(), diff))
                            kospiEntries.add(
                                Entry(
                                    index.toFloat(), amountArray[4].toFloat()
                                )
                            )

                            mTimeMillisMap[index] = diaryDto.currentTimeMillis
                            index++
                        } catch (e: Exception) {
                            Log.i(AAF_TEST, e.message ?: "")
                        }
                    }
                }
            }
            if (index > 0) {
                mTotalDataSetCnt = totalEvaluatedPriceEntries.size
                fun splitEntry(originEntry: ArrayList<Entry>, positive: ArrayList<Entry>, negative: ArrayList<Entry>) {
                    var start = 0F
                    var splitIndex = 0f
                    val splitCnt = 10
                    originEntry.forEachIndexed { index, entry ->
                        if (index > 0 && index < originEntry.size) {
                            val offset = entry.y.minus(start).div(splitCnt)
                            for(i in 0..splitCnt.minus(1)) {
                                val y = offset.times(i).plus(start)
                                negative.add(Entry(splitIndex, if (y > 0) 0f else y))
                                positive.add(Entry(splitIndex, if (y < 0) 0f else y))
                                splitIndex += 0.1f
                            }
                        }
                        start = entry.y
                    }
                }

                // START KR Data
                val krPrincipalDataSet = BarDataSet(krPrincipalEntries, "KR/JP Principal").apply {
                    setColor(requireContext().config.textColor, 100)
                }
                mKrEvaluatedPriceDataSet.run {
//                    clear()
                    values = krEvaluatedPriceEntries
                }
                val krTradingProfitDataSet =
                    LineDataSet(krTradingProfitEntries, "KR/JP Trading Profit").apply {
                        setGhostLineChartColor(this)
                    }
                // split entry
                splitEntry(krTradingProfitEntries, krTradingProfitPositiveEntries, krTradingProfitNegativeEntries)
                val krTradingProfitNegativeDataSet =
                    LineDataSet(krTradingProfitNegativeEntries, "").apply {
                        setDefaultFillChartColor(this, mColorMinus)
                    }
                val krTradingProfitPositiveDataSet =
                    LineDataSet(krTradingProfitPositiveEntries, "").apply {
                        setDefaultFillChartColor(this, mColorPlus)
                    }
                // END KR Data

                // START US Data
                val usPrincipalDataSet = BarDataSet(usPrincipalEntries, "US Principal").apply {
                    color = requireContext().config.textColor
                }
                val usEvaluatedPriceDataSet =
                    LineDataSet(usEvaluatedPriceEntries, "US Evaluated Price").apply {
                        setDefaultLineChartColor(this)
                    }
                val usTradingProfitDataSet =
                    LineDataSet(usTradingProfitEntries, "US Trading Profit").apply {
                        setGhostLineChartColor(this)
                    }
                // split entry
                splitEntry(usTradingProfitEntries, usTradingProfitPositiveEntries, usTradingProfitNegativeEntries)
                val usTradingProfitNegativeDataSet =
                    LineDataSet(usTradingProfitNegativeEntries, "").apply {
                        setDefaultFillChartColor(this, mColorMinus)
                    }
                val usTradingProfitPositiveDataSet =
                    LineDataSet(usTradingProfitPositiveEntries, "").apply {
                        setDefaultFillChartColor(this, mColorPlus)
                    }
                // END US Data

                // START Total Data
                val totalPrincipalDataSet =
                    BarDataSet(totalPrincipalEntries, "Total Principal").apply {
                        color = requireContext().config.textColor
                    }
                val totalEvaluatedPriceDataSet =
                    LineDataSet(totalEvaluatedPriceEntries, "Total Evaluated Price").apply {
                        setDefaultLineChartColor(this)
                    }
                val totalTradingProfitDataSet =
                    LineDataSet(totalTradingProfitEntries, "Total Trading Profit").apply {
                        setGhostLineChartColor(this)
                    }
                // split entry
                splitEntry(totalTradingProfitEntries, totalTradingProfitPositiveEntries, totalTradingProfitNegativeEntries)
                val totalTradingProfitNegativeDataSet =
                    LineDataSet(totalTradingProfitNegativeEntries, "").apply {
                        setDefaultFillChartColor(this, mColorMinus)
                    }
                val totalTradingProfitPositiveDataSet =
                    LineDataSet(totalTradingProfitPositiveEntries, "").apply {
                        setDefaultFillChartColor(this, mColorPlus)
                    }
                // END Total Data

                val kospiDataSet = LineDataSet(kospiEntries, "KOSPI").apply {
                    setDefaultLineChartColor(this)
                }

                when (mChartMode) {
                    "A" -> {
                        mStockBarDataSets.add(krPrincipalDataSet)
                        mStockLineDataSets.add(mKrEvaluatedPriceDataSet)
                        mStockLineDataSets.add(krTradingProfitDataSet)
                        mStockLineDataSets.add(krTradingProfitPositiveDataSet)
                        mStockLineDataSets.add(krTradingProfitNegativeDataSet)
                        mKospiDataSets.add(kospiDataSet)
                        mCombineChart.run {
                            axisLeft.run {
                                axisMinimum = krTradingProfitDataSet.yMin.minus(100000)
                                axisMaximum = krPrincipalDataSet.yMax.plus(2000000)
                            }
                            axisRight.run {
                                axisMinimum = krTradingProfitDataSet.yMin.minus(100000)
                                axisMaximum = krPrincipalDataSet.yMax.plus(2000000)
                            }
                        }
                    }
                    "B" -> {
                        mStockBarDataSets.add(usPrincipalDataSet)
                        mStockLineDataSets.add(usEvaluatedPriceDataSet)
                        mStockLineDataSets.add(usTradingProfitDataSet)
                        mStockLineDataSets.add(usTradingProfitNegativeDataSet)
                        mStockLineDataSets.add(usTradingProfitPositiveDataSet)
                        mCombineChart.run {
                            axisLeft.run {
                                axisMinimum = usTradingProfitDataSet.yMin.minus(100000)
                                axisMaximum = usPrincipalDataSet.yMax.plus(2000000)
                            }
                            axisRight.run {
                                axisMinimum = usTradingProfitDataSet.yMin.minus(100000)
                                axisMaximum = usPrincipalDataSet.yMax.plus(2000000)
                            }
                        }
                    }
                    "C" -> {
                        mStockBarDataSets.add(totalPrincipalDataSet)
                        mStockLineDataSets.add(totalEvaluatedPriceDataSet)
                        mStockLineDataSets.add(totalTradingProfitDataSet)
                        mStockLineDataSets.add(totalTradingProfitPositiveDataSet)
                        mStockLineDataSets.add(totalTradingProfitNegativeDataSet)
                        mCombineChart.run {
                            axisLeft.run {
                                axisMinimum = totalTradingProfitDataSet.yMin.minus(100000)
                                axisMaximum = totalPrincipalDataSet.yMax.plus(2000000)
                            }
                            axisRight.run {
                                axisMinimum = totalTradingProfitDataSet.yMin.minus(100000)
                                axisMaximum = totalPrincipalDataSet.yMax.plus(2000000)
                            }
                        }
                    }
                }

                mKospiChart.run {
                    axisLeft.run {
                        axisMinimum = kospiDataSet.yMin.minus(500)
                        axisMaximum = kospiDataSet.yMax.plus(500)
                    }
                }
            }
            realmInstance.close()
        }
    }

    private fun xAxisTimeMillisToDate(
        timeMillis: Long, dateFormat: Int = SimpleDateFormat.LONG
    ): String =
        if (timeMillis > 0) DateUtils.getDateStringFromTimeMillis(timeMillis, dateFormat) else "N/A"

    private fun getCurrencyFormat() = NumberFormat.getCurrencyInstance(Locale.KOREA)

    private fun initializeCombineChartYAxis(yAxis: YAxis, isEnable: Boolean = false, stockYAxisValueFormatter: IAxisValueFormatter? = null) {
        yAxis.run {
            isEnabled = isEnable
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            setLabelCount(8, false)
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 0f
            axisMinimum = 0f // this replaces setStartAtZero(true)
            setDrawGridLines(true)
            minWidth = 60F
            maxWidth = 60F
            stockYAxisValueFormatter?.let { valueFormatter = it }
        }
    }

    private fun initializeXAxis(xAxis: XAxis, stockXAxisValueFormatter: IAxisValueFormatter? = null) {
        xAxis.run {
            isEnabled = !requireActivity().isLandScape()
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            labelRotationAngle = -65F
            granularity = 1f // only intervals of 1 day
            labelCount = 5
            stockXAxisValueFormatter?.let { valueFormatter = it }
        }
    }

    private fun initializeCombineChartLegend(legend: Legend, isEnable: Boolean = false) {
        legend.run {
            isEnabled = isEnable
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
    }

    private fun setDefaultLineChartColor(lineDataSet: LineDataSet) {
        lineDataSet.run {
            color = requireContext().config.primaryColor
            setCircleColor(requireContext().config.primaryColor)
            setCircleColorHole(requireContext().config.textColor)
            setDrawCircles(mBinding.checkDrawCircle.isChecked)
        }
    }

    private fun setGhostLineChartColor(lineDataSet: LineDataSet) {
        lineDataSet.run {
            setDrawCircles(false)
            enableDashedLine(0f, 1f, 0f)
        }
    }

    private fun setDefaultFillChartColor(lineDataSet: LineDataSet, color: Int) {
        lineDataSet.run {
            fillColor = color
            enableDashedLine(0f, 1f, 0f)
            setDrawFilled(true)
            setDrawCircleHole(false)
            setDrawCircles(false)
        }
    }


    /***************************************************************************************************
     *   inner class
     *
     ***************************************************************************************************/
    companion object {
        const val CHART_TITLE = "chartTitle"
    }

    inner class StockXAxisValueFormatter(
        private var context: Context?, private val dateFormat: Int
    ) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            val timeMillis = if (mTimeMillisMap.size > value) mTimeMillisMap[value.toInt()] ?: 0 else 0
            return xAxisTimeMillisToDate(timeMillis, dateFormat)
        }
    }

    inner class StockYAxisValueFormatter(private var context: Context?) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return getCurrencyFormat().format(value)
        }
    }

    inner class StockMarkerView(
        context: Context, private val xAxisValueFormatter: IAxisValueFormatter
    ) : MarkerView(context, R.layout.partial_marker_view_stock) {
        private val textLabelX: TextView = findViewById(R.id.textLabelX)
        private val textLabelY: TextView = findViewById(R.id.textLabelY)

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            e?.let { entry ->
                textLabelX.run {
                    text = xAxisValueFormatter.getFormattedValue(entry.x, mCombineChart.xAxis)
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
            val pointX = if (mKospiChart.width.div(2) > posX) 10F else width.plus(10F).unaryMinus()
            val pointY =
                if (mKospiChart.height.div(2) > posY) 20F else height.plus(20F).unaryMinus()
            return MPPointF(pointX, pointY)
        }
    }

    inner class KospiMarkerView(
        context: Context, private val xAxisValueFormatter: IAxisValueFormatter
    ) : MarkerView(context, R.layout.partial_marker_view_stock) {
        private val textLabelX: TextView = findViewById(R.id.textLabelX)
        private val textLabelY: TextView = findViewById(R.id.textLabelY)
        private val markerOffset = 20f

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            e?.let { entry ->
                textLabelX.run {
                    text = xAxisValueFormatter.getFormattedValue(entry.x, mCombineChart.xAxis)
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
            val pointX = if (mKospiChart.width.div(2) > posX) markerOffset else width.plus(markerOffset).unaryMinus()
            val pointY =
                if (mKospiChart.height.div(2) > posY) markerOffset else height.plus(markerOffset).unaryMinus()
            return MPPointF(pointX, pointY)
        }
    }
}