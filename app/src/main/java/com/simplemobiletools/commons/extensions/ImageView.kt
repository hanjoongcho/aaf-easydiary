package com.simplemobiletools.commons.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.widget.ImageView

fun ImageView.setBackgroundWithStroke(backgroundColor: Int, realStrokeColor: Int) {
    val strokeColor = realStrokeColor.getContrastColor()
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(backgroundColor)
        setStroke(2, strokeColor)
        setBackgroundDrawable(this)
    }
}

fun ImageView.applyColorFilter(color: Int) = setColorFilter(color, PorterDuff.Mode.SRC_IN)
