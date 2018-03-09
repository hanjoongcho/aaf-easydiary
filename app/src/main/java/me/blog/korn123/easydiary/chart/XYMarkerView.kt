package me.blog.korn123.easydiary.chart

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import me.blog.korn123.easydiary.R
import java.text.DecimalFormat

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
class XYMarkerView(context: Context, private val xAxisValueFormatter: IAxisValueFormatter) : MarkerView(context, R.layout.custom_marker_view) {

    private val tvContent: TextView = findViewById<TextView>(R.id.tvContent)
    private val format: DecimalFormat = DecimalFormat("###,###,###,##0")

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        tvContent.text = context.getString(R.string.write_time) + ": " + xAxisValueFormatter.getFormattedValue(e!!.x, null) + ", " + context.getString(R.string.write_count) + ": " + e.y.toInt()
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}
