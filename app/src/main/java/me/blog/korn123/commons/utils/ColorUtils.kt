package me.blog.korn123.commons.utils

import android.graphics.Color
import android.support.annotation.ColorInt

/**
 * Created by CHO HANJOONG on 2018-05-20.
 */

object ColorUtils {

    @ColorInt
    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}