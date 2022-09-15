package me.blog.korn123.easydiary.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.zhpan.bannerview.utils.BannerUtils
import com.zhpan.indicator.base.BaseIndicatorView
import me.blog.korn123.commons.utils.FontUtils

/**
 * This class from 'com.example.zhpan.banner.view.FigureIndicatorView'
 */
class FigureIndicatorView : BaseIndicatorView {

    private var radius = BannerUtils.dp2px(20f)
    private var backgroundColor = Color.parseColor("#88FF5252")
    private var textColor = Color.WHITE
    private var mTextSize = BannerUtils.dp2px(13f)
    private var mPaint: Paint? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mPaint = Paint()
        alpha = 0.6F
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var height = 0
        var width = 0
        mPaint?.run {
            typeface = FontUtils.getCommonTypeface(context)
            textSize = mTextSize.toFloat()
            val fontMetricsInt = this.fontMetricsInt
            height = fontMetricsInt.descent.minus(fontMetricsInt.ascent)
            width = this.measureText("${getPageSize()}/${getPageSize()}").toInt()
        }
        setRadius(width.times(0.7).toInt())
        setMeasuredDimension(2 * radius, height.times(1.4).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (getPageSize() > 1) {
            mPaint?.run {
                typeface = FontUtils.getCommonTypeface(context)
                color = backgroundColor
                canvas.drawRoundRect(RectF(0F, 0F, width.toFloat(), height.toFloat()), radius.div(5F), radius.div(5F), this)
                color = textColor
                textSize = mTextSize.toFloat()
//                textSize = context.config.settingFontSize
                val text = "${getCurrentPosition().plus(1)}/${getPageSize()}"
                val textWidth = this.measureText(text).toInt()
                val fontMetricsInt = this.fontMetricsInt
                val baseline = ((measuredHeight - fontMetricsInt.bottom + fontMetricsInt.top) / 2
                        - fontMetricsInt.top)
                canvas.drawText(text, (width - textWidth) / 2f, baseline.toFloat(), this)
            }
        }
    }

    fun setRadius(radius: Int) {
        this.radius = radius
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        this.backgroundColor = backgroundColor
    }

    fun setTextSize(textSize: Int) {
        this.mTextSize = textSize
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
    }
}