package me.blog.korn123.easydiary.domain.model

/**
 * A single point of a freehand stroke, in the pixel coordinates of the canvas it was drawn on.
 */
data class SketchPoint(
    val x: Float,
    val y: Float,
)

/**
 * One continuous touch of the sketchpad: what the user drew from putting a finger down to lifting it.
 * A stroke keeps every value it was drawn with, so replaying the history reproduces the drawing exactly
 * and an undo of a later stroke never changes how an earlier one looks.
 *
 * @property colorArgb the pen color of the stroke
 * @property widthDp the pen width in dp, the unit the sketchpad stores so a drawing keeps its look on any density
 * @property isEraser whether the stroke was drawn with the eraser, which clears with the canvas color instead of painting
 * @property points the touched points in draw order
 */
data class SketchStroke(
    val colorArgb: Int,
    val widthDp: Float,
    val points: List<SketchPoint>,
    val isEraser: Boolean = false,
)

/**
 * The pen widths the sketchpad offers: thin, medium and thick.
 */
object SketchStrokeWidths {
    const val THIN_DP = 2f
    const val MEDIUM_DP = 4f
    const val THICK_DP = 8f
    const val DEFAULT_DP = MEDIUM_DP

    val ALL = listOf(THIN_DP, MEDIUM_DP, THICK_DP)
}

/**
 * The pen colors the sketchpad offers as presets.
 */
object SketchPalette {
    const val BLACK = 0xFF000000.toInt()
    const val RED = 0xFFE53935.toInt()
    const val BLUE = 0xFF1E88E5.toInt()
    const val GREEN = 0xFF43A047.toInt()
    const val ORANGE = 0xFFFB8C00.toInt()
    const val PURPLE = 0xFF8E24AA.toInt()

    const val DEFAULT_COLOR_ARGB = BLACK

    val ALL = listOf(BLACK, RED, BLUE, GREEN, ORANGE, PURPLE)
}
