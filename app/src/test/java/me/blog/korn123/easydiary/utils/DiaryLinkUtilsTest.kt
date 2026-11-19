package me.blog.korn123.easydiary.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit test suite for DiaryLinkUtils.
 */
class DiaryLinkUtilsTest {
    private fun assertSequence(
        expected: Long,
        link: String?,
    ) {
        assertEquals(expected, DiaryLinkUtils.parseDiarySequence(link) ?: -1L)
    }

    @Test
    fun `createDiaryLinkUrl builds the expected deep link`() {
        assertEquals("easydiary://diary/221", DiaryLinkUtils.createDiaryLinkUrl(221))
        assertEquals("easydiary://diary/1", DiaryLinkUtils.createDiaryLinkUrl(1))
    }

    @Test
    fun `parseDiarySequence returns the sequence of a diary link`() {
        assertSequence(221, "easydiary://diary/221")
        assertSequence(221, "  easydiary://diary/221  ")
        assertSequence(221, "easydiary://diary/221/")
        assertSequence(221, "EASYDIARY://DIARY/221")
        assertSequence(221, "easydiary://diary/221?from=browser")
    }

    @Test
    fun `parseDiarySequence ignores links that do not point to a diary`() {
        assertNull(DiaryLinkUtils.parseDiarySequence("https://github.com/hanjoongcho/aaf-easydiary"))
        assertNull(DiaryLinkUtils.parseDiarySequence("easydiary://note/221"))
        assertNull(DiaryLinkUtils.parseDiarySequence("easydiary://diary/"))
        assertNull(DiaryLinkUtils.parseDiarySequence("easydiary://diary/not-a-sequence"))
        assertNull(DiaryLinkUtils.parseDiarySequence("easydiary://diary/-1"))
        assertNull(DiaryLinkUtils.parseDiarySequence("easydiary://diary/0"))
        assertNull(DiaryLinkUtils.parseDiarySequence(""))
        assertNull(DiaryLinkUtils.parseDiarySequence(null as String?))
    }

    @Test
    fun `parseDiarySequence round trips a generated link`() {
        val sequence = 12345L
        assertSequence(sequence, DiaryLinkUtils.createDiaryLinkUrl(sequence))
    }
}
