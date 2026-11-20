package me.blog.korn123.easydiary.extensions

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.widget.EditText
import me.blog.korn123.easydiary.utils.EditCursorUtils

/**
 * Paints the insertion cursor of this [EditText] with [color].
 *
 * The editing screen layouts declare `android:textCursorDrawable="@null"`, which makes the platform
 * draw the caret with the text color on Android 8.0 ~ 9.0 (API 26 ~ 28). From Android 10 (API 29)
 * a custom caret is set explicitly so that the color is confirmed on devices whose theme or
 * manufacturer skin substitutes the caret with the primary color (#156, #224).
 *
 * The drawable only declares an intrinsic width, the height is left to the platform which fits
 * every caret to the line height. Do nothing below API 29 where [EditText.setTextCursorDrawable] is
 * not public; the xml attribute covers these versions.
 */
fun EditText.applyTextCursorColor(color: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

    val currentCursor = textCursorDrawable
    if (currentCursor is GradientDrawable) {
        // The editor keeps the first cursor drawable it has loaded and does not load a replacement,
        // so an already applied caret is recolored in place instead of being assigned a new one.
        currentCursor.mutate().setColor(color)
        return
    }

    val cursorWidth = EditCursorUtils.cursorWidthPx(resources.displayMetrics.density)
    textCursorDrawable =
        GradientDrawable().apply {
            setColor(color)
            setSize(cursorWidth, -1)
        }
}
