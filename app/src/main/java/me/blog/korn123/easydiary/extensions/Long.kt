package me.blog.korn123.easydiary.extensions
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

fun Long.toDisplaySize(): String {
    if (this <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()

    val value = this / 1024.0.pow(digitGroups.toDouble())
    val df = DecimalFormat("#,##0.##") // 소수점 둘째 자리까지 표시

    return "${df.format(value)} ${units[digitGroups]}"
}
