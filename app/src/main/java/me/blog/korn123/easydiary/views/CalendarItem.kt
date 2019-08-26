package me.blog.korn123.easydiary.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class CalendarItem : TextView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}