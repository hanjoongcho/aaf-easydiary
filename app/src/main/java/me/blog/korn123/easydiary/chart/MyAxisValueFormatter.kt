package me.blog.korn123.easydiary.chart

import android.content.Context
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import me.blog.korn123.easydiary.R
import java.text.DecimalFormat

class MyAxisValueFormatter(private var context: Context?) : IAxisValueFormatter {
    private val mFormat: DecimalFormat = DecimalFormat("###,###,###,##0")

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        return mFormat.format(value.toDouble()) + context?.getString(R.string.diary_count)
    }
}
