package me.blog.korn123.easydiary.activities

import android.view.ViewGroup
import io.github.hanjoongcho.commons.activities.BaseSimpleActivity
import io.github.hanjoongcho.commons.extensions.updateAppViews
import io.github.hanjoongcho.commons.extensions.updateTextColors
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.resumeLock
import me.blog.korn123.easydiary.helper.APP_BACKGROUND_ALPHA

/**
 * Created by hanjoong on 2017-05-03.
 */

open class EasyDiaryActivity : BaseSimpleActivity() {
    val mRootView: ViewGroup? by lazy {
        findViewById<ViewGroup>(R.id.main_holder)
    }
    
    override fun onPause() {
        super.onPause()
        pauseLock()
    }

    override fun onResume() {
        isBackgroundColorFromPrimaryColor = true
        super.onResume()
        resumeLock()
        
        mRootView?.let { 
            initTextSize(it, this)
            updateTextColors(it)
            updateAppViews(it)
        }
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<ViewGroup>(android.R.id.content))
    }

    override fun getMainViewGroup(): ViewGroup? = mRootView
    override fun getBackgroundAlpha(): Int = APP_BACKGROUND_ALPHA
}
