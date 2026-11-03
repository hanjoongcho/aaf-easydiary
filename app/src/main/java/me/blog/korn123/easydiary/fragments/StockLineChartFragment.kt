package me.blog.korn123.easydiary.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.StatisticsActivity
import me.blog.korn123.easydiary.databinding.DialogStockChartOptionBinding
import me.blog.korn123.easydiary.databinding.FragmentStockLineChartBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateAlertDialog
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.ChartConstants
import me.blog.korn123.easydiary.helper.CHART_LABEL_FONT_SIZE_DEFAULT_DP
import me.blog.korn123.easydiary.helper.DAILY_STOCK
import me.blog.korn123.easydiary.helper.StatisticsConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.diary.DiaryViewModel
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class StockLineChartFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var sDatePickerDialog: DatePickerDialog
    private lateinit var eDatePickerDialog: DatePickerDialog
    private val endCalendar = Calendar.getInstance(Locale.getDefault())
    private var endMillis = 0L
    private var startDateListener: DatePickerDialog.OnDateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            requireContext().config.devStockChartOptionFromMillis = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year)
            drawChart()
        }

    private var endDateListener: DatePickerDialog.OnDateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            endMillis = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year)
            drawChart()
        }
    private lateinit var binding: FragmentStockLineChartBinding
    private lateinit var combineChart: CombinedChart
    private lateinit var kospiChart: LineChart

    // KR & JP
    private lateinit var krPrincipalDataSet: BarDataSet
    private lateinit var krEvaluatedPriceDataSet: LineDataSet
    private lateinit var krTradingProfitDataSet: LineDataSet
    private lateinit var krTradingProfitNegativeDataSet: LineDataSet
    private lateinit var krTradingProfitPositiveDataSet: LineDataSet

    // US
    private lateinit var usPrincipalDataSet: BarDataSet
    private lateinit var usEvaluatedPriceDataSet: LineDataSet
    private lateinit var usTradingProfitDataSet: LineDataSet
    private lateinit var usTradingProfitNegativeDataSet: LineDataSet
    private lateinit var usTradingProfitPositiveDataSet: LineDataSet

    // KR & JP & US
    private lateinit var totalPrincipalDataSet: BarDataSet
    private lateinit var totalEvaluatedPriceDataSet: LineDataSet
    private lateinit var totalTradingProfitDataSet: LineDataSet
    private lateinit var totalTradingProfitNegativeDataSet: LineDataSet
    private lateinit var totalTradingProfitPositiveDataSet: LineDataSet

    private val timeMillisMap = hashMapOf<Int, Long>()
    private var coroutineJob: Job? = null
    private val stockLineDataSets = ArrayList<ILineDataSet>()
    private val stockBarDataSets = ArrayList<IBarDataSet>()
    private val kospiDataSets = ArrayList<ILineDataSet>()
    private var totalDataSetCnt = 0
    private val colorPlus = Color.rgb(204, 31, 8)
    private val colorMinus = Color.rgb(6, 57, 112)
    private val diaryViewModel: DiaryViewModel by viewModels()

    /***************************************************************************************************
     *   chart options
     *
     ***************************************************************************************************/
    private var chartMode = R.id.radio_button_option_a
    private var checkedSyncMarker = true
    private var checkedDrawCircle = true
    private var checkedDrawMarker = true

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentStockLineChartBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.layoutParams.run {
            height = ViewGroup.LayoutParams.MATCH_PARENT
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        // Default setting Combine chart
        combineChart =
            binding.lineChart.apply {
                setMaxVisibleValueCount(60)
                setPinchZoom(false)
                description.isEnabled = false
                extraBottomOffset = 10f

                initializeXAxis(xAxis, StockXAxisValueFormatter(context, SimpleDateFormat.SHORT))
                initializeCombineChartYAxis(axisLeft, true, StockYAxisValueFormatter(context))
                initializeCombineChartYAxis(axisRight, false)
                initializeCombineChartLegend(legend, false)
                marker =
                    StockMarkerView(requireContext(), StockXAxisValueFormatter(context, SimpleDateFormat.FULL)).also { it.chartView = this }

                setOnChartValueSelectedListener(
                    object : OnChartValueSelectedListener {
                        override fun onValueSelected(
                            e: Entry?,
                            h: Highlight?,
                        ) {
                            Log.i(AAF_TEST, h.toString())
                            h?.run {
                                combineChart.setDrawMarkers(checkedDrawMarker)

                                // Sync Marker
                                if (kospiChart.visibility == View.VISIBLE &&
                                    checkedSyncMarker
                                ) {
                                    kospiChart.highlightValue(Highlight(x, y, 0))
                                }
                            }
                        }

                        override fun onNothingSelected() {}
                    },
                )
            }

        // Default setting KOSPI chart
        kospiChart =
            binding.chartKospi.apply {
                description.isEnabled = false
                legend.isEnabled = false
                extraBottomOffset = 10f

                initializeXAxis(xAxis, StockXAxisValueFormatter(context, SimpleDateFormat.SHORT))
                initializeCombineChartYAxis(axisLeft, true, null, 5)
                initializeCombineChartYAxis(axisRight, false)
                marker =
                    KospiMarkerView(requireContext(), StockXAxisValueFormatter(context, SimpleDateFormat.FULL)).also { it.chartView = this }
                setOnChartValueSelectedListener(
                    object : OnChartValueSelectedListener {
                        override fun onValueSelected(
                            e: Entry?,
                            h: Highlight?,
                        ) {
                            h?.run {
                                if (checkedSyncMarker) combineChart.highlightValue(Highlight(x, 0, 0).apply { dataIndex = 0 })
                            }
                        }

                        override fun onNothingSelected() {}
                    },
                )
            }

        setupTitle()
        setupChartOptions()
        drawChart()

        val startCalendar =
            Calendar.getInstance(Locale.getDefault()).apply {
                timeInMillis =
                    requireContext().config.devStockChartOptionFromMillis
            }

        sDatePickerDialog =
            DatePickerDialog(
                requireContext(),
                startDateListener,
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH),
            )
        eDatePickerDialog =
            DatePickerDialog(
                requireContext(),
                endDateListener,
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH),
            )
        binding.run {
            cardFromDate.setOnClickListener { sDatePickerDialog.show() }
            cardToDate.setOnClickListener { eDatePickerDialog.show() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob?.run { if (isActive) cancel() }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initDataSet() {
        krEvaluatedPriceDataSet =
            LineDataSet(null, "KR/JP Evaluated Price").apply {
                setDefaultLineChartColor(this)
                isVisible = requireContext().config.devStockEnableEvaluatePrice
                isHighlightEnabled = requireContext().config.devStockEnableEvaluatePrice
            }
        usEvaluatedPriceDataSet =
            LineDataSet(null, "US Evaluated Price").apply {
                setDefaultLineChartColor(this)
            }
        totalEvaluatedPriceDataSet =
            LineDataSet(null, "Total Evaluated Price").apply {
                setDefaultLineChartColor(this)
            }
        krPrincipalDataSet =
            BarDataSet(listOf(), "KR/JP Principal").apply {
                setColor(requireContext().config.textColor, 100)
                isHighlightEnabled = requireContext().config.devStockEnablePrincipalHighlight
            }
        krTradingProfitDataSet =
            LineDataSet(null, "KR/JP Trading Profit").apply {
                setGhostLineChartColor(this)
            }
        krTradingProfitNegativeDataSet =
            LineDataSet(null, "").apply {
                setDefaultFillChartColor(this, colorMinus)
                isHighlightEnabled = false
            }
        krTradingProfitPositiveDataSet =
            LineDataSet(null, "").apply {
                setDefaultFillChartColor(this, colorPlus)
                isHighlightEnabled = false
            }
        usPrincipalDataSet =
            BarDataSet(listOf(), "US Principal").apply {
                setColor(requireContext().config.textColor, 100)
                isHighlightEnabled = requireContext().config.devStockEnablePrincipalHighlight
            }
        usTradingProfitDataSet =
            LineDataSet(null, "US Trading Profit").apply {
                setGhostLineChartColor(this)
            }
        usTradingProfitNegativeDataSet =
            LineDataSet(null, "").apply {
                setDefaultFillChartColor(this, colorMinus)
                isHighlightEnabled = false
            }
        usTradingProfitPositiveDataSet =
            LineDataSet(null, "").apply {
                setDefaultFillChartColor(this, colorPlus)
                isHighlightEnabled = false
            }
        totalPrincipalDataSet =
            BarDataSet(listOf(), "Total Principal").apply {
                setColor(requireContext().config.textColor, 100)
                isHighlightEnabled = requireContext().config.devStockEnablePrincipalHighlight
            }
        totalTradingProfitDataSet =
            LineDataSet(null, "Total Trading Profit").apply {
                setGhostLineChartColor(this)
            }
        totalTradingProfitNegativeDataSet =
            LineDataSet(null, "").apply {
                setDefaultFillChartColor(this, colorMinus)
                isHighlightEnabled = false
            }
        totalTradingProfitPositiveDataSet =
            LineDataSet(null, "").apply {
                setDefaultFillChartColor(this, colorPlus)
                isHighlightEnabled = false
            }
    }

    private fun setupTitle() {
        arguments?.let { bundle ->
            val title = bundle.getString(ChartConstants.CHART_TITLE)
            if (title != null) {
                binding.run {
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
                                    requireActivity(),
                                    Intent(
                                        requireActivity(),
                                        StatisticsActivity::class.java,
                                    ).putExtra(
                                        StatisticsConstants.CHART_MODE,
                                        StatisticsConstants.MODE_SINGLE_LINE_CHART_STOCK,
                                    ),
                                )
                            }, 300)
                        }
                    }
                }
            }
        }
    }

    private fun setupChartOptions() {
        binding.run {
            cardMultiChartMode.setOnClickListener {
                requireActivity().run {
                    val builder = AlertDialog.Builder(this)
                    builder.setPositiveButton(getString(android.R.string.ok), null)
                    builder.setCancelable(false)
                    val dialogStockChartOptionBinding = DialogStockChartOptionBinding.inflate(layoutInflater)

                    dialogStockChartOptionBinding.root.run {
                        setBackgroundColor(config.backgroundColor)
                        initTextSize(this)
                        updateTextColors(this)
                        updateAppViews(this)
                        FontUtils.setFontsTypeface(requireContext(), null, this)
                    }

                    dialogStockChartOptionBinding.run {
                        radioGroupChartOption.check(chartMode)
                        checkSyncMarker.isChecked = checkedSyncMarker
                        checkDrawCircle.isChecked = checkedDrawCircle
                        checkMarker.isChecked = checkedDrawMarker
                        checkEvaluatePrice.isChecked = requireContext().config.devStockEnableEvaluatePrice
                        checkPrincipalHighlight.isChecked = requireContext().config.devStockEnablePrincipalHighlight

                        radioGroupChartOption.setOnCheckedChangeListener { _, checkedId ->
                            chartMode = checkedId
                            drawChart()
                        }
                        checkSyncMarker.setOnCheckedChangeListener { _, isChecked ->
                            checkedSyncMarker = isChecked
                        }
                        checkDrawCircle.setOnCheckedChangeListener { _, isChecked ->
                            checkedDrawCircle = isChecked
                            combineChart.run {
                                if (combinedData.lineData.dataSets.size == 4) {
                                    combinedData.lineData.dataSets[3].also {
                                        if (it is LineDataSet) it.setDrawCircles(isChecked)
                                    }
                                    invalidate()
                                }
                            }
                            kospiChart.run {
                                if (visibility == View.VISIBLE) {
                                    lineData.dataSets[0]?.also {
                                        if (it is LineDataSet) it.setDrawCircles(isChecked)
                                    }
                                    invalidate()
                                }
                            }
                        }
                        checkMarker.setOnCheckedChangeListener { _, isChecked ->
                            checkedDrawMarker = isChecked
                            combineChart.run {
                                setDrawMarkers(isChecked)
                                invalidate()
                            }
                            kospiChart.run {
                                setDrawMarkers(isChecked)
                                invalidate()
                            }
                        }
                        checkEvaluatePrice.setOnCheckedChangeListener { _, isChecked ->
                            requireContext().config.devStockEnableEvaluatePrice = isChecked
                            when (chartMode) {
                                R.id.radio_button_option_a, R.id.radio_button_option_a_1 -> {
                                    krEvaluatedPriceDataSet.run {
                                        isVisible = isChecked
                                        isHighlightEnabled = isChecked
                                    }
                                }

                                R.id.radio_button_option_b -> {
                                    usEvaluatedPriceDataSet.run {
                                        isVisible = isChecked
                                        isHighlightEnabled = isChecked
                                    }
                                }

                                R.id.radio_button_option_c -> {
                                    totalEvaluatedPriceDataSet.run {
                                        isVisible = isChecked
                                        isHighlightEnabled = isChecked
                                    }
                                }
                            }
                            combineChart.invalidate()
                        }
                        checkPrincipalHighlight.setOnCheckedChangeListener { _, isChecked ->
                            requireContext().config.devStockEnableEvaluatePrice = isChecked
                            krPrincipalDataSet.isHighlightEnabled = isChecked
                            usPrincipalDataSet.isHighlightEnabled = isChecked
                            totalPrincipalDataSet.isHighlightEnabled = isChecked
                        }
                    }

                    builder.create().apply {
                        updateAlertDialog(
                            this,
                            null,
                            dialogStockChartOptionBinding.root,
                            "Chart Options",
                        )
                    }
                }
            }
        }
    }

    private fun drawChart() {
        when (chartMode) {
            R.id.radio_button_option_a -> {
                combineChart.visibility = View.VISIBLE
                kospiChart.visibility = View.VISIBLE
            }

            R.id.radio_button_option_a_1 -> {
                combineChart.visibility = View.VISIBLE
                kospiChart.visibility = View.GONE
            }

            R.id.radio_button_option_a_2 -> {
                combineChart.visibility = View.GONE
                kospiChart.visibility = View.VISIBLE
            }

            R.id.radio_button_option_b -> {
                combineChart.visibility = View.VISIBLE
                kospiChart.visibility = View.GONE
            }

            R.id.radio_button_option_c -> {
                combineChart.visibility = View.VISIBLE
                kospiChart.visibility = View.GONE
            }
        }

        coroutineJob?.run { if (isActive) cancel() }
        coroutineJob =
            lifecycleScope.launch(Dispatchers.IO) {
                initDataSet()
                clearChart()
                setData()
                if (totalDataSetCnt > 0) {
                    withContext(Dispatchers.Main) {
                        combineChart.run {
                            data =
                                CombinedData().apply {
                                    setData(LineData(stockLineDataSets).apply { setDrawValues(false) })
                                    setData(BarData(stockBarDataSets).apply { setDrawValues(false) })
                                }
                            animateY(600)
                        }
                        kospiChart.run {
                            data =
                                LineData(kospiDataSets).apply {
                                    setValueTextSize(10f)
                                    setValueTypeface(FontUtils.getCommonTypeface(requireContext()))
                                    setDrawValues(false)
                                }
                            animateY(600)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    binding.barChartProgressBar.visibility = View.GONE
                }
            }
    }

    private fun clearChart() {
        combineChart.clear()
        kospiChart.clear()
        combineChart.highlightValue(null)
        stockLineDataSets.clear()
        stockBarDataSets.clear()
        kospiDataSets.clear()
    }

    private suspend fun setData() {
        val krPrincipalEntries = arrayListOf<BarEntry>()
        val krEvaluatedPriceEntries = arrayListOf<Entry>()
        val krTradingProfitEntries = arrayListOf<Entry>()
        val krTradingProfitPositiveEntries = arrayListOf<Entry>()
        val krTradingProfitNegativeEntries = arrayListOf<Entry>()

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

        val listDiary =
            diaryViewModel.findDiary(
                null,
                false,
                requireContext().config.devStockChartOptionFromMillis,
                endMillis,
                DAILY_STOCK,
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
                        krEvaluatedPriceEntries.add(StockLineEntry(index.toFloat(), krEvaluatedPrice, StockDataType.KR_EVALUATE_PRICE))
                        krTradingProfitEntries.add(Entry(index.toFloat(), diff))

                        usPrincipalEntries.add(BarEntry(index.toFloat(), usPrincipal))
                        usEvaluatedPriceEntries.add(StockLineEntry(index.toFloat(), usEvaluatedPrice, StockDataType.US_EVALUATE_PRICE))
                        diff = usEvaluatedPrice.minus(usPrincipal)
                        if (diff >= 0) usColors.add(colorPlus) else usColors.add(colorMinus)
                        usTradingProfitEntries.add(Entry(index.toFloat(), diff))

                        totalPrincipalEntries.add(
                            BarEntry(
                                index.toFloat(),
                                krPrincipal.plus(usPrincipal),
                            ),
                        )
                        totalEvaluatedPriceEntries.add(Entry(index.toFloat(), sum))
                        diff =
                            krEvaluatedPrice
                                .plus(usEvaluatedPrice)
                                .minus(krPrincipal.plus(usPrincipal))
                        if (diff >= 0) {
                            totalColors.add(colorPlus)
                        } else {
                            totalColors.add(
                                colorMinus,
                            )
                        }
                        totalTradingProfitEntries.add(Entry(index.toFloat(), diff))
                        kospiEntries.add(
                            Entry(
                                index.toFloat(),
                                amountArray[4].toFloat(),
                            ),
                        )

                        timeMillisMap[index] = diaryDto.currentTimeMillis
                        index++
                    } catch (e: Exception) {
                        Log.i(AAF_TEST, e.message ?: "")
                    }
                }
            }
        }
        if (index > 0) {
            totalDataSetCnt = totalEvaluatedPriceEntries.size

            fun splitEntry(
                originEntry: ArrayList<Entry>,
                positive: ArrayList<Entry>,
                negative: ArrayList<Entry>,
            ) {
                var start = 0F
                var splitIndex = 0f
                val splitCnt = 10
                originEntry.forEachIndexed { idx, entry ->
                    if (idx > 0 && idx < originEntry.size) {
                        val offset = entry.y.minus(start).div(splitCnt)
                        for (i in 0..splitCnt.minus(if (originEntry.size.minus(1) == idx) 0 else 1)) {
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
            krPrincipalDataSet.values = krPrincipalEntries
            krEvaluatedPriceDataSet.values = krEvaluatedPriceEntries
            krTradingProfitDataSet.values = krTradingProfitEntries
            splitEntry(krTradingProfitEntries, krTradingProfitPositiveEntries, krTradingProfitNegativeEntries)
            krTradingProfitNegativeDataSet.values = krTradingProfitNegativeEntries
            krTradingProfitPositiveDataSet.values = krTradingProfitPositiveEntries
            // END KR Data

            // START US Data
            usPrincipalDataSet.values = usPrincipalEntries
            usEvaluatedPriceDataSet.values = usEvaluatedPriceEntries
            usTradingProfitDataSet.values = usTradingProfitEntries
            splitEntry(usTradingProfitEntries, usTradingProfitPositiveEntries, usTradingProfitNegativeEntries)
            usTradingProfitNegativeDataSet.values = usTradingProfitNegativeEntries
            usTradingProfitPositiveDataSet.values = usTradingProfitPositiveEntries
            // END US Data

            // START Total Data
            totalEvaluatedPriceDataSet.values = totalEvaluatedPriceEntries
            totalPrincipalDataSet.values = totalPrincipalEntries
            totalTradingProfitDataSet.values = totalTradingProfitEntries
            splitEntry(totalTradingProfitEntries, totalTradingProfitPositiveEntries, totalTradingProfitNegativeEntries)
            totalTradingProfitNegativeDataSet.values = totalTradingProfitNegativeEntries
            totalTradingProfitPositiveDataSet.values = totalTradingProfitPositiveEntries
            // END Total Data

            val kospiDataSet =
                LineDataSet(kospiEntries, "KOSPI").apply {
                    setDefaultLineChartColor(this)
                }

            when (chartMode) {
                R.id.radio_button_option_a, R.id.radio_button_option_a_1, R.id.radio_button_option_a_2 -> {
                    stockBarDataSets.add(krPrincipalDataSet)
                    stockLineDataSets.add(krTradingProfitDataSet)
                    stockLineDataSets.add(krTradingProfitPositiveDataSet)
                    stockLineDataSets.add(krTradingProfitNegativeDataSet)
                    stockLineDataSets.add(krEvaluatedPriceDataSet)
                    kospiDataSets.add(kospiDataSet)
                    combineChart.run {
                        axisLeft.run {
                            axisMinimum = krTradingProfitDataSet.yMin.minus(100000)
                            axisMaximum = krPrincipalDataSet.yMax.plus(krTradingProfitDataSet.yMax).plus(300000)
                        }
                        axisRight.run {
                            axisMinimum = krTradingProfitDataSet.yMin.minus(100000)
                            axisMaximum = krPrincipalDataSet.yMax.plus(krTradingProfitDataSet.yMax).plus(300000)
                        }
                    }
                }

                R.id.radio_button_option_b -> {
                    stockBarDataSets.add(usPrincipalDataSet)
                    stockLineDataSets.add(usTradingProfitDataSet)
                    stockLineDataSets.add(usTradingProfitNegativeDataSet)
                    stockLineDataSets.add(usTradingProfitPositiveDataSet)
                    stockLineDataSets.add(usEvaluatedPriceDataSet)
                    combineChart.run {
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

                R.id.radio_button_option_c -> {
                    stockBarDataSets.add(totalPrincipalDataSet)
                    stockLineDataSets.add(totalTradingProfitDataSet)
                    stockLineDataSets.add(totalTradingProfitPositiveDataSet)
                    stockLineDataSets.add(totalTradingProfitNegativeDataSet)
                    stockLineDataSets.add(totalEvaluatedPriceDataSet)
                    combineChart.run {
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

            kospiChart.run {
                axisLeft.run {
                    axisMinimum = kospiDataSet.yMin.minus(500)
                    axisMaximum = kospiDataSet.yMax.plus(500)
                }
            }
        }
    }

    private fun xAxisTimeMillisToDate(
        timeMillis: Long,
        dateFormat: Int = SimpleDateFormat.LONG,
    ): String = if (timeMillis > 0) DateUtils.getDateStringFromTimeMillis(timeMillis, dateFormat) else "N/A"

    private fun getCurrencyFormat() = NumberFormat.getCurrencyInstance(Locale.KOREA)

    private fun initializeCombineChartYAxis(
        yAxis: YAxis,
        isEnable: Boolean = false,
        stockYAxisValueFormatter: IAxisValueFormatter? = null,
        labelCount: Int = 8,
    ) {
        yAxis.run {
            isEnabled = isEnable
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            setLabelCount(labelCount, true)
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 0f
            axisMinimum = 0f
            setDrawGridLines(true)
            minWidth = 65F
            maxWidth = 65F
            stockYAxisValueFormatter?.let { valueFormatter = it }
        }
    }

    private fun initializeXAxis(
        xAxis: XAxis,
        stockXAxisValueFormatter: IAxisValueFormatter? = null,
    ) {
        xAxis.run {
            isEnabled = !requireActivity().isLandScape()
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            labelRotationAngle = -65F
            granularity = 1f
            setLabelCount(6, true)
            stockXAxisValueFormatter?.let { valueFormatter = it }
        }
    }

    private fun initializeCombineChartLegend(
        legend: Legend,
        isEnable: Boolean = false,
    ) {
        legend.run {
            isEnabled = isEnable
            typeface = FontUtils.getCommonTypeface(requireContext())
            textSize = CHART_LABEL_FONT_SIZE_DEFAULT_DP
            textColor = requireContext().config.textColor
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            orientation = Legend.LegendOrientation.HORIZONTAL
            form = Legend.LegendForm.CIRCLE
            isWordWrapEnabled = true
        }
    }

    private fun setDefaultLineChartColor(lineDataSet: LineDataSet) {
        lineDataSet.run {
            color = requireContext().config.primaryColor
            highLightColor = requireContext().config.textColor
            setCircleColor(requireContext().config.primaryColor)
            circleRadius = 1f
            setDrawCircles(checkedDrawCircle)
        }
    }

    private fun setGhostLineChartColor(lineDataSet: LineDataSet) {
        lineDataSet.run {
            setDrawCircles(false)
            enableDashedLine(0f, 1f, 0f)
            highLightColor = requireContext().config.textColor
        }
    }

    private fun setDefaultFillChartColor(
        lineDataSet: LineDataSet,
        color: Int,
    ) {
        lineDataSet.run {
            fillColor = color
            this.color = color
            setDrawFilled(true)
            setDrawCircleHole(false)
            setDrawCircles(false)
            highLightColor = requireContext().config.textColor
        }
    }

    /***************************************************************************************************
     *   inner class
     *
     ***************************************************************************************************/

    inner class StockXAxisValueFormatter(
        private var context: Context?,
        private val dateFormat: Int,
    ) : IAxisValueFormatter {
        override fun getFormattedValue(
            value: Float,
            axis: AxisBase,
        ): String {
            val timeMillis = if (timeMillisMap.size > value) timeMillisMap[value.toInt()] ?: 0 else 0
            return xAxisTimeMillisToDate(timeMillis, dateFormat)
        }
    }

    inner class StockYAxisValueFormatter(
        private var context: Context?,
    ) : IAxisValueFormatter {
        override fun getFormattedValue(
            value: Float,
            axis: AxisBase,
        ): String = getCurrencyFormat().format(value)
    }

    inner class StockMarkerView(
        context: Context,
        private val xAxisValueFormatter: IAxisValueFormatter,
    ) : MarkerView(context, R.layout.partial_marker_view_stock) {
        private val textLabelX: TextView = findViewById(R.id.textLabelX)
        private val textLabelY: TextView = findViewById(R.id.textLabelY)

        override fun refreshContent(
            e: Entry?,
            highlight: Highlight?,
        ) {
            e?.let { entry ->
                textLabelX.run {
                    text = xAxisValueFormatter.getFormattedValue(entry.x, combineChart.xAxis)
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

        override fun getOffsetForDrawingAtPoint(
            posX: Float,
            posY: Float,
        ): MPPointF {
            val pointX = if (kospiChart.width.div(2) > posX) 10F else width.plus(10F).unaryMinus()
            val pointY =
                if (kospiChart.height.div(2) > posY) 20F else height.plus(20F).unaryMinus()
            return MPPointF(pointX, pointY)
        }
    }

    inner class KospiMarkerView(
        context: Context,
        private val xAxisValueFormatter: IAxisValueFormatter,
    ) : MarkerView(context, R.layout.partial_marker_view_stock) {
        private val textLabelX: TextView = findViewById(R.id.textLabelX)
        private val textLabelY: TextView = findViewById(R.id.textLabelY)
        private val markerOffset = 20f

        override fun refreshContent(
            e: Entry?,
            highlight: Highlight?,
        ) {
            e?.let { entry ->
                textLabelX.run {
                    text = xAxisValueFormatter.getFormattedValue(entry.x, combineChart.xAxis)
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

        override fun getOffsetForDrawingAtPoint(
            posX: Float,
            posY: Float,
        ): MPPointF {
            val pointX = if (kospiChart.width.div(2) > posX) markerOffset else width.plus(markerOffset).unaryMinus()
            val pointY =
                if (kospiChart.height.div(2) > posY) markerOffset else height.plus(markerOffset).unaryMinus()
            return MPPointF(pointX, pointY)
        }
    }

    class StockLineEntry : Entry {
        var stockDataType: StockDataType? = null

        constructor(x: Float, y: Float) : super(x, y)

        constructor(x: Float, y: Float, stockDataType: StockDataType) : super(x, y) {
            this.stockDataType = stockDataType
        }
    }

    enum class StockDataType {
        KR_EVALUATE_PRICE,
        US_EVALUATE_PRICE,
        TOTAL_EVALUATE_PRICE,
    }
}
