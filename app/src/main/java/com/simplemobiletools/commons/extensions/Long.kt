package com.simplemobiletools.commons.extensions

import android.text.format.DateFormat
import java.text.DecimalFormat
import java.util.*

fun Long.formatSize(): String {
    if (this <= 0)
        return "0 B"

    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(toDouble()) / Math.log10(1024.0)).toInt()
    return "${DecimalFormat("#,##0.#").format(this / Math.pow(1024.0, digitGroups.toDouble()))} ${units[digitGroups]}"
}

fun Long.formatDate(): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    return DateFormat.format("dd.MM.yyyy kk:mm", cal).toString()
}
