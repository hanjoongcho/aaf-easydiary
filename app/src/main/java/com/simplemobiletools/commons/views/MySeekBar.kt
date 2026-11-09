package com.simplemobiletools.commons.views

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.widget.SeekBar

class MySeekBar : SeekBar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int, backgroundColor: Int) {
        progressDrawable.colorFilter = PorterDuffColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
        thumb.colorFilter = PorterDuffColorFilter(accentColor, PorterDuff.Mode.SRC_IN)
    }
}
