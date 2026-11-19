package me.blog.korn123.easydiary.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.min

/**
 * Pure Kotlin helpers for the PDF export feature.
 *
 * Everything here can be verified with plain JVM unit tests: file name rules, the export
 * count limit and the bitmap downsampling / photo fitting math used by the renderer.
 */
object PdfExportUtils {
    const val FILE_NAME_PREFIX = "EasyDiary"
    const val MAX_EXPORT_COUNT = 50

    private const val FILE_NAME_EXTENSION = "pdf"
    private const val SINGLE_FILE_NAME_DATE_PATTERN = "yyyyMMdd"
    private const val MULTI_FILE_NAME_DATE_PATTERN = "yyyyMMdd_HHmmss"

    fun buildSingleExportFileName(
        diaryId: Long,
        timeMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault(),
    ): String =
        buildFileName(
            "${FILE_NAME_PREFIX}_${diaryId}_${format(timeMillis, SINGLE_FILE_NAME_DATE_PATTERN, timeZone)}",
        )

    fun buildMultiExportFileName(
        timeMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault(),
    ): String = buildFileName("${FILE_NAME_PREFIX}_${format(timeMillis, MULTI_FILE_NAME_DATE_PATTERN, timeZone)}")

    fun isWithinExportLimit(
        selectedCount: Int,
        maxCount: Int = MAX_EXPORT_COUNT,
    ): Boolean = selectedCount in 1..maxCount

    /**
     * Returns a power-of-two [android.graphics.BitmapFactory.Options.inSampleSize] that keeps
     * the decoded bitmap at or above the requested size with the smallest amount of pixels,
     * following the algorithm from the Android documentation.
     */
    fun calculateInSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        requiredWidth: Int,
        requiredHeight: Int,
    ): Int {
        if (sourceWidth <= 0 || sourceHeight <= 0 || requiredWidth <= 0 || requiredHeight <= 0) return 1

        var sampleSize = 1
        var halfWidth = sourceWidth / 2
        var halfHeight = sourceHeight / 2
        while (halfWidth / sampleSize >= requiredWidth && halfHeight / sampleSize >= requiredHeight) {
            sampleSize *= 2
        }
        return sampleSize
    }

    /**
     * Scale factor that fits a [sourceWidth] x [sourceHeight] photo into the given bounds while
     * keeping its aspect ratio. The result is never smaller than 1, so photos are scaled up to
     * the printable width instead of being printed at their original pixel size.
     */
    fun calculatePhotoScale(
        sourceWidth: Int,
        sourceHeight: Int,
        maxWidth: Float,
        maxHeight: Float,
    ): Float {
        if (sourceWidth <= 0 || sourceHeight <= 0 || maxWidth <= 0f || maxHeight <= 0f) return 1f
        return min(maxWidth / sourceWidth, maxHeight / sourceHeight)
    }

    private fun buildFileName(baseName: String): String = "$baseName.$FILE_NAME_EXTENSION"

    private fun format(
        timeMillis: Long,
        pattern: String,
        timeZone: TimeZone,
    ): String =
        SimpleDateFormat(pattern, Locale.US)
            .apply { this.timeZone = timeZone }
            .format(Date(timeMillis))
}
