package me.blog.korn123.easydiary.extensions

import android.graphics.Color
import me.blog.korn123.easydiary.helper.ColorConstants.DARK_GREY
import java.util.Locale
import java.util.Random

fun Int.getContrastColor(): Int {
    val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
    return if (y >= 149) DARK_GREY else Color.WHITE
}

fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this).uppercase()

fun Int.adjustAlpha(factor: Float): Int {
    val alpha = Math.round(Color.alpha(this) * factor)
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

fun Int.getFormattedDuration(): String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this > 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}

// TODO: how to do "bits & ~bit" in kotlin?
fun Int.removeBit(bit: Int) = addBit(bit) - bit

fun Int.addBit(bit: Int) = this or bit

fun Int.flipBit(bit: Int) = if (this and bit == 0) addBit(bit) else removeBit(bit)

fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start

// taken from https://stackoverflow.com/a/40964456/1967672
fun Int.darkenColor(darkFactor: Int = 8): Int {
    if (this == Color.WHITE) {
        return -2105377
    } else if (this == Color.BLACK) {
        return Color.BLACK
    }

    var hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    val hsl = hsv2hsl(hsv)
    hsl[2] -= darkFactor / 100f
    if (hsl[2] < 0) {
        hsl[2] = 0f
    }
    hsv = hsl2hsv(hsl)
    return Color.HSVToColor(hsv)
}

private fun hsl2hsv(hsl: FloatArray): FloatArray {
    val hue = hsl[0]
    var sat = hsl[1]
    val light = hsl[2]
    sat *= if (light < .5) light else 1 - light
    return floatArrayOf(hue, 2f * sat / (light + sat), light + sat)
}

private fun hsv2hsl(hsv: FloatArray): FloatArray {
    val hue = hsv[0]
    val sat = hsv[1]
    val value = hsv[2]

    val newHue = (2f - sat) * value
    var newSat = sat * value / if (newHue < 1f) newHue else 2f - newHue
    if (newSat > 1f) {
        newSat = 1f
    }

    return floatArrayOf(hue, newSat, newHue / 2f)
}
