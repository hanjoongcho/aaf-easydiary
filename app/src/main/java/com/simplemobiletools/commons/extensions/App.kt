package com.simplemobiletools.commons.extensions

import android.app.Application
import java.util.*

fun Application.checkUseEnglish() {
    if (baseConfig.useEnglish) {
        val conf = resources.configuration
        conf.locale = Locale.ENGLISH
        resources.updateConfiguration(conf, resources.displayMetrics)
    }
}
