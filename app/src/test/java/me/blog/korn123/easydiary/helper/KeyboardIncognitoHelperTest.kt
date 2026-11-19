package me.blog.korn123.easydiary.helper

import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the keyboard incognito policy added for issue #177.
 * The policy only manipulates imeOptions bits, so it can be verified without an Android device.
 */
class KeyboardIncognitoHelperTest {
    /** [EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING] (0x01000000), added in API level 26. */
    private val incognitoFlag = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING

    @Test
    fun `resolveImeOptions adds the no personalized learning flag when enabled`() {
        assertEquals(incognitoFlag, KeyboardIncognitoHelper.resolveImeOptions(0, true))
        assertTrue((KeyboardIncognitoHelper.resolveImeOptions(0, true) and incognitoFlag) != 0)
    }

    @Test
    fun `resolveImeOptions keeps the other imeOptions bits when enabled`() {
        val imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN
        val resolved = KeyboardIncognitoHelper.resolveImeOptions(imeOptions, true)

        assertEquals(imeOptions or incognitoFlag, resolved)
        assertEquals(EditorInfo.IME_ACTION_DONE, resolved and EditorInfo.IME_MASK_ACTION)
    }

    @Test
    fun `resolveImeOptions removes only the no personalized learning flag when disabled`() {
        val imeOptions = EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN or incognitoFlag
        val resolved = KeyboardIncognitoHelper.resolveImeOptions(imeOptions, false)

        assertEquals(0, resolved and incognitoFlag)
        assertEquals(EditorInfo.IME_ACTION_DONE, resolved and EditorInfo.IME_MASK_ACTION)
        assertTrue((resolved and EditorInfo.IME_FLAG_NO_FULLSCREEN) != 0)
    }

    @Test
    fun `resolveImeOptions is idempotent`() {
        val once = KeyboardIncognitoHelper.resolveImeOptions(0, true)
        val twice = KeyboardIncognitoHelper.resolveImeOptions(once, true)

        assertEquals(once, twice)
        assertEquals(0, KeyboardIncognitoHelper.resolveImeOptions(once, false))
    }
}
