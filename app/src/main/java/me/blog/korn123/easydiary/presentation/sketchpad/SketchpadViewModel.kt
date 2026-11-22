package me.blog.korn123.easydiary.presentation.sketchpad

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.blog.korn123.easydiary.domain.model.SketchHistory
import me.blog.korn123.easydiary.domain.model.SketchPalette
import me.blog.korn123.easydiary.domain.model.SketchStroke
import me.blog.korn123.easydiary.domain.model.SketchStrokeWidths
import javax.inject.Inject

/**
 * The confirmation or notice the sketchpad asks for. The screen turns each one into the dialog it shows.
 */
enum class SketchpadAlert {
    CLEAR_CANVAS_CONFIRM,
    DISCARD_CONFIRM,
    SAVE_FAILED,
}

data class SketchpadUiState(
    val strokes: List<SketchStroke> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val hasContent: Boolean = false,
    val penColorArgb: Int = SketchPalette.DEFAULT_COLOR_ARGB,
    val strokeWidthDp: Float = SketchStrokeWidths.DEFAULT_DP,
    val isEraser: Boolean = false,
    val isSaving: Boolean = false,
    val alert: SketchpadAlert? = null,
)

/**
 * Keeps the drawing of the sketchpad (issue #260) while the screen lives: the strokes and their undo/redo
 * history, the pen settings the user picked and the dialog that is waiting for an answer. Holding the
 * drawing here is what lets a configuration change repaint what the user drew instead of starting over.
 */
@HiltViewModel
class SketchpadViewModel @Inject constructor() : ViewModel() {
    private val history = SketchHistory()

    private val _uiState = MutableStateFlow(SketchpadUiState())
    val uiState: StateFlow<SketchpadUiState> = _uiState.asStateFlow()

    fun commitStroke(stroke: SketchStroke) {
        history.addStroke(stroke)
        publishDrawing()
    }

    fun selectColor(colorArgb: Int) {
        _uiState.update { it.copy(penColorArgb = colorArgb, isEraser = false) }
    }

    fun selectWidth(widthDp: Float) {
        _uiState.update { it.copy(strokeWidthDp = widthDp) }
    }

    fun toggleEraser() {
        _uiState.update { it.copy(isEraser = it.isEraser.not()) }
    }

    fun undo() {
        if (history.undo()) publishDrawing()
    }

    fun redo() {
        if (history.redo()) publishDrawing()
    }

    fun requestClear() {
        if (_uiState.value.hasContent.not()) return
        _uiState.update { it.copy(alert = SketchpadAlert.CLEAR_CANVAS_CONFIRM) }
    }

    fun confirmClear() {
        history.clear()
        _uiState.update { it.copy(alert = null) }
        publishDrawing()
    }

    fun requestDiscard() {
        _uiState.update { it.copy(alert = SketchpadAlert.DISCARD_CONFIRM) }
    }

    fun dismissAlert() {
        _uiState.update { it.copy(alert = null) }
    }

    fun setSaving(isSaving: Boolean) {
        _uiState.update { it.copy(isSaving = isSaving) }
    }

    fun notifySaveFailed() {
        _uiState.update { it.copy(isSaving = false, alert = SketchpadAlert.SAVE_FAILED) }
    }

    private fun publishDrawing() {
        _uiState.update {
            it.copy(
                strokes = history.strokes,
                canUndo = history.canUndo,
                canRedo = history.canRedo,
                hasContent = history.hasContent,
            )
        }
    }
}
