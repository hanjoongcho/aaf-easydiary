package com.simplemobiletools.commons.activities

import android.app.ActivityManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import com.simplemobiletools.commons.extensions.*

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

open class BaseSimpleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun updateActionbarColor(color: Int = baseConfig.primaryColor) {
//        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
//        supportActionBar?.title = Html.fromHtml("<font color='${color.getContrastColor().toHex()}'>${supportActionBar?.title}</font>")
//        updateStatusbarColor(color)
//
//        if (isLollipopPlus()) {
//            setTaskDescription(ActivityManager.TaskDescription(null, null, color))
//        }
    }

}
