package me.blog.korn123.easydiary.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.zhpan.bannerview.utils.BannerUtils
import com.zhpan.indicator.base.BaseIndicatorView

/**
 * This class from 'com.example.zhpan.banner.view.FigureIndicatorView'
 */
class FigureIndicatorView : BaseIndicatorView {

    private var radius = BannerUtils.dp2px(20f)
    private var backgroundColor = Color.parseColor("#88FF5252")
    private var textColor = Color.WHITE
    private var textSize = BannerUtils.dp2px(13f)
    private var mPaint: Paint? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mPaint = Paint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(2 * radius, 2 * radius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (getPageSize() > 1) {
            mPaint!!.color = backgroundColor
            canvas.drawCircle(width / 2f, height / 2f, radius.toFloat(), mPaint!!)
            mPaint!!.color = textColor
            mPaint!!.textSize = textSize.toFloat()
            val text = "${getCurrentPosition().plus(1)}/${getPageSize()}"
            val textWidth = mPaint!!.measureText(text).toInt()
            val fontMetricsInt = mPaint!!.fontMetricsInt
            val baseline = ((measuredHeight - fontMetricsInt.bottom + fontMetricsInt.top) / 2
                    - fontMetricsInt.top)
            canvas.drawText(text, (width - textWidth) / 2f, baseline.toFloat(), mPaint!!)
        }
    }

    fun setRadius(radius: Int) {
        this.radius = radius
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        this.backgroundColor = backgroundColor
    }

    fun setTextSize(textSize: Int) {
        this.textSize = textSize
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
    }
}