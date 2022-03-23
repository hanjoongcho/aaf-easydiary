package me.blog.korn123.easydiary.chart

import android.content.Context
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import me.blog.korn123.easydiary.R
import java.text.DecimalFormat

class WeightAxisYValueFormatter(private var context: Context?) : IAxisValueFormatter {
    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        return "${value}kg"
    }
}
