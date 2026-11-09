package com.simplemobiletools.commons.extensions

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable

fun Resources.getColoredBitmap(resourceId: Int, newColor: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inMutable = true
    val bmp = BitmapFactory.decodeResource(this, resourceId, options)
    val paint = Paint()
    val filter = PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_IN)
    paint.colorFilter = filter
    val canvas = Canvas(bmp)
    canvas.drawBitmap(bmp, 0f, 0f, paint)
    return bmp
}

fun Resources.getColoredDrawable(drawableId: Int, colorId: Int, alpha: Int = 255) = getColoredDrawableWithColor(drawableId, getColor(colorId), alpha)

fun Resources.getColoredDrawableWithColor(drawableId: Int, color: Int, alpha: Int = 255): Drawable {
    val drawable = getDrawable(drawableId)
    drawable.mutate().applyColorFilter(color)
    drawable.mutate().alpha = alpha
    return drawable
}

fun Resources.hasNavBar(): Boolean {
    val id = getIdentifier("config_showNavigationBar", "bool", "android")
    return id > 0 && getBoolean(id)
}

fun Resources.getNavBarHeight(): Int {
    val id = getIdentifier("navigation_bar_height", "dimen", "android")
    return if (id > 0 && hasNavBar()) {
        getDimensionPixelSize(id)
    } else
        0
}
