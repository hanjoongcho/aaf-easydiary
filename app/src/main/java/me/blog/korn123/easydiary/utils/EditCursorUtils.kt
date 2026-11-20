package me.blog.korn123.easydiary.utils

import kotlin.math.roundToInt

/**
 * Geometry of the insertion cursor (caret) drawn on the diary editing screen.
 *
 * Easy Diary paints the caret of the editing screen with the text color so that it stays visible
 * on every customized theme (#156, #224). The caret is drawn as a rectangle whose thickness is
 * [CURSOR_WIDTH_DP] dp, the same thickness as the platform caret, and whose height follows the
 * line of the caret. Only the thickness has to be computed, the platform stretches the caret to the
 * line height.
 */
object EditCursorUtils {
    /** Caret thickness in dp, kept equal to the thickness of the platform caret. */
    const val CURSOR_WIDTH_DP = 2

    /**
     * Returns the caret thickness in pixels for the given screen [density].
     *
     * The result never falls below one pixel, otherwise the caret would be invisible on a low
     * density screen.
     */
    fun cursorWidthPx(density: Float): Int =
        (CURSOR_WIDTH_DP * density)
            .roundToInt()
            .coerceAtLeast(1)
}
