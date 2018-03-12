package me.blog.korn123.easydiary.chart

import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

import me.blog.korn123.easydiary.activities.BarChartActivity

/**
 * Created by philipp on 02/06/16.
 * x축 데이터 관리
 */
class DayAxisValueFormatter(private var barChartActivity: BarChartActivity, private val chart: BarLineChartBase<*>) : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return barChartActivity.itemNumberToRange(value.toInt())
    }
}
