package me.blog.korn123.easydiary.presentation.sketchpad

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.extensions.getUriForFile
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.sketchpad.components.SketchpadScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.views.SketchpadView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * The built-in sketchpad (issue #260).
 *
 * It opens from the bottom toolbar of the diary editor, draws offline and returns the sketch as a PNG
 * content URI in the result data, which the editor feeds into its existing photo attachment pipeline.
 */
@AndroidEntryPoint
class SketchpadActivity : EasyDiaryComposeBaseActivity() {
    private val viewModel: SketchpadViewModel by viewModels()

    /**
     * The view that shows the canvas while this screen is up. Saving renders the sketch from it, so it is
     * kept here instead of being reached through the Compose tree.
     */
    private var sketchpadView: SketchpadView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleClose()
                }
            },
        )

        setContent {
            AppTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                SketchpadScreen(
                    uiState = uiState,
                    onSketchpadViewCreated = { sketchpadView = it },
                    onStrokeCommitted = viewModel::commitStroke,
                    onColorSelected = viewModel::selectColor,
                    onWidthSelected = viewModel::selectWidth,
                    onEraserToggled = viewModel::toggleEraser,
                    onUndoClick = viewModel::undo,
                    onRedoClick = viewModel::redo,
                    onClearClick = viewModel::requestClear,
                    onConfirmClear = viewModel::confirmClear,
                    onDismissAlert = viewModel::dismissAlert,
                    onDiscardConfirmed = { finishWithResult(RESULT_CANCELED, null) },
                    onSaveClick = { saveSketch() },
                    onCloseClick = { handleClose() },
                )
            }
        }
    }

    /**
     * A drawing that has not been saved yet asks before it is thrown away; an untouched canvas just closes.
     */
    private fun handleClose() {
        if (viewModel.uiState.value.hasContent) {
            viewModel.requestDiscard()
        } else {
            finishWithResult(RESULT_CANCELED, null)
        }
    }

    private fun finishWithResult(
        resultCode: Int,
        resultData: Intent?,
    ) {
        viewModel.dismissAlert()
        setResult(resultCode, resultData)
        finishActivityWithPauseLock()
    }

    private fun saveSketch() {
        if (viewModel.uiState.value.isSaving) return
        val bitmap = sketchpadView?.exportBitmap()
        if (bitmap == null) {
            viewModel.notifySaveFailed()
            return
        }

        viewModel.setSaving(true)
        lifecycleScope.launch {
            val sketchUri = withContext(Dispatchers.IO) { writeSketch(bitmap) }
            when (sketchUri) {
                null -> viewModel.notifySaveFailed()
                else -> finishWithResult(RESULT_OK, Intent().setData(sketchUri))
            }
        }
    }

    /**
     * Writes the drawing as a PNG file into the sketch cache and returns the content URI the diary editor
     * reads it from. The off-screen bitmap is recycled here as soon as the file is written, so the full
     * size copy does not stay in memory while the editor attaches the sketch.
     */
    private fun writeSketch(bitmap: Bitmap): Uri? =
        try {
            val sketchDirectory = File(cacheDir, SKETCH_CACHE_DIRECTORY)
            if (sketchDirectory.exists().not()) sketchDirectory.mkdirs()
            removeStaleSketches(sketchDirectory)
            val sketchFile = File(sketchDirectory, "$SKETCH_FILE_PREFIX${UUID.randomUUID()}$SKETCH_FILE_EXTENSION")
            FileOutputStream(sketchFile).use { output ->
                if (bitmap.compress(Bitmap.CompressFormat.PNG, PNG_COMPRESS_QUALITY, output).not()) {
                    throw IOException("The sketch could not be compressed to PNG.")
                }
            }
            getUriForFile(sketchFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            bitmap.recycle()
        }

    /**
     * Drops sketches an earlier save left behind, so the cache does not keep one file per sketch. A file
     * young enough to still be read by the editor that asked for it is kept.
     */
    private fun removeStaleSketches(sketchDirectory: File) {
        val expireTimeMillis = System.currentTimeMillis() - STALE_SKETCH_MILLIS
        sketchDirectory.listFiles()?.forEach { file ->
            if (file.lastModified() < expireTimeMillis) file.delete()
        }
    }

    companion object {
        private const val SKETCH_CACHE_DIRECTORY = "sketchpad"
        private const val SKETCH_FILE_PREFIX = "sketch_"
        private const val SKETCH_FILE_EXTENSION = ".png"
        private const val PNG_COMPRESS_QUALITY = 100
        private const val STALE_SKETCH_MILLIS = 60 * 60 * 1000L
    }
}
