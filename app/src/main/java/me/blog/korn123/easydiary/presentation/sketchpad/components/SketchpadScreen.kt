package me.blog.korn123.easydiary.presentation.sketchpad.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.SketchPalette
import me.blog.korn123.easydiary.domain.model.SketchStroke
import me.blog.korn123.easydiary.domain.model.SketchStrokeWidths
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.sketchpad.SketchpadAlert
import me.blog.korn123.easydiary.presentation.sketchpad.SketchpadUiState
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.views.SketchpadView

/**
 * The built-in sketchpad screen (issue #260): a white drawing canvas with a pen palette, a width selector,
 * an eraser, undo/redo and 'Clear all', and a save action that hands the drawing back to the diary editor.
 */
@Composable
fun SketchpadScreen(
    uiState: SketchpadUiState,
    onSketchpadViewCreated: (SketchpadView) -> Unit,
    onStrokeCommitted: (SketchStroke) -> Unit,
    onColorSelected: (Int) -> Unit,
    onWidthSelected: (Float) -> Unit,
    onEraserToggled: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onClearClick: () -> Unit,
    onConfirmClear: () -> Unit,
    onDismissAlert: () -> Unit,
    onDiscardConfirmed: () -> Unit,
    onSaveClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val primaryColor = Color(context.config.primaryColor)
    val textColor = Color(context.config.textColor)
    val canvasShape = RoundedCornerShape(4.dp)

    Scaffold(
        topBar = {
            EasyDiaryActionBar(
                title = stringResource(id = R.string.sketchpad),
                useCloseIcon = true,
                actions = {
                    when (uiState.isSaving) {
                        true ->
                            CircularProgressIndicator(
                                modifier =
                                    Modifier
                                        .padding(end = 16.dp)
                                        .size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )

                        false ->
                            IconButton(
                                onClick = onSaveClick,
                                enabled = uiState.hasContent,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_save_w),
                                    contentDescription = stringResource(id = R.string.save),
                                    tint =
                                        when (uiState.hasContent) {
                                            true -> Color.White
                                            false -> Color.White.copy(alpha = SKETCH_DISABLED_ALPHA)
                                        },
                                )
                            }
                    }
                },
                close = onCloseClick,
            )
        },
        containerColor = Color(context.config.backgroundColor),
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .clip(canvasShape)
                        .background(Color.White),
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { viewContext ->
                        SketchpadView(viewContext).also { onSketchpadViewCreated(it) }
                    },
                    update = { view ->
                        view.onStrokeCommitted = onStrokeCommitted
                        view.penColorArgb = uiState.penColorArgb
                        view.strokeWidthDp = uiState.strokeWidthDp
                        view.isEraser = uiState.isEraser
                        view.submitStrokes(uiState.strokes)
                    },
                )
            }

            SketchpadToolbar(
                uiState = uiState,
                primaryColor = primaryColor,
                textColor = textColor,
                onColorSelected = onColorSelected,
                onWidthSelected = onWidthSelected,
                onEraserToggled = onEraserToggled,
                onUndoClick = onUndoClick,
                onRedoClick = onRedoClick,
                onClearClick = onClearClick,
            )
        }
    }

    when (uiState.alert) {
        SketchpadAlert.CLEAR_CANVAS_CONFIRM ->
            SketchpadConfirmDialog(
                message = stringResource(id = R.string.sketchpad_clear_confirm),
                onConfirm = onConfirmClear,
                onDismiss = onDismissAlert,
            )

        SketchpadAlert.DISCARD_CONFIRM ->
            SketchpadConfirmDialog(
                message = stringResource(id = R.string.sketchpad_discard_confirm),
                onConfirm = onDiscardConfirmed,
                onDismiss = onDismissAlert,
            )

        SketchpadAlert.SAVE_FAILED ->
            SketchpadMessageDialog(
                message = stringResource(id = R.string.sketchpad_save_failed),
                onDismiss = onDismissAlert,
            )

        null -> {}
    }
}

@Composable
private fun SketchpadToolbar(
    uiState: SketchpadUiState,
    primaryColor: Color,
    textColor: Color,
    onColorSelected: (Int) -> Unit,
    onWidthSelected: (Float) -> Unit,
    onEraserToggled: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SketchColorPalette(
                selectedColorArgb = uiState.penColorArgb,
                isPenActive = uiState.isEraser.not(),
                primaryColor = primaryColor,
                onColorSelected = onColorSelected,
            )
            Spacer(modifier = Modifier.weight(1f))
            SketchEraserToggle(
                isEraser = uiState.isEraser,
                primaryColor = primaryColor,
                onEraserToggled = onEraserToggled,
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SketchStrokeWidthSelector(
                selectedWidthDp = uiState.strokeWidthDp,
                textColor = textColor,
                primaryColor = primaryColor,
                onWidthSelected = onWidthSelected,
            )
            Spacer(modifier = Modifier.weight(1f))
            SketchHistoryActions(
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                canClear = uiState.hasContent,
                textColor = textColor,
                onUndoClick = onUndoClick,
                onRedoClick = onRedoClick,
                onClearClick = onClearClick,
            )
        }
    }
}

@Composable
private fun SketchColorPalette(
    selectedColorArgb: Int,
    isPenActive: Boolean,
    primaryColor: Color,
    onColorSelected: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        SketchPalette.ALL.forEach { colorArgb ->
            val label = stringResource(id = sketchColorLabel(colorArgb))
            val isSelected = isPenActive && selectedColorArgb == colorArgb
            Box(
                modifier =
                    Modifier
                        .size(SKETCH_COLOR_TOUCH_SIZE)
                        .clip(CircleShape)
                        .clickable { onColorSelected(colorArgb) }
                        .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(SKETCH_COLOR_DOT_SIZE)
                            .clip(CircleShape)
                            .background(Color(colorArgb))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) primaryColor else SKETCH_UNSELECTED_COLOR,
                                shape = CircleShape,
                            ),
                )
            }
        }
    }
}

@Composable
private fun SketchStrokeWidthSelector(
    selectedWidthDp: Float,
    textColor: Color,
    primaryColor: Color,
    onWidthSelected: (Float) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        SketchStrokeWidths.ALL.forEach { widthDp ->
            val label = stringResource(id = sketchWidthLabel(widthDp))
            val isSelected = selectedWidthDp == widthDp
            Box(
                modifier =
                    Modifier
                        .size(SKETCH_WIDTH_TOUCH_SIZE)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.border(width = 2.dp, color = primaryColor, shape = CircleShape)
                            } else {
                                Modifier
                            },
                        )
                        .clickable { onWidthSelected(widthDp) }
                        .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(SKETCH_WIDTH_LINE_WIDTH)
                            .height(widthDp.dp)
                            .clip(CircleShape)
                            .background(textColor),
                )
            }
        }
    }
}

@Composable
private fun SketchEraserToggle(
    isEraser: Boolean,
    primaryColor: Color,
    onEraserToggled: () -> Unit,
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier =
            Modifier
                .clip(shape)
                .border(width = 1.dp, color = primaryColor, shape = shape)
                .background(if (isEraser) primaryColor else Color.Transparent)
                .clickable { onEraserToggled() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = stringResource(id = R.string.sketchpad_eraser),
            style = MaterialTheme.typography.labelLarge,
            color = if (isEraser) Color.White else primaryColor,
        )
    }
}

@Composable
private fun SketchHistoryActions(
    canUndo: Boolean,
    canRedo: Boolean,
    canClear: Boolean,
    textColor: Color,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        IconButton(
            onClick = onUndoClick,
            enabled = canUndo,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_aaf_undo),
                contentDescription = stringResource(id = R.string.sketchpad_undo),
                tint = sketchActionTint(textColor, canUndo),
            )
        }
        IconButton(
            onClick = onRedoClick,
            enabled = canRedo,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_aaf_redo),
                contentDescription = stringResource(id = R.string.sketchpad_redo),
                tint = sketchActionTint(textColor, canRedo),
            )
        }
        IconButton(
            onClick = onClearClick,
            enabled = canClear,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete_w),
                contentDescription = stringResource(id = R.string.sketchpad_clear),
                tint = sketchActionTint(textColor, canClear),
            )
        }
    }
}

@Composable
private fun SketchpadConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
    )
}

@Composable
private fun SketchpadMessageDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
    )
}

private fun sketchActionTint(
    textColor: Color,
    isEnabled: Boolean,
): Color = if (isEnabled) textColor else textColor.copy(alpha = SKETCH_DISABLED_ALPHA)

@StringRes
private fun sketchColorLabel(colorArgb: Int): Int =
    when (colorArgb) {
        SketchPalette.BLACK -> R.string.sketchpad_color_black
        SketchPalette.RED -> R.string.sketchpad_color_red
        SketchPalette.BLUE -> R.string.sketchpad_color_blue
        SketchPalette.GREEN -> R.string.sketchpad_color_green
        SketchPalette.ORANGE -> R.string.sketchpad_color_orange
        else -> R.string.sketchpad_color_purple
    }

@StringRes
private fun sketchWidthLabel(widthDp: Float): Int =
    when (widthDp) {
        SketchStrokeWidths.THIN_DP -> R.string.sketchpad_width_thin
        SketchStrokeWidths.THICK_DP -> R.string.sketchpad_width_thick
        else -> R.string.sketchpad_width_medium
    }

private val SKETCH_COLOR_TOUCH_SIZE = 36.dp
private val SKETCH_COLOR_DOT_SIZE = 26.dp
private val SKETCH_WIDTH_TOUCH_SIZE = 36.dp
private val SKETCH_WIDTH_LINE_WIDTH = 20.dp
private val SKETCH_UNSELECTED_COLOR = Color(0xFFBDBDBD)
private const val SKETCH_DISABLED_ALPHA = 0.38f

@Preview(showBackground = true)
@Composable
private fun SketchpadToolbarPreview() {
    AppTheme {
        SketchpadToolbar(
            uiState =
                SketchpadUiState(
                    penColorArgb = SketchPalette.RED,
                    strokeWidthDp = SketchStrokeWidths.MEDIUM_DP,
                    hasContent = true,
                    canUndo = true,
                ),
            primaryColor = Color(SketchPalette.RED),
            textColor = Color(SketchPalette.BLACK),
            onColorSelected = {},
            onWidthSelected = {},
            onEraserToggled = {},
            onUndoClick = {},
            onRedoClick = {},
            onClearClick = {},
        )
    }
}
