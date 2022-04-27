package me.blog.korn123.easydiary.dialogs

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import com.simplemobiletools.commons.extensions.copyToClipboard
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toHex
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.commons.interfaces.LineColorPickerListener
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BaseSimpleActivity
import me.blog.korn123.easydiary.databinding.DialogLineColorPickerBinding
import me.blog.korn123.easydiary.extensions.darkenColor
import me.blog.korn123.easydiary.extensions.getThemeId
import me.blog.korn123.easydiary.helper.AUTO_SETTUP_SCREEN_BACKGROUND_DARKEN_COLOR

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

class LineColorPickerDialog(val activity: BaseSimpleActivity, val color: Int, val callback: (wasPositivePressed: Boolean, color: Int) -> Unit) {
    private var mDialogLineColorPickerBinding: DialogLineColorPickerBinding = DialogLineColorPickerBinding.inflate(activity.layoutInflater)
    private val PRIMARY_COLORS_COUNT = 19
    private val DEFAULT_PRIMARY_COLOR_INDEX = 7
    private val DEFAULT_SECONDARY_COLOR_INDEX = 6
    private val DEFAULT_COLOR_VALUE = activity.resources.getColor(R.color.colorPrimary)
    private var dialog: AlertDialog? = null

    init {
        mDialogLineColorPickerBinding.run {
            hexCode.text = color.toHex()
            hexCode.setOnLongClickListener { activity.copyToClipboard(hexCode.value.substring(1)); true }
            val indexes = getColorIndexes(color)

            primaryLineColorPicker.updateColors(getColors(R.array.md_primary_colors), indexes.first)
            primaryLineColorPicker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondaryLineColorPicker.updateColors(secondaryColors)
                    colorUpdated(secondaryLineColorPicker.getCurrentColor())
                }
            }

            secondaryLineColorPicker.updateColors(getColorsForIndex(indexes.first), indexes.second)
            secondaryLineColorPicker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    colorUpdated(color)
                }
            }
        }

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, { dialog, which -> dialogConfirmed() })
                .setNegativeButton(R.string.cancel, { dialog, which -> dialogDismissed() })
                .setOnCancelListener { dialogDismissed() }
                .create().apply {
            activity.setupDialogStuff(mDialogLineColorPickerBinding.root, this)
        }
    }

    fun getSpecificColor() = mDialogLineColorPickerBinding.secondaryLineColorPicker.getCurrentColor()

    private fun colorUpdated(color: Int) {
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        activity.updateActionbarColor(color)
        activity.setTheme(activity.getThemeId(color))
        activity.updateBackgroundColor(color.darkenColor(AUTO_SETTUP_SCREEN_BACKGROUND_DARKEN_COLOR))
        mDialogLineColorPickerBinding.run {
            hexCode.text = color.toHex()
            switchStatusBarDarkenColor.run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    trackTintList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
                        Color.parseColor("#AEAEAE"),
                        ColorUtils.setAlphaComponent(color, 100),
                    ))
                    thumbTintList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
                        Color.parseColor("#EAE4E4"),
                        color
                    ))
                }
            }
        }
    }

    private fun getColorIndexes(color: Int): Pair<Int, Int> {
        if (color == DEFAULT_COLOR_VALUE) {
            return getDefaultColorPair()
        }

        for (i in 0 until PRIMARY_COLORS_COUNT) {
            val colors = getColorsForIndex(i)
            val size = colors.size
            (0 until size).filter { color == colors[it] }
                    .forEach { return Pair(i, it) }
        }

        return getDefaultColorPair()
    }

    private fun getDefaultColorPair() = Pair(DEFAULT_PRIMARY_COLOR_INDEX, DEFAULT_SECONDARY_COLOR_INDEX)

    private fun dialogDismissed() {
        callback(false, 0)
    }

    private fun dialogConfirmed() {
        val color = mDialogLineColorPickerBinding.secondaryLineColorPicker.getCurrentColor()
        callback(true, color)
    }

    private fun getColorsForIndex(index: Int) = when (index) {
        0 -> getColors(R.array.md_reds)
        1 -> getColors(R.array.md_pinks)
        2 -> getColors(R.array.md_purples)
        3 -> getColors(R.array.md_deep_purples)
        4 -> getColors(R.array.md_indigos)
        5 -> getColors(R.array.md_blues)
        6 -> getColors(R.array.md_light_blues)
        7 -> getColors(R.array.md_cyans)
        8 -> getColors(R.array.md_teals)
        9 -> getColors(R.array.md_greens)
        10 -> getColors(R.array.md_light_greens)
        11 -> getColors(R.array.md_limes)
        12 -> getColors(R.array.md_yellows)
        13 -> getColors(R.array.md_ambers)
        14 -> getColors(R.array.md_oranges)
        15 -> getColors(R.array.md_deep_oranges)
        16 -> getColors(R.array.md_browns)
        17 -> getColors(R.array.md_greys)
        18 -> getColors(R.array.md_blue_greys)
        else -> throw RuntimeException("Invalid color id $index")
    }

    private fun getColors(id: Int) = activity.resources.getIntArray(id).toCollection(ArrayList())

    private var mDarkenColorOptionChangeCallback: ((enableStatusBarDarkenColor: Boolean, color: Int) -> Unit)? = null
    fun setDarkenColorOptionChangeListener(currentOption: Boolean, callback: (enableStatusBarDarkenColor: Boolean, color: Int) -> Unit): Unit {
        mDarkenColorOptionChangeCallback = callback
        mDialogLineColorPickerBinding.switchStatusBarDarkenColor.run {
            isChecked = currentOption
            setOnCheckedChangeListener { _, isChecked ->
                callback.invoke(isChecked, mDialogLineColorPickerBinding.secondaryLineColorPicker.getCurrentColor())
            }
        }
    }
}
