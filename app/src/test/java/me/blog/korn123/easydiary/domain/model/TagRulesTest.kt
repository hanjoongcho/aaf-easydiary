package me.blog.korn123.easydiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The name rules that decide when two tags are the same tag (issue #270).
 */
class TagRulesTest {
    @Test
    fun `surrounding and repeated whitespace is collapsed`() {
        assertEquals("trip to busan", TagRules.displayName("  trip   to  busan "))
    }

    @Test
    fun `names are compared without case`() {
        assertEquals(TagRules.normalize("Trip"), TagRules.normalize("  tRiP "))
    }

    @Test
    fun `a blank name is no tag`() {
        assertNull(TagRules.sanitize("   "))
        assertNull(TagRules.sanitize(""))
    }

    @Test
    fun `names are cut to the readable length`() {
        val longName = "a".repeat(TagRules.MAX_NAME_LENGTH + 10)
        assertEquals(TagRules.MAX_NAME_LENGTH, TagRules.displayName(longName).length)
    }

    @Test
    fun `sanitizeAll drops blanks and repeated tags`() {
        val result = TagRules.sanitizeAll(listOf("Trip", " ", "trip", "Busan", "TRIP "))

        assertEquals(listOf("Trip", "Busan"), result)
    }

    @Test
    fun `sanitizeAll keeps the order the writer picked the tags in`() {
        assertEquals(listOf("work", "travel", "diet"), TagRules.sanitizeAll(listOf("work", "travel", "diet")))
    }

    @Test
    fun `the picker finds a tag by a part of its name`() {
        assertTrue(TagRules.containsQuery("trip to busan", "busa"))
        assertTrue(TagRules.containsQuery("Busan", "BUS"))
        assertFalse(TagRules.containsQuery("Busan", "seoul"))
        assertTrue(TagRules.containsQuery("Busan", ""))
    }

    @Test
    fun `an existing tag is not offered again as a new one`() {
        assertTrue(TagRules.matchesAny("trip", listOf("Trip", "work")))
        assertFalse(TagRules.matchesAny("trip to busan", listOf("Trip", "work")))
    }

    @Test
    fun `a chip shows the tag marker in front of the name`() {
        assertEquals("#trip", TagRules.label("trip"))
    }
}
