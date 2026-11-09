package com.simplemobiletools.commons.extensions

import android.graphics.Paint
import android.widget.TextView

val TextView.value: String get() = text.toString().trim()

fun TextView.underlineText() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}
