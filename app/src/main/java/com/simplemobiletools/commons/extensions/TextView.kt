package com.simplemobiletools.commons.extensions

import android.widget.TextView

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

val TextView.value: String get() = text.toString().trim()
