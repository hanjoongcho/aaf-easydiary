package me.blog.korn123.easydiary.chart

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler

import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BarChartActivity

/**
 * Created by CHO HANJOONG on 2017-03-23.
 */

class IValueFormatterExt(private var barChartActivity: BarChartActivity) : IValueFormatter {

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
        return value.toInt().toString() + barChartActivity.getString(R.string.diary_count)
    }
}
