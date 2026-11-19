package me.blog.korn123.easydiary.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.DiaryPdfSnapshot
import java.io.File
import java.io.OutputStream

/**
 * Renders diary snapshots into a data driven, print friendly A4 PDF document.
 *
 * The document is composed from the snapshot values only (never from on-screen views), so the
 * result does not depend on the app theme or the device screen size, and the body text stays
 * real vector text inside the PDF.
 */
object DiaryPdfExporter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val PAGE_MARGIN = 48f
    private const val CONTENT_WIDTH = 499f
    private const val CONTENT_BOTTOM = 778f
    private const val FOOTER_BASELINE = 814f

    private const val BODY_TEXT_SIZE = 12f
    private const val BODY_LINE_SPACING_MULTIPLIER = 1.25f
    private const val TITLE_TEXT_SIZE = 17f
    private const val TITLE_LINE_SPACING_MULTIPLIER = 1.1f
    private const val META_TEXT_SIZE = 9.5f
    private const val FOOTER_TEXT_SIZE = 8f

    private const val META_TITLE_GAP = 8f
    private const val TITLE_DIVIDER_GAP = 10f
    private const val DIVIDER_THICKNESS = 0.8f
    private const val ENTRY_BODY_GAP = 14f
    private const val CONTINUED_HEADER_GAP = 12f
    private const val PHOTO_GAP = 14f

    // Photos are downsampled to roughly this size before they are drawn, then scaled to fit the page.
    private const val PHOTO_DECODE_MAX_SIZE = 1200
    private const val PHOTO_MAX_HEIGHT = 400f

    private val TEXT_COLOR = 0xFF1F1F1F.toInt()
    private val META_TEXT_COLOR = 0xFF666666.toInt()
    private val DIVIDER_COLOR = 0xFFDDDDDD.toInt()

    /**
     * Writes [snapshots] to [outputStream] as a single PDF document and returns the page count.
     *
     * Encrypted entries are only rendered with their real title and contents when [decryptKey]
     * is provided (the single entry flow verifies the password before exporting). Without a key
     * the entry is printed with a notice instead of its protected text.
     */
    fun export(
        context: Context,
        snapshots: List<DiaryPdfSnapshot>,
        includePhotos: Boolean,
        decryptKey: String?,
        outputStream: OutputStream,
    ): Int {
        val document = PdfDocument()
        val typeface = FontUtils.getCommonTypeface(context)
        val writer =
            PageWriter(
                document = document,
                footerPaint = createTextPaint(typeface, FOOTER_TEXT_SIZE, META_TEXT_COLOR),
                pageWidth = PAGE_WIDTH,
                pageHeight = PAGE_HEIGHT,
                pageMargin = PAGE_MARGIN,
                footerBaseline = FOOTER_BASELINE,
            )

        return try {
            snapshots.forEach { snapshot ->
                renderEntry(context, writer, snapshot, includePhotos, decryptKey, typeface)
            }
            writer.finishPage()
            document.writeTo(outputStream)
            writer.pageNumber
        } finally {
            runCatching { writer.finishPage() }
            document.close()
        }
    }

    private fun renderEntry(
        context: Context,
        writer: PageWriter,
        snapshot: DiaryPdfSnapshot,
        includePhotos: Boolean,
        decryptKey: String?,
        typeface: Typeface,
    ) {
        val title = resolveTitle(snapshot, decryptKey)
        val contents = resolveContents(context, snapshot, decryptKey)

        val bodyPaint = createTextPaint(typeface, BODY_TEXT_SIZE, TEXT_COLOR)
        val titlePaint = createTextPaint(Typeface.create(typeface, Typeface.BOLD), TITLE_TEXT_SIZE, TEXT_COLOR)
        val metaPaint = createTextPaint(typeface, META_TEXT_SIZE, META_TEXT_COLOR)
        val dividerPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = DIVIDER_COLOR
                strokeWidth = DIVIDER_THICKNESS
            }
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val bodyLayout =
            contents
                .takeIf { it.isNotBlank() }
                ?.let { createTextLayout(it, bodyPaint, BODY_LINE_SPACING_MULTIPLIER) }
        val bodyLineCount = bodyLayout?.lineCount ?: 0
        val continuedLabel = context.getString(R.string.pdf_export_continued)

        var startLine = 0
        var photoIndex = 0
        var firstPage = true

        while (true) {
            writer.startPage()
            val canvas = writer.canvas()
            var y =
                if (firstPage) {
                    drawEntryHeader(canvas, snapshot, title, metaPaint, titlePaint, dividerPaint)
                } else {
                    drawContinuationHeader(canvas, continuedLabel, metaPaint)
                }

            if (bodyLayout != null && startLine < bodyLineCount) {
                val drawn = drawBodySlice(canvas, bodyLayout, startLine, y)
                y = drawn.first
                startLine = drawn.second
            }

            if (startLine >= bodyLineCount && includePhotos) {
                while (photoIndex < snapshot.photoPaths.size) {
                    val bitmap = decodePhoto(snapshot.photoPaths[photoIndex])
                    if (bitmap == null) {
                        photoIndex++
                        continue
                    }

                    val scale =
                        PdfExportUtils.calculatePhotoScale(
                            bitmap.width,
                            bitmap.height,
                            CONTENT_WIDTH,
                            PHOTO_MAX_HEIGHT,
                        )
                    val drawWidth = bitmap.width * scale
                    val drawHeight = bitmap.height * scale
                    if (y + drawHeight > CONTENT_BOTTOM) {
                        bitmap.recycle()
                        break
                    }

                    val left = PAGE_MARGIN + (CONTENT_WIDTH - drawWidth) / 2f
                    canvas.drawBitmap(
                        bitmap,
                        null,
                        RectF(left, y, left + drawWidth, y + drawHeight),
                        photoPaint,
                    )
                    bitmap.recycle()
                    y += drawHeight + PHOTO_GAP
                    photoIndex++
                }
            }

            firstPage = false
            val bodyDone = startLine >= bodyLineCount
            val photosDone = !includePhotos || photoIndex >= snapshot.photoPaths.size
            if (bodyDone && photosDone) break
        }
    }

    /**
     * Draws the part of [layout] that fits under [startY] on the current page.
     *
     * Returns the y position right below the drawn text together with the line index the next
     * page has to continue from. When not even one line fits, nothing is drawn and the same line
     * index is returned so the caller starts a new page.
     */
    private fun drawBodySlice(
        canvas: Canvas,
        layout: StaticLayout,
        startLine: Int,
        startY: Float,
    ): Pair<Float, Int> {
        val layoutTop = layout.getLineTop(startLine)
        val availableHeight = CONTENT_BOTTOM - startY
        var endLine = startLine
        while (endLine < layout.lineCount && layout.getLineBottom(endLine) - layoutTop <= availableHeight) {
            endLine++
        }
        if (endLine <= startLine) return startY to startLine

        canvas.save()
        canvas.clipRect(PAGE_MARGIN, startY, PAGE_WIDTH.toFloat() - PAGE_MARGIN, CONTENT_BOTTOM)
        canvas.translate(PAGE_MARGIN, startY - layoutTop)
        layout.draw(canvas)
        canvas.restore()

        return (startY + layout.getLineBottom(endLine - 1) - layoutTop) to endLine
    }

    private fun drawEntryHeader(
        canvas: Canvas,
        snapshot: DiaryPdfSnapshot,
        title: String,
        metaPaint: TextPaint,
        titlePaint: TextPaint,
        dividerPaint: Paint,
    ): Float {
        var y = PAGE_MARGIN
        val metaText =
            listOf(snapshot.dateString, snapshot.symbolLabel)
                .filter { it.isNotBlank() }
                .joinToString("  ·  ")
        if (metaText.isNotEmpty()) {
            y += -metaPaint.fontMetrics.ascent
            canvas.drawText(metaText, PAGE_MARGIN, y, metaPaint)
            y += metaPaint.fontMetrics.descent + META_TITLE_GAP
        }

        if (title.isNotBlank()) {
            val titleLayout = createTextLayout(title, titlePaint, TITLE_LINE_SPACING_MULTIPLIER)
            canvas.save()
            canvas.translate(PAGE_MARGIN, y)
            titleLayout.draw(canvas)
            canvas.restore()
            y += titleLayout.height + TITLE_DIVIDER_GAP
        }

        canvas.drawRect(
            PAGE_MARGIN,
            y,
            PAGE_WIDTH.toFloat() - PAGE_MARGIN,
            y + DIVIDER_THICKNESS,
            dividerPaint,
        )
        return y + ENTRY_BODY_GAP
    }

    private fun drawContinuationHeader(
        canvas: Canvas,
        continuedLabel: String,
        metaPaint: TextPaint,
    ): Float {
        var y = PAGE_MARGIN
        y += -metaPaint.fontMetrics.ascent
        canvas.drawText(continuedLabel, PAGE_MARGIN, y, metaPaint)
        return y + metaPaint.fontMetrics.descent + CONTINUED_HEADER_GAP
    }

    private fun resolveTitle(
        snapshot: DiaryPdfSnapshot,
        decryptKey: String?,
    ): String =
        when {
            !snapshot.isEncrypt -> snapshot.title
            decryptKey == null -> ""
            else -> decryptOrNull(snapshot.title, decryptKey) ?: ""
        }

    private fun resolveContents(
        context: Context,
        snapshot: DiaryPdfSnapshot,
        decryptKey: String?,
    ): String {
        if (!snapshot.isEncrypt) return snapshot.contents

        val decrypted = decryptKey?.let { decryptOrNull(snapshot.contents, it) }
        return decrypted ?: context.getString(R.string.pdf_export_encrypted_entry_note)
    }

    private fun decryptOrNull(
        cipherText: String,
        decryptKey: String,
    ): String? =
        try {
            JasyptUtils.decrypt(cipherText, decryptKey)
        } catch (_: Exception) {
            null
        }

    private fun createTextLayout(
        text: String,
        paint: TextPaint,
        lineSpacingMultiplier: Float,
    ): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, CONTENT_WIDTH.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, lineSpacingMultiplier)
            .setIncludePad(false)
            .build()

    private fun createTextPaint(
        typeface: Typeface,
        textSize: Float,
        color: Int,
    ): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = textSize
            this.color = color
        }

    /**
     * Decodes [path] with a power of two sample size near [PHOTO_DECODE_MAX_SIZE], applies the
     * EXIF orientation and returns null for missing or corrupted files so that one bad photo
     * never aborts the whole export.
     */
    private fun decodePhoto(path: String): Bitmap? {
        if (!File(path).exists()) return null

        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val options =
                BitmapFactory.Options().apply {
                    inSampleSize =
                        PdfExportUtils.calculateInSampleSize(
                            bounds.outWidth,
                            bounds.outHeight,
                            PHOTO_DECODE_MAX_SIZE,
                            PHOTO_DECODE_MAX_SIZE,
                        )
                }
            val decoded = BitmapFactory.decodeFile(path, options) ?: return null
            applyExifOrientation(decoded, path)
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun applyExifOrientation(
        bitmap: Bitmap,
        path: String,
    ): Bitmap {
        val orientation =
            try {
                ExifInterface(path).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }

        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    /** Holds the currently open PDF page and writes the page number footer when it is finished. */
    private class PageWriter(
        private val document: PdfDocument,
        private val footerPaint: TextPaint,
        private val pageWidth: Int,
        private val pageHeight: Int,
        private val pageMargin: Float,
        private val footerBaseline: Float,
    ) {
        var pageNumber = 0
            private set
        private var currentPage: PdfDocument.Page? = null
        private var currentCanvas: Canvas? = null

        fun startPage() {
            finishPage()
            pageNumber++
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            document.startPage(pageInfo).also { page ->
                page.canvas.drawColor(Color.WHITE)
                currentPage = page
                currentCanvas = page.canvas
            }
        }

        fun canvas(): Canvas = checkNotNull(currentCanvas) { "A PDF page must be started before drawing" }

        fun finishPage() {
            val page = currentPage ?: return
            page.canvas.run {
                drawText(FOOTER_LEFT_LABEL, pageMargin, footerBaseline, footerPaint)
                val pageNumberText = pageNumber.toString()
                drawText(
                    pageNumberText,
                    pageWidth - pageMargin - footerPaint.measureText(pageNumberText),
                    footerBaseline,
                    footerPaint,
                )
            }
            document.finishPage(page)
            currentPage = null
            currentCanvas = null
        }

        private companion object {
            const val FOOTER_LEFT_LABEL = "EasyDiary"
        }
    }
}
