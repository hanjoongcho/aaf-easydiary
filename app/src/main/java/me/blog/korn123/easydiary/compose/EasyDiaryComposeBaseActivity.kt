package me.blog.korn123.easydiary.compose

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.squareup.seismic.ShakeDetector
import me.blog.korn123.easydiary.activities.BaseSimpleActivity
import me.blog.korn123.easydiary.extensions.applyPolicyForRecentApps
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getStatusBarColor
import me.blog.korn123.easydiary.extensions.isBelowVanillaIceCream
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.resumeLock
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.viewmodels.SettingsViewModel

open class EasyDiaryComposeBaseActivity :
    BaseSimpleActivity(),
    ShakeDetector.Listener {
    val mSettingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (config.enableShakeDetector) setupMotionSensor()

        if (isBelowVanillaIceCream()) {
            @Suppress("DEPRECATION")
            window.statusBarColor = getStatusBarColor(config.primaryColor)
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishActivityWithTransition()
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()

        if (config.enableShakeDetector) mShakeDetector?.start(mSensorManager)

        resumeLock()
        applyPolicyForRecentApps()
    }

    override fun onPause() {
        super.onPause()

        mShakeDetector?.stop()
        pauseLock()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
//        hideSystemBars()
    }

    override fun hearShake() {
        if (this is QuickSettingsActivity) {
            return
        } else {
            TransitionHelper.startActivityWithTransition(
                this,
                Intent(this, QuickSettingsActivity::class.java),
            )
        }
    }

    fun finishActivityWithTransition() {
        TransitionHelper.finishActivityWithTransition(this@EasyDiaryComposeBaseActivity)
    }

    private var mSensorManager: SensorManager? = null
    private var mShakeDetector: ShakeDetector? = null

    private fun setupMotionSensor() {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mShakeDetector =
            ShakeDetector(this).apply { setSensitivity(ShakeDetector.SENSITIVITY_LIGHT) }
    }
}
