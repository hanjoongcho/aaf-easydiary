package me.blog.korn123.easydiary.activities

import android.view.ViewGroup
import io.github.hanjoongcho.commons.activities.BaseCustomizationActivity
import me.blog.korn123.easydiary.R

/**
 * Created by CHO HANJOONG on 2018-02-06.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

class CustomizationActivity : BaseCustomizationActivity() {
    override fun getMainViewGroup(): ViewGroup? = findViewById(R.id.main_holder)
}