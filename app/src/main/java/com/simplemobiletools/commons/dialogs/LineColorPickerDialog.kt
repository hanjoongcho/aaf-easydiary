package com.simplemobiletools.commons.dialogs

import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.interfaces.LineColorPickerListener
import kotlinx.android.synthetic.main.dialog_line_color_picker.view.*
import me.blog.korn123.easydiary.R
import java.util.*

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

class LineColorPickerDialog(val activity: BaseSimpleActivity, val color: Int, val callback: (wasPositivePressed: Boolean, color: Int) -> Unit) {
    private val PRIMARY_COLORS_COUNT = 19
    private val DEFAULT_PRIMARY_COLOR_INDEX = 7
    private val DEFAULT_SECONDARY_COLOR_INDEX = 5
    private val DEFAULT_COLOR_VALUE = activity.resources.getColor(R.color.colorPrimary)

    private var dialog: AlertDialog? = null
    private var view: View

    init {
        view = activity.layoutInflater.inflate(R.layout.dialog_line_color_picker, null).apply {
            hex_code.text = color.toHex()
            hex_code.setOnLongClickListener { activity.copyToClipboard(hex_code.value.substring(1)); true }
            val indexes = getColorIndexes(color)

            primary_line_color_picker.updateColors(getColors(R.array.md_primary_colors), indexes.first)
            primary_line_color_picker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondary_line_color_picker.updateColors(secondaryColors)
                    colorUpdated(secondary_line_color_picker.getCurrentColor())
                }
            }

            secondary_line_color_picker.updateColors(getColorsForIndex(indexes.first), indexes.second)
            secondary_line_color_picker.listener = object : LineColorPickerListener {
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
            activity.setupDialogStuff(view, this)
        }
    }

    fun getSpecificColor() = view.secondary_line_color_picker.getCurrentColor()

    private fun colorUpdated(color: Int) {
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        activity.updateActionbarColor(color)
        activity.setTheme(activity.getThemeId(color))
        view.hex_code.text = color.toHex()
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
        val color = view.secondary_line_color_picker.getCurrentColor()
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
}
