package me.blog.korn123.easydiary.activities

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.extensions.setBackgroundWithStroke
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityCustomizationBinding
import me.blog.korn123.easydiary.dialogs.LineColorPickerDialog
import me.blog.korn123.easydiary.enums.Launcher
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*

/**
 * Created by CHO HANJOONG on 2018-02-06.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

class CustomizationActivity : BaseSimpleActivity() {
    private lateinit var mActivityCustomizationBinding: ActivityCustomizationBinding
    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curScreenBackgroundColor = 0
    private var curPrimaryColor = 0
    private var hasUnsavedChanges = false
    private var isLineColorPickerVisible = false
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null
    private var launcher: Launcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityCustomizationBinding = ActivityCustomizationBinding.inflate(layoutInflater)
        mActivityCustomizationBinding.run {
            setContentView(this.root)

            setSupportActionBar(toolbar)
            supportActionBar?.run {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_cross)
            }

            updateTextColors(mainHolder)
            updateAppViews(mainHolder)
            updateCardViewPolicy(mainHolder)
            initTextSize(mainHolder)
            updateCustomThemeIcon(config.textColor)
            initColorVariables()
            setupColorsPickers()

            customizationTextColorHolder.setOnClickListener { pickTextColor() }
            customizationBackgroundColorHolder.setOnClickListener { pickBackgroundColor() }
            customizationScreenBackgroundColorHolder.setOnClickListener { pickScreenBackgroundColor() }
            customizationPrimaryColorHolder.setOnClickListener { pickPrimaryColor() }
            autoSetupEasyDiaryTheme.setOnClickListener {
                setCurrentPrimaryColor(Color.parseColor(EASYDIARY_THEME_PRIMARY_COLOR))
                setCurrentBackgroundColor(Color.parseColor(EASYDIARY_THEME_BACKGROUND_COLOR))
                setCurrentScreenBackgroundColor(Color.parseColor(EASYDIARY_THEME_SCREEN_BACKGROUND_COLOR))
                setCurrentTextColor(Color.parseColor(EASYDIARY_THEME_TEXT_COLOR))
                colorChanged()
                launcher = Launcher.EASY_DIARY
            }
            autoSetupDarkTheme.setOnClickListener {
                setCurrentPrimaryColor(Color.parseColor(DARK_THEME_PRIMARY_COLOR))
                setCurrentBackgroundColor(Color.parseColor(DARK_THEME_BACKGROUND_COLOR))
                setCurrentScreenBackgroundColor(Color.parseColor(DARK_THEME_SCREEN_BACKGROUND_COLOR))
                setCurrentTextColor(Color.parseColor(DARK_THEME_TEXT_COLOR))
                colorChanged()
                launcher = Launcher.DARK
            }
            autoSetupGreenTheme.setOnClickListener {
                setCurrentPrimaryColor(Color.parseColor(GREEN_THEME_PRIMARY_COLOR))
                setCurrentBackgroundColor(Color.parseColor(GREEN_THEME_BACKGROUND_COLOR))
                setCurrentScreenBackgroundColor(Color.parseColor(GREEN_THEME_SCREEN_BACKGROUND_COLOR))
                setCurrentTextColor(Color.parseColor(GREEN_THEME_TEXT_COLOR))
                colorChanged()
                launcher = Launcher.GREEN
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pauseLock()
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundColor(curScreenBackgroundColor)
        updateActionbarColor(curPrimaryColor)
        setTheme(getThemeId(curPrimaryColor))

        curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            updateActionbarColor(this)
            setTheme(getThemeId(this))
        }

        FontUtils.setFontsTypeface(applicationContext, null, findViewById(android.R.id.content))
        resumeLock()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_customization, menu)
        menu.findItem(R.id.save).isVisible = hasUnsavedChanges
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> saveChanges()
            android.R.id.home -> super.onBackPressed()
//            else -> return super.onOptionsItemSelected(item)
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        return true
    }

    override fun onBackPressed() {
        if (!hasUnsavedChanges) super.onBackPressed()

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun getMainViewGroup(): ViewGroup? = findViewById<ViewGroup>(R.id.main_holder)

    private fun updateCustomThemeIcon(color: Int) {
        mActivityCustomizationBinding.run {
            updateDrawableColorInnerCardView(imageAutoSetupEasyDiaryTheme, color)
            updateDrawableColorInnerCardView(imageAutoSetupDarkTheme, color)
            updateDrawableColorInnerCardView(imageAutoSetupGreenTheme, color)
        }
    }

    private fun saveChanges() {
        config.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            screenBackgroundColor = curScreenBackgroundColor
            primaryColor = curPrimaryColor
            isThemeChanged = true
        }
        finish()
        launcher?.let { toggleLauncher(it) }
    }

    private fun initColorVariables() {
        curTextColor = config.textColor
        curBackgroundColor = config.backgroundColor
        curScreenBackgroundColor = config.screenBackgroundColor
        curPrimaryColor = config.primaryColor
    }

    private fun setupColorsPickers() {
        mActivityCustomizationBinding.run {
            customizationTextColor.setBackgroundWithStroke(curTextColor, curBackgroundColor)
            customizationPrimaryColor.setBackgroundWithStroke(curPrimaryColor, curBackgroundColor)
            customizationBackgroundColor.setBackgroundWithStroke(curBackgroundColor, curBackgroundColor)
            customizationScreenBackgroundColor.setBackgroundWithStroke(curScreenBackgroundColor, curBackgroundColor)
        }
    }

    private fun hasColorChanged(old: Int, new: Int) = Math.abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        invalidateOptionsMenu()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
        updateTextColors(mActivityCustomizationBinding.mainHolder, curTextColor)
        updateCustomThemeIcon(curTextColor)
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
        updateAppViews(mActivityCustomizationBinding.mainHolder, curBackgroundColor)
    }

    private fun setCurrentScreenBackgroundColor(color: Int) {
        curScreenBackgroundColor = color
        updateBackgroundColor(color)
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
        setCurrentScreenBackgroundColor(color.darkenColor(AUTO_SETUP_SCREEN_BACKGROUND_DARKEN_COLOR))
        updateActionbarColor(color)
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, curTextColor) {
            if (hasColorChanged(curTextColor, it)) {
                setCurrentTextColor(it)
                colorChanged()
            }
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, curBackgroundColor) {
            if (hasColorChanged(curBackgroundColor, it)) {
                setCurrentBackgroundColor(it)
                colorChanged()
            }
        }
    }

    private fun pickScreenBackgroundColor() {
        ColorPickerDialog(this, curScreenBackgroundColor) {
            if (hasColorChanged(curScreenBackgroundColor, it)) {
                setCurrentScreenBackgroundColor(it)
                colorChanged()
            }
        }
    }

    private fun pickPrimaryColor() {
        isLineColorPickerVisible = true
        curPrimaryLineColorPicker = LineColorPickerDialog(this, curPrimaryColor) { wasPositivePressed, color ->
            curPrimaryLineColorPicker = null
            isLineColorPickerVisible = false
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    setTheme(getThemeId(color))
                }
            } else {
                updateActionbarColor(curPrimaryColor)
                setTheme(getThemeId(curPrimaryColor))
                updateBackgroundColor(curScreenBackgroundColor)
            }
        }.apply {
            setDarkenColorOptionChangeListener(config.enableStatusBarDarkenColor) { enableStatusBarDarkenColor, color ->
                config.enableStatusBarDarkenColor = enableStatusBarDarkenColor
                updateStatusBarColor(color)
            }
        }
    }
}

