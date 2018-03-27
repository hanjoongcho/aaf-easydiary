package me.blog.korn123.easydiary.extensions

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.easydiary.helper.Config
import me.blog.korn123.easydiary.helper.DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE

/**
 * Created by CHO HANJOONG on 2018-02-06.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

val Context.config: Config get() = Config.newInstance(this)

fun Context.initTextSize(viewGroup: ViewGroup, context: Context) {
    val cnt = viewGroup.childCount
    val defaultFontSize: Float = CommonUtils.dpToPixel(context, DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE).toFloat()
    val settingFontSize: Float = config.settingFontSize
    (0 until cnt)
            .map { viewGroup.getChildAt(it) }
            .forEach {
                when (it) {
                    is TextView -> it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
                    is ViewGroup -> initTextSize(it, context)
                }
            }
}

fun Context.initTextSize(textView: TextView, context: Context) {
    val defaultFontSize: Float = CommonUtils.dpToPixel(context, DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE).toFloat()
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

fun Context.showAlertDialog(message: String, positiveListener: DialogInterface.OnClickListener, negativeListener: DialogInterface.OnClickListener) {
    val builder = AlertDialog.Builder(this)
    //        builder.setIcon(R.drawable.ic_launcher);
    //        builder.setTitle("일기삭제");
    builder.setMessage(message)
    builder.setCancelable(true)
    builder.setNegativeButton("취소", negativeListener)
    builder.setPositiveButton("확인", positiveListener)
    val alert = builder.create()
    alert.show()
}

fun Context.showAlertDialog(message: String, positiveListener: DialogInterface.OnClickListener) {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(true)
    builder.setPositiveButton("확인", positiveListener)
    val alert = builder.create()
    alert.show()
}

fun Context.showAlertDialog(title: String, message: String, positiveListener: DialogInterface.OnClickListener) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    //        builder.setIcon(R.drawable.book);
    builder.setMessage(message)
    builder.setCancelable(true)
    builder.setPositiveButton("확인", positiveListener)
    val alert = builder.create()
    alert.show()
}

fun Context.preferencesContains(key: String): Boolean {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    return preferences.contains(key)
}