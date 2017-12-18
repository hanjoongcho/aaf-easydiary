package com.simplemobiletools.commons.extensions

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this)