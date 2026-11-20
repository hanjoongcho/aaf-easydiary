package me.blog.korn123.easydiary.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit test suite for EditCursorUtils.
 */
class EditCursorUtilsTest {
    @Test
    fun `cursorWidthPx keeps the platform caret thickness on every density`() {
        assertEquals(2, EditCursorUtils.cursorWidthPx(1.0f))
        assertEquals(3, EditCursorUtils.cursorWidthPx(1.5f))
        assertEquals(4, EditCursorUtils.cursorWidthPx(2.0f))
        assertEquals(6, EditCursorUtils.cursorWidthPx(2.75f))
        assertEquals(8, EditCursorUtils.cursorWidthPx(4.0f))
    }

    @Test
    fun `cursorWidthPx rounds a fractional density to the nearest pixel`() {
        // 2 * 0.75 = 1.5 px and 2 * 2.625 = 5.25 px
        assertEquals(2, EditCursorUtils.cursorWidthPx(0.75f))
        assertEquals(5, EditCursorUtils.cursorWidthPx(2.625f))
    }

    @Test
    fun `cursorWidthPx never returns a width thinner than one pixel`() {
        assertEquals(1, EditCursorUtils.cursorWidthPx(0.0f))
        assertEquals(1, EditCursorUtils.cursorWidthPx(0.1f))
        assertEquals(1, EditCursorUtils.cursorWidthPx(-1.0f))
    }
}
