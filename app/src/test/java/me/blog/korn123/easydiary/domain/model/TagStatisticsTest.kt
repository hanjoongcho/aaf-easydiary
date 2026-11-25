package me.blog.korn123.easydiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The tag ranking of the statistics screen (issue #270).
 *
 * The screen draws the tags that the most entries carry, so the tests pin what the ranking holds: the order by
 * count, the order of equal counts, the limit of ten, and that a tag no entry uses is left out so the screen can
 * tell an empty chart from a chart of empty bars.
 */
class TagStatisticsTest {
    private fun tag(
        id: Long,
        name: String,
        count: Int,
    ) = TagWithCount(tag = Tag(tagId = id, name = name), diaryCount = count)

    @Test
    fun `the tags the most entries carry come first`() {
        val ranked =
            TagStatistics.topRanked(
                listOf(
                    tag(1L, "work", 3),
                    tag(2L, "travel", 12),
                    tag(3L, "diet", 7),
                ),
            )

        assertEquals(listOf("travel", "diet", "work"), ranked.map { it.tag.name })
    }

    @Test
    fun `equal counts are ordered by name without case`() {
        val ranked =
            TagStatistics.topRanked(
                listOf(
                    tag(1L, "banana", 4),
                    tag(2L, "Apple", 4),
                    tag(3L, "cherry", 4),
                ),
            )

        assertEquals(listOf("Apple", "banana", "cherry"), ranked.map { it.tag.name })
    }

    @Test
    fun `equal counts of the same name keep one order`() {
        val rows = listOf(tag(7L, "trip", 2), tag(3L, "trip", 2))

        assertEquals(
            TagStatistics.topRanked(rows),
            TagStatistics.topRanked(rows.reversed()),
        )
    }

    @Test
    fun `a tag no entry carries is not ranked`() {
        val ranked = TagStatistics.topRanked(listOf(tag(1L, "unused", 0), tag(2L, "travel", 1)))

        assertEquals(listOf("travel"), ranked.map { it.tag.name })
    }

    @Test
    fun `nothing to rank when no entry carries a tag`() {
        assertTrue(TagStatistics.topRanked(listOf(tag(1L, "unused", 0))).isEmpty())
        assertTrue(TagStatistics.topRanked(emptyList()).isEmpty())
    }

    @Test
    fun `only ten tags are ranked`() {
        val tags = (1..15).map { index -> tag(index.toLong(), "tag$index", index) }

        val ranked = TagStatistics.topRanked(tags)

        assertEquals(TagStatistics.TOP_RANK_COUNT, ranked.size)
        assertEquals("tag15", ranked.first().tag.name)
        assertEquals("tag6", ranked.last().tag.name)
    }

    @Test
    fun `the count the bars are measured against is the top of the ranking`() {
        assertEquals(12, TagStatistics.maxCount(TagStatistics.topRanked(listOf(tag(1L, "travel", 12), tag(2L, "work", 3)))))
        assertEquals(0, TagStatistics.maxCount(emptyList()))
    }
}
