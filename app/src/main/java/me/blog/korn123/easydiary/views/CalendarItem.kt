package me.blog.korn123.easydiary.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import me.blog.korn123.easydiary.R

class CalendarItem : AppCompatTextView {
    var applyStroke: Boolean = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        applyStroke = context.obtainStyledAttributes(attrs, R.styleable.CalendarItem).getBoolean(R.styleable.CalendarItem_applyStroke, false)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (applyStroke) {
            val colorState = textColors

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.0F
            setTextColor(Color.WHITE)
            super.onDraw(canvas)

            paint.style = Paint.Style.FILL
            setTextColor(colorState)
            super.onDraw(canvas)
        }
    }
}