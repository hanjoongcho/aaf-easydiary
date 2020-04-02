package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import io.github.aafactory.commons.activities.BaseCustomizationActivity
import me.blog.korn123.commons.utils.FontUtils
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
    }

    override fun onPause() {
        super.onPause()
        pauseLock()
    }

    override fun onResume() {
        super.onResume()
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<ViewGroup>(android.R.id.content))
        resumeLock()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        return true
    }
    
    override fun getMainViewGroup(): ViewGroup? = findViewById<ViewGroup>(R.id.main_holder)
//    override fun getBackgroundAlpha(): Int = APP_BACKGROUND_ALPHA
}

