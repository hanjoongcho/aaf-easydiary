package me.blog.korn123.easydiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The tag ranking of the statistics screen (issue #270).
 *
 * The screen draws the tags that the most entries carry, so the tests pin what the ranking holds: the order by
 * count, the order of equal counts, the limit of ten, and that a tag no entry uses is left out so the screen can
 * tell an empty chart from a chart of empty bars. They pin the shares as well: the percent of a row is counted
 * inside the ranking and the percents of a ranking add up to 100%, which is what lets the ring close.
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
    fun `the ranking totals the entries its tags carry`() {
        val ranked = TagStatistics.topRanked(listOf(tag(1L, "travel", 12), tag(2L, "work", 3)))

        assertEquals(15, TagStatistics.totalCount(ranked))
        assertEquals(0, TagStatistics.totalCount(emptyList()))
    }

    @Test
    fun `the shares of the ranking add up to a hundred`() {
        val ranked =
            TagStatistics.topRanked(
                listOf(
                    tag(1L, "travel", 12),
                    tag(2L, "work", 7),
                    tag(3L, "diet", 3),
                    tag(4L, "reading", 1),
                ),
            )

        val shares = TagStatistics.shares(ranked)

        assertEquals(100, shares.sumOf { it.percent })
        assertEquals(ranked, shares.map { it.ranked })
    }

    @Test
    fun `a share is the part the tag holds of the ranking`() {
        val ranked = TagStatistics.topRanked(listOf(tag(1L, "travel", 15), tag(2L, "work", 5)))

        assertEquals(listOf(75, 25), TagStatistics.shares(ranked).map { it.percent })
    }

    @Test
    fun `equal counts share the ranking evenly`() {
        val ranked = TagStatistics.topRanked((1..4).map { index -> tag(index.toLong(), "tag$index", 5) })

        assertEquals(listOf(25, 25, 25, 25), TagStatistics.shares(ranked).map { it.percent })
    }

    @Test
    fun `the points rounding cuts off go to the largest parts first`() {
        // 3, 2 and 2 of 7 are 42.85%, 28.57% and 28.57%: the two points rounding cuts off go to the largest part
        // first and then to the row that comes first in the ranking, so the ring closes at 100%.
        val ranked = TagStatistics.topRanked(listOf(tag(1L, "a", 3), tag(2L, "b", 2), tag(3L, "c", 2)))

        val shares = TagStatistics.shares(ranked)

        assertEquals(100, shares.sumOf { it.percent })
        assertEquals(listOf(43, 29, 28), shares.map { it.percent })
    }

    @Test
    fun `a ranking of one tag holds the whole ring`() {
        val ranked = TagStatistics.topRanked(listOf(tag(1L, "travel", 9), tag(2L, "unused", 0)))

        assertEquals(listOf(100), TagStatistics.shares(ranked).map { it.percent })
    }

    @Test
    fun `a ranking with nothing counted has no share to state`() {
        assertTrue(TagStatistics.shares(emptyList()).isEmpty())
        assertTrue(TagStatistics.shares(TagStatistics.topRanked(listOf(tag(1L, "unused", 0)))).isEmpty())
    }
}
