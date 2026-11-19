package me.blog.korn123.easydiary.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class PdfExportUtilsTest {
    private val seoulTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Seoul")

    // 2026-11-19 16:56:33 (Asia/Seoul)
    private val exportTimeMillis = 1_795_074_993_000L

    @Test
    fun `single export file name contains the diary id and export date`() {
        val fileName = PdfExportUtils.buildSingleExportFileName(12L, exportTimeMillis, seoulTimeZone)

        assertEquals("EasyDiary_12_20261119.pdf", fileName)
    }

    @Test
    fun `multi export file name contains the export date and time`() {
        val fileName = PdfExportUtils.buildMultiExportFileName(exportTimeMillis, seoulTimeZone)

        assertEquals("EasyDiary_20261119_165633.pdf", fileName)
    }

    @Test
    fun `export limit accepts one entry up to the maximum`() {
        assertTrue(PdfExportUtils.isWithinExportLimit(1))
        assertTrue(PdfExportUtils.isWithinExportLimit(PdfExportUtils.MAX_EXPORT_COUNT))
        assertFalse(PdfExportUtils.isWithinExportLimit(0))
        assertFalse(PdfExportUtils.isWithinExportLimit(PdfExportUtils.MAX_EXPORT_COUNT + 1))
    }

    @Test
    fun `inSampleSize halves large bitmaps until they reach the requested size`() {
        assertEquals(1, PdfExportUtils.calculateInSampleSize(800, 600, 1200, 1200))
        assertEquals(2, PdfExportUtils.calculateInSampleSize(4000, 3000, 1200, 1200))
        assertEquals(4, PdfExportUtils.calculateInSampleSize(8000, 6000, 1200, 1200))
    }

    @Test
    fun `inSampleSize falls back to one for invalid sizes`() {
        assertEquals(1, PdfExportUtils.calculateInSampleSize(0, 600, 1200, 1200))
        assertEquals(1, PdfExportUtils.calculateInSampleSize(800, -1, 1200, 1200))
        assertEquals(1, PdfExportUtils.calculateInSampleSize(800, 600, 0, 1200))
    }

    @Test
    fun `photo scale fits the landscape photo width and keeps the aspect ratio`() {
        val scale = PdfExportUtils.calculatePhotoScale(2000, 1000, 500f, 400f)

        assertEquals(0.25f, scale, 0.0001f)
        assertEquals(500f, 2000 * scale, 0.0001f)
        assertEquals(250f, 1000 * scale, 0.0001f)
    }

    @Test
    fun `photo scale bounds a tall photo by the maximum height`() {
        val scale = PdfExportUtils.calculatePhotoScale(1000, 4000, 500f, 400f)

        assertEquals(0.1f, scale, 0.0001f)
        assertEquals(400f, 4000 * scale, 0.0001f)
    }

    @Test
    fun `photo scale returns one for invalid input`() {
        assertEquals(1f, PdfExportUtils.calculatePhotoScale(0, 4000, 500f, 400f), 0.0001f)
        assertEquals(1f, PdfExportUtils.calculatePhotoScale(1000, 4000, 0f, 400f), 0.0001f)
    }
}
