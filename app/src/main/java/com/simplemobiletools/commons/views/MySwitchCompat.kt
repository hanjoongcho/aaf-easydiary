package com.simplemobiletools.commons.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.simplemobiletools.commons.extensions.adjustAlpha
import me.blog.korn123.easydiary.R

class MySwitchCompat : SwitchCompat {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(
        textColor: Int,
        accentColor: Int,
        backgroundColor: Int,
    ) {
        setTextColor(textColor)
        val states = arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked))
        val thumbColors = intArrayOf(resources.getColor(R.color.thumb_deactivated), accentColor)
        val trackColors = intArrayOf(resources.getColor(R.color.track_deactivated), accentColor.adjustAlpha(0.3f))
        DrawableCompat.setTintList(DrawableCompat.wrap(thumbDrawable), ColorStateList(states, thumbColors))
        DrawableCompat.setTintList(DrawableCompat.wrap(trackDrawable), ColorStateList(states, trackColors))
    }
}
