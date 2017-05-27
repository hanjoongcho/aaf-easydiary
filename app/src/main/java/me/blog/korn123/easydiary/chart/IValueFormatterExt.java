package me.blog.korn123.easydiary.chart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import me.blog.korn123.easydiary.R;

/**
 * Created by CHO HANJOONG on 2017-03-23.
 */

public class IValueFormatterExt implements IValueFormatter {

    BarChartActivity barChartActivity;

    IValueFormatterExt(BarChartActivity barChartActivity) {
        this.barChartActivity = barChartActivity;
    }

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
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        String result = (int)value + barChartActivity.getString(R.string.diary_count);
        return result;
    }
}
