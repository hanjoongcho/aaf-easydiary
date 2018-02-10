package me.blog.korn123.easydiary.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * Created by CHO HANJOONG on 2017-12-19.
 */

class LabelLayout : LinearLayout {
    constructor(context: Context) : super(context) 

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) 

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) 

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        setBackgroundColor(accentColor)
    }
}
