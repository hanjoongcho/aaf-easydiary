package me.blog.korn123.easydiary.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import me.blog.korn123.easydiary.R

open class FixedCardView : CardView {
    var fixedAppcompatPadding: Boolean = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        fixedAppcompatPadding = context.obtainStyledAttributes(attrs, R.styleable.FixedCardView).getBoolean(R.styleable.FixedCardView_fixedAppcompatPadding, false)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (fixedAppcompatPadding) useCompatPadding = true
    }
}