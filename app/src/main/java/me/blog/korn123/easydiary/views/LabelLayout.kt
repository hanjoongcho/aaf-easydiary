package me.blog.korn123.easydiary.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class LabelLayout : LinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        setBackgroundColor(accentColor)
    }
}