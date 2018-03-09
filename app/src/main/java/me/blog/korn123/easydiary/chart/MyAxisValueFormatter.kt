package me.blog.korn123.easydiary.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

import java.text.DecimalFormat

import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BarChartActivity

class MyAxisValueFormatter(internal var barChartActivity: BarChartActivity) : IAxisValueFormatter {
    private val mFormat: DecimalFormat = DecimalFormat("###,###,###,##0")

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        return mFormat.format(value.toDouble()) + barChartActivity.getString(R.string.diary_count)
    }
}
