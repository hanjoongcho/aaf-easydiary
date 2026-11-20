package me.blog.korn123.easydiary.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for ClickableLinkUtils, the rules behind the clickable URL and phone
 * number support of a diary (#167).
 */
class ClickableLinkUtilsTest {
    @Test
    fun `a diary link stays inside the app`() {
        assertEquals(DiaryLinkTarget.Diary(221), ClickableLinkUtils.resolveTarget("easydiary://diary/221"))
        assertEquals(DiaryLinkTarget.Diary(221), ClickableLinkUtils.resolveTarget("  easydiary://diary/221  "))
    }

    @Test
    fun `a tel link becomes a phone target`() {
        assertEquals(DiaryLinkTarget.Phone("01012345678"), ClickableLinkUtils.resolveTarget("tel:01012345678"))
        assertEquals(DiaryLinkTarget.Phone("010-1234-5678"), ClickableLinkUtils.resolveTarget("TEL:010-1234-5678"))
        assertEquals(DiaryLinkTarget.Phone("+821031201547"), ClickableLinkUtils.resolveTarget("tel:+821031201547"))
    }

    @Test
    fun `a tel link that is not a valid uri still becomes a phone target`() {
        assertEquals(DiaryLinkTarget.Phone("02 123 4567"), ClickableLinkUtils.resolveTarget("tel:02 123 4567"))
    }

    @Test
    fun `a web page becomes a web target`() {
        assertEquals(
            DiaryLinkTarget.Web("https://github.com/hanjoongcho/aaf-easydiary"),
            ClickableLinkUtils.resolveTarget("https://github.com/hanjoongcho/aaf-easydiary"),
        )
        assertEquals(DiaryLinkTarget.Web("http://example.com"), ClickableLinkUtils.resolveTarget("http://example.com"))
    }

    @Test
    fun `any other address is left to the system`() {
        assertEquals(
            DiaryLinkTarget.External("mailto:someone@example.com"),
            ClickableLinkUtils.resolveTarget("mailto:someone@example.com"),
        )
        assertEquals(
            DiaryLinkTarget.External("easydiary://note/221"),
            ClickableLinkUtils.resolveTarget("easydiary://note/221"),
        )
    }

    @Test
    fun `a blank link has no target`() {
        assertNull(ClickableLinkUtils.resolveTarget(null))
        assertNull(ClickableLinkUtils.resolveTarget(""))
        assertNull(ClickableLinkUtils.resolveTarget("   "))
        assertNull(ClickableLinkUtils.resolveTarget("tel:"))
    }

    @Test
    fun `a date or a time is not a phone number`() {
        assertTrue(ClickableLinkUtils.isDateOrTimeLike("2021-10-23"))
        assertTrue(ClickableLinkUtils.isDateOrTimeLike("2021.10.23"))
        assertTrue(ClickableLinkUtils.isDateOrTimeLike("2021/10/23"))
        assertTrue(ClickableLinkUtils.isDateOrTimeLike("2021. 10. 23"))
        assertTrue(ClickableLinkUtils.isDateOrTimeLike("2021-10-23 05:14"))
        assertTrue(ClickableLinkUtils.isDateOrTimeLike("05:14"))
        assertTrue(ClickableLinkUtils.isDateOrTimeLike("05:14:33"))
    }

    @Test
    fun `a phone number is not a date or a time`() {
        assertFalse(ClickableLinkUtils.isDateOrTimeLike("010-1234-5678"))
        assertFalse(ClickableLinkUtils.isDateOrTimeLike("+82 10-3120-1547"))
        assertFalse(ClickableLinkUtils.isDateOrTimeLike("1544-1234"))
        assertFalse(ClickableLinkUtils.isDateOrTimeLike("2021"))
        assertFalse(ClickableLinkUtils.isDateOrTimeLike(""))
        assertFalse(ClickableLinkUtils.isDateOrTimeLike(null))
    }

    @Test
    fun `only a readable diary in the reading screen gets text links`() {
        assertTrue(ClickableLinkUtils.shouldApplyTextLinks(isEncrypt = false, isRecyclerItem = false))
        assertFalse(ClickableLinkUtils.shouldApplyTextLinks(isEncrypt = true, isRecyclerItem = false))
        assertFalse(ClickableLinkUtils.shouldApplyTextLinks(isEncrypt = false, isRecyclerItem = true))
        assertFalse(ClickableLinkUtils.shouldApplyTextLinks(isEncrypt = true, isRecyclerItem = true))
    }
}
