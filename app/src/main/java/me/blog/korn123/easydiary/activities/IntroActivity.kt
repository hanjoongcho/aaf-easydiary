package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ActivityIntroBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-12-31.
 */

class IntroActivity : AppCompatActivity(), Handler.Callback {
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        forceInitRealmLessThanOreo()
        setContentView(binding.root)
        rescheduleEnabledAlarms()

        // determine device language
        if (!Locale.getDefault().language.matches(CUSTOM_FONTS_SUPPORT_LANGUAGE.toRegex())) {

            // Initial font typeface setting
            if (!preferencesContains(SETTING_FONT_NAME)) {
                config.settingFontName = CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT
            }

            // Initial font size setting
            if (!preferencesContains(SETTING_FONT_SIZE)) {
                config.settingFontSize = CommonUtils.dpToPixelFloatValue(this, DEFAULT_FONT_SIZE_UN_SUPPORT_LANGUAGE.toFloat())
            }
        } else {
            // Initial font size setting
            if (!preferencesContains(SETTING_FONT_SIZE)) {
                config.settingFontSize = CommonUtils.dpToPixelFloatValue(this, DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE.toFloat())
            }
        }
        Handler(this).sendEmptyMessageDelayed(START_MAIN_ACTIVITY, 500)
    }

    override fun onResume() {
        super.onResume()
        binding.root.run {
            FontUtils.setFontsTypeface(this@IntroActivity, assets, null, this)
            initTextSize(this)
            setBackgroundColor(config.primaryColor)
        }
    }

    override fun handleMessage(message: Message): Boolean {
        when (message.what) {
            START_MAIN_ACTIVITY -> {
                TransitionHelper.startActivityWithTransition(this, Intent(this, DiaryMainActivity::class.java))
                finish()
            }
            else -> {}
        }
        return false
    }
}
