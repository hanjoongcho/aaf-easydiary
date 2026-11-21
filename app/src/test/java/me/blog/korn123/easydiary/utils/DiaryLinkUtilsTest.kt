package me.blog.korn123.easydiary.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `findDiaryLinkRanges finds every diary link of a text`() {
        val text = "어제 일기는 easydiary://diary/221 이고 오늘은 easydiary://diary/268."

        val ranges = DiaryLinkUtils.findDiaryLinkRanges(text)

        assertEquals(listOf("easydiary://diary/221", "easydiary://diary/268"), ranges.map { it.link })
        assertEquals(ranges.map { it.link }, ranges.map { text.subSequence(it.start, it.end).toString() })
    }

    @Test
    fun `a diary link range covers its whole address and sequence`() {
        val text = "easydiary://diary/20261121"

        val range = DiaryLinkUtils.findDiaryLinkRanges(text).single()

        assertEquals(0, range.start)
        assertEquals(text.length, range.end)
        assertEquals(text, range.link)
    }

    @Test
    fun `a short sequence is a diary link too`() {
        val text = "easydiary://diary/5"

        assertEquals(listOf(DiaryLinkRange(0, text.length, text)), DiaryLinkUtils.findDiaryLinkRanges(text))
    }

    @Test
    fun `a diary link written in another case is normalised`() {
        val text = "EASYDIARY://DIARY/221"

        assertEquals(
            listOf(DiaryLinkRange(0, text.length, "easydiary://diary/221")),
            DiaryLinkUtils.findDiaryLinkRanges(text),
        )
    }

    @Test
    fun `a punctuation around a diary link stays outside it`() {
        val text = "(easydiary://diary/221)"

        val range = DiaryLinkUtils.findDiaryLinkRanges(text).single()

        assertEquals("easydiary://diary/221", text.subSequence(range.start, range.end).toString())
        assertEquals(1, range.start)
    }

    @Test
    fun `text without a diary link has no range`() {
        assertTrue(DiaryLinkUtils.findDiaryLinkRanges("오늘은 010-1234-5678 로 전화했다").isEmpty())
        assertTrue(DiaryLinkUtils.findDiaryLinkRanges("easydiary://diary/").isEmpty())
        assertTrue(DiaryLinkUtils.findDiaryLinkRanges("easydiary://diary/not-a-sequence").isEmpty())
        assertTrue(DiaryLinkUtils.findDiaryLinkRanges("easydiary://diary/0").isEmpty())
        assertTrue(DiaryLinkUtils.findDiaryLinkRanges("easydiary://note/221").isEmpty())
        assertTrue(DiaryLinkUtils.findDiaryLinkRanges("https://example.com").isEmpty())
        assertTrue(DiaryLinkUtils.findDiaryLinkRanges("").isEmpty())
        assertTrue(DiaryLinkUtils.findDiaryLinkRanges(null).isEmpty())
    }
}
