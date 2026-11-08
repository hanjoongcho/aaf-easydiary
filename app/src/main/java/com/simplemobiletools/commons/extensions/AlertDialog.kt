package com.simplemobiletools.commons.extensions

import androidx.appcompat.app.AlertDialog
import android.view.WindowManager
import android.widget.EditText

fun AlertDialog.showKeyboard(editText: EditText) {
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    editText.apply {
        onGlobalLayout {
            setSelection(text.toString().length)
        }
    }
}

fun AlertDialog.hideKeyboard() {
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}
