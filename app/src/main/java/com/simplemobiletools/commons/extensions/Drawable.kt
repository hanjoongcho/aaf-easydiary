package com.simplemobiletools.commons.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable

fun Drawable.applyColorFilter(color: Int) = mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
