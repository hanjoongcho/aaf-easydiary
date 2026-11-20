package me.blog.korn123.easydiary.helper

import android.view.inputmethod.EditorInfo
import android.widget.EditText

/**
 * Keyboard incognito mode support for issue #177 (Add option to force keyboard into incognito mode).
 *
 * Most keyboards (Gboard, SwiftKey, ...) learn the words a user types and use them for personalized
 * suggestions. Because those suggestions can reproduce sentences written before, diary contents typed
 * into the diary editor may be surfaced while chatting with someone else.
 *
 * [EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING] asks the current IME not to learn from the text of the
 * field and not to personalize its suggestions for it. The flag was added in API 26 (Android 8.0), which
 * is the minSdk of this project, so no version branching is needed.
 */
object KeyboardIncognitoHelper {
    /**
     * Returns [imeOptions] with [EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING] turned on when [enabled] is true
     * and turned off when it is false, so the policy can be re-applied whenever the setting changes.
     */
    fun resolveImeOptions(imeOptions: Int, enabled: Boolean): Int =
        when (enabled) {
            true -> imeOptions or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            false -> imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING.inv()
        }

    /**
     * Applies the keyboard incognito policy to the given [editText].
     * Nothing is changed when the current imeOptions already match the policy, so calling this method
     * repeatedly does not restart the input connection of the IME unnecessarily.
     */
    fun applyPolicy(editText: EditText, enabled: Boolean) {
        val imeOptions = resolveImeOptions(editText.imeOptions, enabled)
        if (editText.imeOptions != imeOptions) {
            editText.imeOptions = imeOptions
        }
    }
}
