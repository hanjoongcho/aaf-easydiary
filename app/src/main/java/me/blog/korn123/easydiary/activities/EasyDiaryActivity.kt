package me.blog.korn123.easydiary.activities

import android.view.ViewGroup
import com.simplemobiletools.commons.models.Release
import io.github.aafactory.commons.activities.BaseSimpleActivity
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
        pauseLock()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    override fun onPause() {
        super.onPause()
        pauseLock()
    }

    override fun onResume() {
        super.onResume()
        if (config.updatePreference) {
            config.updatePreference = false
            startMainActivityWithClearTask()
        } else {
            resumeLock()
            mRootView?.let {
                initTextSize(it, this)
                updateTextColors(it)
                updateAppViews(it)
                updateCardViewPolicy(it)
            }
            FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<ViewGroup>(android.R.id.content), mCustomLineSpacing)
        }
    }

    override fun getMainViewGroup(): ViewGroup? = mRootView
    override fun getBackgroundAlpha(): Int = APP_BACKGROUND_ALPHA

    fun checkWhatsNewDialog(applyFilter: Boolean = true) {
        arrayListOf<Release>().apply {
            add(Release(192, R.string.release_192))
            add(Release(191, R.string.release_191))
            add(Release(190, R.string.release_190))
            add(Release(188, R.string.release_188))
            add(Release(187, R.string.release_187))
            add(Release(186, R.string.release_186))
            add(Release(185, R.string.release_185))
            add(Release(184, R.string.release_184))
            add(Release(182, R.string.release_182))
            add(Release(180, R.string.release_180))
            add(Release(179, R.string.release_179))
            add(Release(178, R.string.release_178))
            add(Release(176, R.string.release_176))
            add(Release(174, R.string.release_174))
            add(Release(173, R.string.release_173))
            add(Release(172, R.string.release_172))
            add(Release(171, R.string.release_171))
            add(Release(170, R.string.release_170))
            add(Release(169, R.string.release_169))
            add(Release(167, R.string.release_167))
            add(Release(165, R.string.release_165))
            add(Release(163, R.string.release_163))
            add(Release(160, R.string.release_160))
            add(Release(159, R.string.release_159))
            add(Release(157, R.string.release_157))
            add(Release(154, R.string.release_154))
            add(Release(152, R.string.release_152))
            add(Release(151, R.string.release_151))
            add(Release(150, R.string.release_150))
            add(Release(149, R.string.release_149))
            add(Release(147, R.string.release_147))
            add(Release(143, R.string.release_143))
            add(Release(141, R.string.release_141))
            add(Release(140, R.string.release_140))
            add(Release(139, R.string.release_139))
            add(Release(138, R.string.release_138))
            add(Release(137, R.string.release_137))
            add(Release(136, R.string.release_136))
            add(Release(134, R.string.release_134))
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
