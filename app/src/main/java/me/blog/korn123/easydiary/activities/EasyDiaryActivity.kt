package me.blog.korn123.easydiary.activities

import android.view.ViewGroup
import com.simplemobiletools.commons.models.Release
import io.github.aafactory.commons.activities.BaseSimpleActivity
import io.github.aafactory.commons.extensions.updateAppViews
import io.github.aafactory.commons.extensions.updateTextColors
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.APP_BACKGROUND_ALPHA

/**
 * Created by hanjoong on 2017-05-03.
 */

open class EasyDiaryActivity : BaseSimpleActivity() {
    var mCustomLineSpacing = true
    val mRootView: ViewGroup? by lazy {
        findViewById<ViewGroup>(R.id.main_holder)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
            updateCardViewPolicy(it)
        }
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<ViewGroup>(android.R.id.content), mCustomLineSpacing)
    }

    override fun getMainViewGroup(): ViewGroup? = mRootView
    override fun getBackgroundAlpha(): Int = APP_BACKGROUND_ALPHA

    fun checkWhatsNewDialog(applyFilter: Boolean = true) {
        arrayListOf<Release>().apply {
            add(Release(133, R.string.release_133))
            add(Release(132, R.string.release_132))
            add(Release(131, R.string.release_131))
            add(Release(130, R.string.release_130))
            add(Release(128, R.string.release_128))
            add(Release(126, R.string.release_126))
            add(Release(120, R.string.release_120))
            add(Release(118, R.string.release_118))
            add(Release(116, R.string.release_116))
            add(Release(114, R.string.release_114))
            add(Release(110, R.string.release_110))
            add(Release(105, R.string.release_105))
            add(Release(103, R.string.release_103))
            checkWhatsNew(this, BuildConfig.VERSION_CODE, applyFilter)
        }
    }
}
