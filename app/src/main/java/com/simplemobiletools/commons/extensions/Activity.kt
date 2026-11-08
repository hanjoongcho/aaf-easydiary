package com.simplemobiletools.commons.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.TransactionTooLargeException
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.WhatsNewDialog
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.Release
import com.simplemobiletools.commons.models.SharedTheme
import com.simplemobiletools.commons.views.MyTextView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.DialogTitleBinding
import java.io.File
import java.util.*

fun Activity.toast(
    id: Int,
    length: Int = Toast.LENGTH_SHORT,
) {
    if (isOnMainThread()) {
        showToast(this, id, length)
    } else {
        runOnUiThread {
            showToast(this, id, length)
        }
    }
}

fun Activity.toast(
    msg: String,
    length: Int = Toast.LENGTH_SHORT,
) {
    if (isOnMainThread()) {
        showToast(this, msg, length)
    } else {
        runOnUiThread {
            showToast(this, msg, length)
        }
    }
}

private fun showToast(
    activity: Activity,
    messageId: Int,
    length: Int,
) {
    if (!activity.isActivityDestroyed()) {
        Toast.makeText(activity.applicationContext, messageId, length).show()
    }
}

private fun showToast(
    activity: Activity,
    message: String,
    length: Int,
) {
    if (!activity.isActivityDestroyed()) {
        Toast.makeText(activity.applicationContext, message, length).show()
    }
}

fun Activity.showErrorToast(
    msg: String,
    length: Int = Toast.LENGTH_LONG,
) {
    toast(String.format(getString(R.string.an_error_occurred), msg), length)
}

fun Activity.showErrorToast(
    exception: Exception,
    length: Int = Toast.LENGTH_LONG,
) {
    showErrorToast(exception.toString(), length)
}

fun Activity.launchViewIntent(url: String) {
    Thread {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            if (resolveActivity(packageManager) != null) {
                startActivity(this)
            } else {
                toast(R.string.no_app_found)
            }
        }
    }.start()
}

fun Activity.tryGenericMimeType(
    intent: Intent,
    mimeType: String,
    uri: Uri,
): Boolean {
    var genericMimeType = mimeType.getGenericMimeType()
    if (genericMimeType.isEmpty()) {
        genericMimeType = "*/*"
    }

    intent.setDataAndType(uri, genericMimeType)
    return if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
        true
    } else {
        false
    }
}

fun BaseSimpleActivity.checkWhatsNew(
    releases: List<Release>,
    currVersion: Int,
) {
    if (baseConfig.lastVersion == 0) {
        baseConfig.lastVersion = currVersion
        return
    }

    val newReleases = arrayListOf<Release>()
    releases.filterTo(newReleases) { it.id > baseConfig.lastVersion }

    if (newReleases.isNotEmpty() && !baseConfig.avoidWhatsNew) {
        WhatsNewDialog(this, newReleases)
    }

    baseConfig.lastVersion = currVersion
}

private fun deleteRecursively(file: File): Boolean {
    if (file.isDirectory) {
        val files = file.listFiles() ?: return file.delete()
        for (child in files) {
            deleteRecursively(child)
        }
    }

    return file.delete()
}

fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun Activity.showKeyboard(et: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun BaseSimpleActivity.useEnglishToggled() {
    val conf = resources.configuration
    conf.locale = if (baseConfig.useEnglish) Locale.ENGLISH else Locale.getDefault()
    resources.updateConfiguration(conf, resources.displayMetrics)
    restartActivity()
}

fun BaseSimpleActivity.restartActivity() {
    finish()
    startActivity(intent)
}

fun Activity.isActivityDestroyed() = isJellyBean1Plus() && isDestroyed

fun Activity.updateSharedTheme(sharedTheme: SharedTheme): Int {
    try {
        val contentValues = MyContentProvider.fillThemeContentValues(sharedTheme)
        return applicationContext.contentResolver.update(MyContentProvider.CONTENT_URI, contentValues, null, null)
    } catch (e: Exception) {
        showErrorToast(e)
    }
    return 0
}

fun Activity.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText(getString(R.string.simple_commons), text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
    toast(R.string.value_copied_to_clipboard)
}

fun Activity.setupDialogStuff(
    view: View,
    dialog: AlertDialog,
    titleId: Int = 0,
    callback: (() -> Unit)? = null,
) {
    if (isActivityDestroyed()) {
        return
    }

    if (view is ViewGroup) {
        updateTextColors(view)
    } else if (view is MyTextView) {
        view.setColors(baseConfig.textColor, getAdjustedPrimaryColor(), baseConfig.backgroundColor)
    }

    dialog.apply {
        setView(view)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (titleId != 0) {
            val binding = DialogTitleBinding.inflate(layoutInflater)
            binding.dialogTitleTextview.apply {
                setText(titleId)
                setTextColor(baseConfig.textColor)
            }
            setCustomTitle(binding.dialogTitleTextview)
        }
        setCanceledOnTouchOutside(true)
        show()
        getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(baseConfig.textColor)
        getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(baseConfig.textColor)
        getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(baseConfig.textColor)
        window!!.setBackgroundDrawable(ColorDrawable(baseConfig.backgroundColor))
    }
    callback?.invoke()
}
