package me.blog.korn123.easydiary.chart

import android.content.Context
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import me.blog.korn123.easydiary.R

class DayAxisValueFormatter(private var context: Context?, private val chart: BarLineChartBase<*>) : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase?): String = when (value.toInt()) {
        1 -> context!!.getString(R.string.range_a)
        2 -> context!!.getString(R.string.range_b)
        3 -> context!!.getString(R.string.range_c)
        4 -> context!!.getString(R.string.range_d)
        5 -> context!!.getString(R.string.range_e)
        6 -> context!!.getString(R.string.range_f)
        else -> context!!.getString(R.string.range_g)
    }
}
