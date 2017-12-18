package com.simplemobiletools.commons.extensions

import android.graphics.Color

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

fun Int.getContrastColor(): Int {
    val DARK_GREY = -13421773
    val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
    return if (y >= 186) DARK_GREY else Color.WHITE
}

fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this)