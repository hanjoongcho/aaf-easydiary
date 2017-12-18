package com.simplemobiletools.commons.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.simplemobiletools.commons.views.MyTextView
import me.blog.korn123.easydiary.R

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

fun Activity.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    if (isOnMainThread()) {
        showToast(this, id, length)
    } else {
        runOnUiThread {
            showToast(this, id, length)
        }
    }
}

private fun showToast(activity: Activity, messageId: Int, length: Int) {
    if (!activity.isActivityDestroyed()) {
        Toast.makeText(activity, messageId, length).show()
    }
}

fun Activity.isActivityDestroyed() = isJellyBean1Plus() && isDestroyed

fun Activity.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText(getString(R.string.simple_commons), text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip = clip
    toast(R.string.value_copied_to_clipboard)
}

fun Activity.setupDialogStuff(view: View, dialog: AlertDialog, titleId: Int = 0, callback: (() -> Unit)? = null) {
//    if (isActivityDestroyed()) {
//        return
//    }
//
//    if (view is ViewGroup)
//        updateTextColors(view)
//    else if (view is MyTextView) {
//        view.setTextColor(baseConfig.textColor)
//    }
//
//    var title: TextView? = null
//    if (titleId != 0) {
//        title = layoutInflater.inflate(R.layout.dialog_title, null) as TextView
//        title.dialog_title_textview.apply {
//            setText(titleId)
//            setTextColor(baseConfig.textColor)
//        }
//    }
//
//    dialog.apply {
//        setView(view)
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        setCustomTitle(title)
//        setCanceledOnTouchOutside(true)
//        show()
//        getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(baseConfig.textColor)
//        getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(baseConfig.textColor)
//        getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(baseConfig.textColor)
//        window.setBackgroundDrawable(ColorDrawable(baseConfig.backgroundColor))
//    }
//    callback?.invoke()
}