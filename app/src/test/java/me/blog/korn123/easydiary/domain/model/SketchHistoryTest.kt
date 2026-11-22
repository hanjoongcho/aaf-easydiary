package me.blog.korn123.easydiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for the sketchpad undo/redo history (issue #260).
 */
class SketchHistoryTest {
    private fun stroke(x: Float = 0f) = SketchStroke(
        colorArgb = SketchPalette.BLACK,
        widthDp = SketchStrokeWidths.MEDIUM_DP,
        points = listOf(SketchPoint(x, x)),
    )

    @Test
    fun `a new history is empty and offers no undo or redo`() {
        val history = SketchHistory()

        assertTrue(history.strokes.isEmpty())
        assertFalse(history.hasContent)
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun `an added stroke becomes the content of the drawing`() {
        val history = SketchHistory()
        val firstStroke = stroke(1f)

        history.addStroke(firstStroke)

        assertEquals(listOf(firstStroke), history.strokes)
        assertTrue(history.hasContent)
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun `undo removes the last stroke and redo puts it back`() {
        val history = SketchHistory()
        val firstStroke = stroke(1f)
        val secondStroke = stroke(2f)
        history.addStroke(firstStroke)
        history.addStroke(secondStroke)

        assertTrue(history.undo())
        assertEquals(listOf(firstStroke), history.strokes)
        assertTrue(history.canRedo)

        assertTrue(history.redo())
        assertEquals(listOf(firstStroke, secondStroke), history.strokes)
        assertFalse(history.canRedo)
    }

    @Test
    fun `undo and redo do nothing at the ends of the history`() {
        val history = SketchHistory()

        assertFalse(history.undo())
        assertFalse(history.redo())

        history.addStroke(stroke())
        assertFalse(history.redo())

        history.undo()
        assertFalse(history.undo())
    }

    @Test
    fun `a stroke drawn after an undo drops the undone steps`() {
        val history = SketchHistory()
        history.addStroke(stroke(1f))
        history.addStroke(stroke(2f))
        history.undo()

        history.addStroke(stroke(3f))

        assertFalse(history.canRedo)
        assertEquals(listOf(1f, 3f), history.strokes.map { it.points.first().x })
    }

    @Test
    fun `clearing the canvas can be undone`() {
        val history = SketchHistory()
        history.addStroke(stroke(1f))
        history.addStroke(stroke(2f))

        assertTrue(history.clear())
        assertTrue(history.strokes.isEmpty())
        assertFalse(history.hasContent)

        assertTrue(history.undo())
        assertEquals(listOf(1f, 2f), history.strokes.map { it.points.first().x })
    }

    @Test
    fun `clearing an empty canvas is not a step of the history`() {
        val history = SketchHistory()

        assertFalse(history.clear())
        assertFalse(history.canUndo)
    }

    @Test
    fun `eraser strokes take part in the history like any other stroke`() {
        val history = SketchHistory()
        val eraserStroke = stroke(1f).copy(isEraser = true)
        history.addStroke(stroke(2f))
        history.addStroke(eraserStroke)

        history.undo()

        assertEquals(1, history.strokes.size)
        assertFalse(history.strokes.first().isEraser)

        history.redo()

        assertEquals(2, history.strokes.size)
        assertTrue(history.strokes.last().isEraser)
    }

    @Test
    fun `the history keeps only the last steps`() {
        val history = SketchHistory(maxStepCount = 2)
        history.addStroke(stroke(1f))
        history.addStroke(stroke(2f))
        history.addStroke(stroke(3f))

        assertTrue(history.undo())
        assertTrue(history.undo())
        assertFalse(history.undo())
        assertEquals(listOf(1f), history.strokes.map { it.points.first().x })
    }

    @Test
    fun `the drawing is a copy that a later change does not alter`() {
        val history = SketchHistory()
        history.addStroke(stroke(1f))
        val previousDrawing = history.strokes

        history.addStroke(stroke(2f))

        assertEquals(1, previousDrawing.size)
        assertEquals(2, history.strokes.size)
    }
}
