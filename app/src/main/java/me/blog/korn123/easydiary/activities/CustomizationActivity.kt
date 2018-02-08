package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.ViewGroup
import io.github.hanjoongcho.commons.activities.BaseCustomizationActivity
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.BACKGROUND_ALPHA

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
    }
    
    override fun getMainViewGroup(): ViewGroup? = findViewById<ViewGroup>(R.id.main_holder)
    override fun getBackgroundAlpha(): Int = BACKGROUND_ALPHA
}

