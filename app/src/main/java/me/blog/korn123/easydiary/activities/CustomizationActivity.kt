package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import io.github.hanjoongcho.commons.activities.BaseCustomizationActivity
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.resumeLock
import me.blog.korn123.easydiary.helper.APP_BACKGROUND_ALPHA

/**
 * Created by CHO HANJOONG on 2018-02-06.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

class CustomizationActivity : BaseCustomizationActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBackgroundColorFromPrimaryColor = true
        val backgroundLabel: TextView = findViewById<TextView>(R.id.customization_background_color_label)
        backgroundLabel.text = "${backgroundLabel.text}(다이어리 카드)"

    }

    override fun onPause() {
        super.onPause()
        pauseLock()
    }

    override fun onResume() {
        super.onResume()
        resumeLock()
    }
    
    override fun getMainViewGroup(): ViewGroup? = findViewById<ViewGroup>(R.id.main_holder)
    override fun getBackgroundAlpha(): Int = APP_BACKGROUND_ALPHA
}

