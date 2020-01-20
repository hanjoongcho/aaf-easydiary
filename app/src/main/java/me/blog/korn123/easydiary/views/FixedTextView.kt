package me.blog.korn123.easydiary.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import me.blog.korn123.easydiary.R

class FixedTextView : AppCompatTextView {
    var applyGlobalSize: Boolean = true
    var applyGlobalColor: Boolean = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        applyGlobalSize = context.obtainStyledAttributes(attrs, R.styleable.FixedTextView).getBoolean(R.styleable.FixedTextView_applyGlobalSize, true)
        applyGlobalColor = context.obtainStyledAttributes(attrs, R.styleable.FixedTextView).getBoolean(R.styleable.FixedTextView_applyGlobalColor, true)
    }

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        setTextColor(textColor)
        setLinkTextColor(accentColor)
    }
}