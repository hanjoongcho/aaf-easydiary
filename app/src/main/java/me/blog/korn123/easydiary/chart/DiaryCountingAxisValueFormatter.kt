package me.blog.korn123.easydiary.chart

import android.content.Context
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.text.DecimalFormat

class DiaryCountingAxisValueFormatter(
    private var context: Context?,
) : IAxisValueFormatter {
    private val mFormat: DecimalFormat = DecimalFormat("###,###,###,##0")

    override fun getFormattedValue(
        value: Float,
        axis: AxisBase,
    ): String = mFormat.format(value.toDouble())
}
