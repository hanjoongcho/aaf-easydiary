package me.blog.korn123.easydiary.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.simplemobiletools.commons.extensions.baseConfig
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.Config
import me.blog.korn123.easydiary.helper.DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE

/**
 * Created by CHO HANJOONG on 2018-02-06.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

val Context.config: Config get() = Config.newInstance(this)

fun Context.updateCardViewPolicy(viewGroup: ViewGroup) {
    val cnt = viewGroup.childCount
    (0 until cnt)
            .map { viewGroup.getChildAt(it) }
            .forEach {
                when (it) {
                    is androidx.cardview.widget.CardView -> {
                        if (config.enableCardViewPolicy) {
                            it.useCompatPadding = true
                            it.cardElevation = CommonUtils.dpToPixelFloatValue(this, 2F)
                        } else {
                            it.useCompatPadding = false
                            it.cardElevation = 0F
                        }
                    }
                    is ViewGroup -> updateCardViewPolicy(it)
                }
            }
}

fun Context.updateTextSize(viewGroup: ViewGroup, context: Context, addSize: Int) {
    val cnt = viewGroup.childCount
    val settingFontSize: Float = config.settingFontSize + addSize
    (0 until cnt)
            .map { index -> viewGroup.getChildAt(index) }
            .forEach {
                when (it) {
                    is TextView -> {
                        it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
                    }
                    is ViewGroup -> updateTextSize(it, context, addSize)
                }
            }
}

fun Context.initTextSize(viewGroup: ViewGroup, context: Context) {
    val cnt = viewGroup.childCount
    val defaultFontSize: Float = CommonUtils.dpToPixelFloatValue(context, DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE.toFloat())
    val settingFontSize: Float = config.settingFontSize
    (0 until cnt)
            .map { index -> viewGroup.getChildAt(index) }
            .forEach {
                when (it) {
                    is TextView -> { 
                        when (it.id) {
                            R.id.diaryCount -> it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize * 0.7F)
                            R.id.symbolTextArrow -> {}
                            else -> it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
                        }
                    }
                    is ViewGroup -> initTextSize(it, context)
                }
            }
}

fun Context.initTextSize(textView: TextView, context: Context) {
    val defaultFontSize: Float = CommonUtils.dpToPixelFloatValue(context, DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE.toFloat())
    val settingFontSize: Float = config.settingFontSize
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
}

fun Context.checkPermission(permissions: Array<String>): Boolean {
    val listDeniedPermissions: List<String> = permissions.filter { permission -> 
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
    }
    return listDeniedPermissions.isEmpty()
}

fun Context.makeSnackBar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
}

fun Context.showAlertDialog(message: String, positiveListener: DialogInterface.OnClickListener, negativeListener: DialogInterface.OnClickListener?) {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(true)
    builder.setNegativeButton(getString(R.string.cancel), negativeListener)
    builder.setPositiveButton(getString(R.string.ok), positiveListener)
    val alert = builder.create()
    alert.show()
}

fun Context.showAlertDialog(message: String, positiveListener: DialogInterface.OnClickListener?, cancelable: Boolean = true) {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(cancelable)
    builder.setPositiveButton(getString(R.string.ok), positiveListener)
    val alert = builder.create()
    alert.show()
}

fun Context.showAlertDialog(title: String, message: String, positiveListener: DialogInterface.OnClickListener?) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.ok), positiveListener)
    val alert = builder.create()
    alert.show()
}

fun Context.preferencesContains(key: String): Boolean {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    return preferences.contains(key)
}