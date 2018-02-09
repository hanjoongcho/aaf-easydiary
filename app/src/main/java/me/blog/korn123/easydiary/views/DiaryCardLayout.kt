package me.blog.korn123.easydiary.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * Created by Administrator on 2018-02-09.
 */

class DiaryCardLayout : LinearLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        val drawable = background as GradientDrawable
        drawable.setColor(backgroundColor)
    }
}