package me.blog.korn123.easydiary.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityIntroBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.forceInitRealmLessThanOreo
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.rescheduleEnabledAlarms
import me.blog.korn123.easydiary.extensions.updateStatusBarColor
import me.blog.korn123.easydiary.helper.EXECUTION_MODE_WELCOME_DASHBOARD
import me.blog.korn123.easydiary.helper.START_MAIN_ACTIVITY
import me.blog.korn123.easydiary.helper.TransitionHelper

/**
 * Created by CHO HANJOONG on 2016-12-31.
 */
class IntroActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityIntroBinding
    private lateinit var mHandler: Handler
    override fun onResume() {
        super.onResume()
        mBinding.root.run {
            FontUtils.setFontsTypeface(this@IntroActivity, null, this)
            initTextSize(this)
            setBackgroundColor(config.primaryColor)
            updateStatusBarColor(config.primaryColor)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        mBinding = ActivityIntroBinding.inflate(layoutInflater)
        forceInitRealmLessThanOreo()
        setContentView(mBinding.root)
        rescheduleEnabledAlarms()
        FontUtils.checkFontSetting(this)

        splashScreen.setKeepOnScreenCondition { true }
        mHandler = object: Handler(this.mainLooper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    START_MAIN_ACTIVITY -> {
                        TransitionHelper.startActivityWithTransition(
                            this@IntroActivity, Intent(this@IntroActivity, DiaryMainActivity::class.java).apply {
                                if (config.enableWelcomeDashboardPopup) putExtra(EXECUTION_MODE_WELCOME_DASHBOARD, true)
                            }
                        )
                        finish()
                    }
                    else -> {}
                }
            }
        }.apply { sendEmptyMessageDelayed(START_MAIN_ACTIVITY, 500) }
    }
}
