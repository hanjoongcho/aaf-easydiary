package me.blog.korn123.easydiary.domain.model

/**
 * Undo/redo history of a sketchpad drawing.
 *
 * Every change (a new stroke or a cleared canvas) pushes the drawing as it was before the change, so an
 * undo restores exactly what the user saw and a redo puts the change back. Drawing after an undo drops the
 * redo steps, the way an editor behaves. Only the last [maxStepCount] steps are kept so a long drawing does
 * not grow the memory of the screen without bound.
 *
 * The class holds no Android dependency, so the history behaviour is covered by JVM unit tests.
 */
class SketchHistory(private val maxStepCount: Int = DEFAULT_MAX_STEP_COUNT) {
    private val undoStack = ArrayDeque<List<SketchStroke>>()
    private val redoStack = ArrayDeque<List<SketchStroke>>()

    /**
     * The strokes of the drawing right now, oldest first.
     */
    var strokes: List<SketchStroke> = emptyList()
        private set

    val canUndo: Boolean get() = undoStack.isNotEmpty()

    val canRedo: Boolean get() = redoStack.isNotEmpty()

    val hasContent: Boolean get() = strokes.isNotEmpty()

    /**
     * Adds a finished stroke to the drawing as an undoable step.
     */
    fun addStroke(stroke: SketchStroke) {
        recordStep()
        strokes = strokes + stroke
    }

    /**
     * Removes every stroke as an undoable step, so 'Clear all' can be taken back.
     *
     * @return true when there was something to clear
     */
    fun clear(): Boolean {
        if (strokes.isEmpty()) return false
        recordStep()
        strokes = emptyList()
        return true
    }

    /**
     * @return true when a step was taken back
     */
    fun undo(): Boolean {
        val previous = undoStack.removeLastOrNull() ?: return false
        redoStack.addLast(strokes)
        strokes = previous
        return true
    }

    /**
     * @return true when an undone step was put back
     */
    fun redo(): Boolean {
        val next = redoStack.removeLastOrNull() ?: return false
        pushUndoStep(strokes)
        strokes = next
        return true
    }

    private fun recordStep() {
        pushUndoStep(strokes)
        redoStack.clear()
    }

    private fun pushUndoStep(previousStrokes: List<SketchStroke>) {
        undoStack.addLast(previousStrokes)
        while (undoStack.size > maxStepCount) undoStack.removeFirst()
    }

    companion object {
        const val DEFAULT_MAX_STEP_COUNT = 100
    }
}
