package com.simplemobiletools.commons.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.models.MyTheme
//import com.simplemobiletools.commons.helpers.MyContentProvider
//import com.simplemobiletools.commons.models.MyTheme
//import com.simplemobiletools.commons.models.RadioItem
//import com.simplemobiletools.commons.models.SharedTheme
import kotlinx.android.synthetic.main.activity_customization.*
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import java.util.*

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

class CustomizationActivity : BaseSimpleActivity() {
    private val THEME_LIGHT = 0
    private val THEME_DARK = 1
    private val THEME_SOLARIZED = 2
    private val THEME_DARK_RED = 3
    private val THEME_CUSTOM = 4
    private val THEME_SHARED = 5

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curSelectedThemeId = 0
    private var hasUnsavedChanges = false
    private var isLineColorPickerVisible = false
    private var predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null
//    private var storedSharedTheme: SharedTheme? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customization)

        predefinedThemes.apply {
//            put(THEME_LIGHT, MyTheme(R.string.light_theme, R.color.theme_light_text_color, R.color.theme_light_background_color, R.color.color_primary))
//            put(THEME_DARK, MyTheme(R.string.dark_theme, R.color.theme_dark_text_color, R.color.theme_dark_background_color, R.color.color_primary))
            //put(THEME_SOLARIZED, MyTheme(R.string.solarized, R.color.theme_solarized_text_color, R.color.theme_solarized_background_color, R.color.theme_solarized_primary_color))
//            put(THEME_DARK_RED, MyTheme(R.string.dark_red, R.color.theme_dark_text_color, R.color.theme_dark_background_color, R.color.theme_dark_red_primary_color))
            put(THEME_CUSTOM, MyTheme(R.string.custom, 0, 0, 0))

//            if (baseConfig.wasSharedThemeEverActivated) {
//                put(THEME_SHARED, MyTheme(R.string.shared, 0, 0, 0))
//            }
        }

//        if (!isThankYouInstalled()) {
//            baseConfig.isUsingSharedTheme = false
//        }

//        if (baseConfig.wasSharedThemeEverActivated) {
//            getSharedTheme {
//                storedSharedTheme = it
//            }
//        }

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true);
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        updateTextColors(customization_holder)
        initColorVariables()
        setupColorsPickers()

        customization_text_color_holder.setOnClickListener { pickTextColor() }
        customization_background_color_holder.setOnClickListener { pickBackgroundColor() }
        customization_primary_color_holder.setOnClickListener { pickPrimaryColor() }
//        apply_to_all_holder.setOnClickListener { applyToAll() }
//        apply_to_all_holder.beGoneIf(baseConfig.wasSharedThemeEverActivated)
        setupThemePicker()
    }

    override fun onResume() {
        super.onResume()
//        updateBackgroundColor(curBackgroundColor)
        updateActionbarColor(curPrimaryColor)
        setTheme(getThemeId(curPrimaryColor))

        curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            updateActionbarColor(this)
            setTheme(getThemeId(this))
        }

        setFontsStyle()
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<View>(android.R.id.content) as ViewGroup)
        val fontSize = CommonUtils.loadFloatPreference(this, Constants.SETTING_FONT_SIZE, -1f)
        if (fontSize > 0) FontUtils.setFontsSize(fontSize, findViewById<View>(android.R.id.content) as ViewGroup)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_customization, menu)
        menu.findItem(R.id.save).isVisible = hasUnsavedChanges
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> saveChanges(true)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (hasUnsavedChanges) {
//            promptSaveDiscard()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
//        customization_theme.text = getThemeText()
//        customization_theme_holder.setOnClickListener {
//            val items = arrayListOf<RadioItem>()
//            for ((key, value) in predefinedThemes) {
//                items.add(RadioItem(key, getString(value.nameId)))
//            }
//
//            RadioGroupDialog(this@CustomizationActivity, items, curSelectedThemeId) {
//                if (it == THEME_SHARED && !isThankYouInstalled()) {
//                    PurchaseThankYouDialog(this)
//                    return@RadioGroupDialog
//                }
//
//                updateColorTheme(it as Int, true)
//                if (it != THEME_CUSTOM && it != THEME_SHARED && !baseConfig.wasCustomThemeSwitchDescriptionShown) {
//                    baseConfig.wasCustomThemeSwitchDescriptionShown = true
//                    toast(R.string.changing_color_description)
//                }
//            }
//        }
    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
//        customization_theme.text = getThemeText()

        resources.apply {
            if (curSelectedThemeId == THEME_CUSTOM) {
                if (useStored) {
                    curTextColor = baseConfig.customTextColor
                    curBackgroundColor = baseConfig.customBackgroundColor
                    curPrimaryColor = baseConfig.customPrimaryColor
                    setTheme(getThemeId(curPrimaryColor))
                    setupColorsPickers()
                } else {
                    baseConfig.customPrimaryColor = curPrimaryColor
                    baseConfig.customBackgroundColor = curBackgroundColor
                    baseConfig.customTextColor = curTextColor
                }
//            } else if (curSelectedThemeId == THEME_SHARED) {
//                if (useStored) {
//                    storedSharedTheme?.apply {
//                        curTextColor = textColor
//                        curBackgroundColor = backgroundColor
//                        curPrimaryColor = primaryColor
//                    }
//                    setTheme(getThemeId(curPrimaryColor))
//                    setupColorsPickers()
//                }
//            } else {
//                val theme = predefinedThemes[curSelectedThemeId]!!
//                curTextColor = getColor(theme.textColorId)
//                curBackgroundColor = getColor(theme.backgroundColorId)
//                curPrimaryColor = getColor(theme.primaryColorId)
//                setTheme(getThemeId(curPrimaryColor))
//                colorChanged()
            }
        }

        hasUnsavedChanges = true
        invalidateOptionsMenu()
        updateTextColors(customization_holder, curTextColor)
        updateBackgroundColor(curBackgroundColor)
        updateActionbarColor(curPrimaryColor)
    }

    private fun getCurrentThemeId(): Int {
        if (baseConfig.isUsingSharedTheme)
            return THEME_SHARED

        var themeId = THEME_CUSTOM
        resources.apply {
            for ((key, value) in predefinedThemes.filter { it.key != THEME_CUSTOM && it.key != THEME_SHARED }) {
                if (curTextColor == getColor(value.textColorId) && curBackgroundColor == getColor(value.backgroundColorId) && curPrimaryColor == getColor(value.primaryColorId)) {
                    themeId = key
                }
            }
        }
        return themeId
    }

//    private fun getThemeText(): String {
//        var nameId = R.string.custom
//        for ((key, value) in predefinedThemes) {
//            if (key == curSelectedThemeId) {
//                nameId = value.nameId
//            }
//        }
//        return getString(nameId)
//    }

//    private fun promptSaveDiscard() {
//        ConfirmationAdvancedDialog(this, "", R.string.save_before_closing, R.string.save, R.string.discard) {
//            if (it) {
//                saveChanges(true)
//            } else {
//                resetColors()
//                finish()
//            }
//        }
//    }

    private fun saveChanges(finishAfterSave: Boolean) {
        baseConfig.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            primaryColor = curPrimaryColor
        }

//        if (curSelectedThemeId == THEME_SHARED) {
//            val newSharedTheme = SharedTheme(curTextColor, curBackgroundColor, curPrimaryColor)
//            updateSharedTheme(newSharedTheme)
//            Intent().apply {
//                action = MyContentProvider.SHARED_THEME_UPDATED
//                sendBroadcast(this)
//            }
//        }
        baseConfig.isUsingSharedTheme = curSelectedThemeId == THEME_SHARED
        hasUnsavedChanges = false
        if (finishAfterSave) {
            val readDiaryIntent = Intent(this@CustomizationActivity, DiaryMainActivity::class.java)
            readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(readDiaryIntent)
            finish()
        } else {
            invalidateOptionsMenu()
        }
    }

//    private fun resetColors() {
//        hasUnsavedChanges = false
//        invalidateOptionsMenu()
//        initColorVariables()
//        setupColorsPickers()
//        updateBackgroundColor()
//        updateActionbarColor()
//        invalidateOptionsMenu()
//        updateTextColors(customization_holder)
//    }

    private fun initColorVariables() {
        curTextColor = baseConfig.textColor
        curBackgroundColor = baseConfig.backgroundColor
        curPrimaryColor = baseConfig.primaryColor
    }

    private fun setupColorsPickers() {
        customization_text_color.setBackgroundWithStroke(curTextColor, curBackgroundColor)
        customization_primary_color.setBackgroundWithStroke(curPrimaryColor, curBackgroundColor)
        customization_background_color.setBackgroundWithStroke(curBackgroundColor, curBackgroundColor)
    }

    private fun hasColorChanged(old: Int, new: Int) = Math.abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        invalidateOptionsMenu()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
//        updateTextColors(customization_holder, color)
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
//        updateBackgroundColor(color)
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
        updateActionbarColor(color)
    }

    private fun pickTextColor() {

        AlertDialog.Builder(this@CustomizationActivity)?.apply {
            setMessage(getString(R.string.pick_text_color_guide_message))
            setPositiveButton(getString(R.string.ok), null)
        }.create().show()

//        ColorPickerDialog(this, curTextColor) {
//            if (hasColorChanged(curTextColor, it)) {
//                setCurrentTextColor(it)
//                colorChanged()
//                updateColorTheme(getUpdatedTheme())
//            }
//        }
    }

    private fun pickBackgroundColor() {

        AlertDialog.Builder(this@CustomizationActivity)?.apply {
            setMessage(getString(R.string.pick_background_color_guide_message))
            setPositiveButton(getString(R.string.ok), null)
        }.create().show()
//        ColorPickerDialog(this, curBackgroundColor) {
//            if (hasColorChanged(curBackgroundColor, it)) {
//                setCurrentBackgroundColor(it)
//                colorChanged()
//                updateColorTheme(getUpdatedTheme())
//            }
//        }
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
                    updateColorTheme(getUpdatedTheme())
                    setTheme(getThemeId(color))
                }
            } else {
                updateActionbarColor(curPrimaryColor)
                setTheme(getThemeId(curPrimaryColor))
            }
        }
    }

    private fun getUpdatedTheme() = if (curSelectedThemeId == THEME_SHARED) THEME_SHARED else THEME_CUSTOM

    private fun applyToAll() {
//        if (isThankYouInstalled()) {
//            ConfirmationDialog(this, "", R.string.share_colors_success, R.string.ok, 0) {
//                Intent().apply {
//                    action = MyContentProvider.SHARED_THEME_ACTIVATED
//                    sendBroadcast(this)
//                }
//
//                predefinedThemes.put(THEME_SHARED, MyTheme(R.string.shared, 0, 0, 0))
//                baseConfig.wasSharedThemeEverActivated = true
//                apply_to_all_holder.beGone()
//                updateColorTheme(THEME_SHARED)
//                saveChanges(false)
//            }
//        } else {
//            PurchaseThankYouDialog(this)
//        }
    }
}
