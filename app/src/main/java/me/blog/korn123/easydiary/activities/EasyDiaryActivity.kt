package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.models.Release
import com.squareup.seismic.ShakeDetector
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.compose.QuickSettingsActivity
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.TransitionHelper

/**
 * Created by hanjoong on 2017-05-03.
 */

open class EasyDiaryActivity : BaseSimpleActivity(), ShakeDetector.Listener {
    var mCustomLineSpacing = true


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = !isNightMode()
        super.onCreate(savedInstanceState)

        if (config.enableDebugMode) setupMotionSensor()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (arrayOf("DiaryMainActivity", "DashboardActivity").contains(this::class.java.simpleName)) {
            applyHorizontalInsets()
        } else {
            hideSystemBars()
        }
    }

    override fun onResume() {
        useDynamicTheme = !isNightMode()
        super.onResume()

        if (config.enableShakeDetector) mShakeDetector?.start(mSensorManager)

        if (config.updatePreference) {
            config.updatePreference = false
            startMainActivityWithClearTask()
        } else {
            resumeLock()
            getMainViewGroup()?.let {
                initTextSize(it)
                updateTextColors(it)
                updateAppViews(it)
                updateCardViewPolicy(it)
            }
            updateBackgroundColor(config.screenBackgroundColor)
            FontUtils.setFontsTypeface(
                applicationContext,
                null,
                findViewById(android.R.id.content),
                mCustomLineSpacing
            )
        }
        applyPolicyForRecentApps()
    }

    override fun onPause() {
        super.onPause()

        mShakeDetector?.stop()
        pauseLock()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        pauseLock()
        TransitionHelper.finishActivityWithTransition(this)
    }

    override fun getMainViewGroup(): ViewGroup? = if (findViewById<View>(R.id.main_holder) != null) findViewById(R.id.main_holder) else findViewById(R.id.compose_view)

    override fun hearShake() {
        TransitionHelper.startActivityWithTransition(
            this,
            Intent(this, QuickSettingsActivity::class.java)
        )
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    fun checkWhatsNewDialog(applyFilter: Boolean = true) {
        arrayListOf<Release>().apply {
            add(Release(330, R.string.release_330))
            add(Release(329, R.string.release_329))
            add(Release(328, R.string.release_328))
            add(Release(327, R.string.release_327))
            add(Release(326, R.string.release_326))
            add(Release(325, R.string.release_325))
            add(Release(324, R.string.release_324))
            add(Release(319, R.string.release_319))
            add(Release(318, R.string.release_318))
            add(Release(317, R.string.release_317))
            add(Release(313, R.string.release_313))
            add(Release(312, R.string.release_312))
            add(Release(309, R.string.release_309))
            add(Release(308, R.string.release_308))
            add(Release(307, R.string.release_307))
            add(Release(306, R.string.release_306))
            add(Release(305, R.string.release_305))
            add(Release(304, R.string.release_304))
            add(Release(303, R.string.release_303))
            add(Release(302, R.string.release_302))
            add(Release(301, R.string.release_301))
            add(Release(300, R.string.release_300))
            add(Release(299, R.string.release_299))
            add(Release(298, R.string.release_298))
            add(Release(297, R.string.release_297))
            add(Release(295, R.string.release_295))
            add(Release(294, R.string.release_294))
            add(Release(293, R.string.release_293))
            add(Release(292, R.string.release_292))
            add(Release(291, R.string.release_291))
            add(Release(289, R.string.release_289))
            add(Release(288, R.string.release_288))
            add(Release(287, R.string.release_287))
            add(Release(286, R.string.release_286))
            add(Release(285, R.string.release_285))
            add(Release(284, R.string.release_284))
            add(Release(280, R.string.release_280))
            add(Release(279, R.string.release_279))
            add(Release(277, R.string.release_277))
            add(Release(275, R.string.release_275))
            add(Release(274, R.string.release_274))
            add(Release(273, R.string.release_273))
            add(Release(272, R.string.release_272))
            add(Release(271, R.string.release_271))
            add(Release(269, R.string.release_269))
            add(Release(268, R.string.release_268))
            add(Release(267, R.string.release_267))
            add(Release(266, R.string.release_266))
            add(Release(265, R.string.release_265))
            add(Release(264, R.string.release_264))
            add(Release(263, R.string.release_263))
            add(Release(262, R.string.release_262))
            add(Release(261, R.string.release_261))
            add(Release(260, R.string.release_260))
            add(Release(259, R.string.release_259))
            add(Release(258, R.string.release_258))
            add(Release(257, R.string.release_257))
            add(Release(252, R.string.release_252))
            add(Release(251, R.string.release_251))
            add(Release(250, R.string.release_250))
            add(Release(246, R.string.release_246))
            add(Release(244, R.string.release_244))
            add(Release(243, R.string.release_243))
            add(Release(242, R.string.release_242))
            add(Release(241, R.string.release_241))
            add(Release(238, R.string.release_238))
            add(Release(237, R.string.release_237))
            add(Release(235, R.string.release_235))
            add(Release(234, R.string.release_234))
            add(Release(233, R.string.release_233))
            add(Release(232, R.string.release_232))
            add(Release(230, R.string.release_230))
            add(Release(229, R.string.release_229))
            add(Release(227, R.string.release_227))
            add(Release(225, R.string.release_225))
            add(Release(222, R.string.release_222))
            add(Release(221, R.string.release_221))
            add(Release(220, R.string.release_220))
            add(Release(219, R.string.release_219))
            add(Release(218, R.string.release_218))
            add(Release(217, R.string.release_217))
            add(Release(216, R.string.release_216))
            add(Release(214, R.string.release_214))
            add(Release(212, R.string.release_212))
            add(Release(211, R.string.release_211))
            add(Release(209, R.string.release_209))
            add(Release(207, R.string.release_207))
            add(Release(206, R.string.release_206))
            add(Release(205, R.string.release_205))
            add(Release(203, R.string.release_203))
            add(Release(202, R.string.release_202))
            add(Release(201, R.string.release_201))
            add(Release(200, R.string.release_200))
            add(Release(198, R.string.release_198))
            add(Release(197, R.string.release_197))
            add(Release(196, R.string.release_196))
            add(Release(195, R.string.release_195))
            add(Release(193, R.string.release_193))
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

    private var mSensorManager: SensorManager? = null
    private var mShakeDetector: ShakeDetector? = null
    private fun setupMotionSensor() {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mShakeDetector =
            ShakeDetector(this).apply { setSensitivity(ShakeDetector.SENSITIVITY_LIGHT) }
    }
}
