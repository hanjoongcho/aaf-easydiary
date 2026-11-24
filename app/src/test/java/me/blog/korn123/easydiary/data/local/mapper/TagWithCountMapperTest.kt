package me.blog.korn123.easydiary.data.local.mapper

import me.blog.korn123.easydiary.data.local.entity.TagEntity
import me.blog.korn123.easydiary.data.local.relations.TagWithCountRow
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The row the tag manager reads becomes the tag with its usage count (issue #270), so the screen lists the count
 * of the query rather than counting anything itself.
 */
class TagWithCountMapperTest {
    @Test
    fun `a counted row keeps the tag and its diary count`() {
        val row =
            TagWithCountRow(
                tag = TagEntity(tagId = 7L, name = "Travel", normalizedName = "travel"),
                diaryCount = 12,
            )

        val tagWithCount = row.toDomain()

        assertEquals(7L, tagWithCount.tag.tagId)
        assertEquals("Travel", tagWithCount.tag.name)
        assertEquals("#Travel", tagWithCount.tag.label)
        assertEquals(12, tagWithCount.diaryCount)
    }

    @Test
    fun `a tag no entry carries keeps a count of zero`() {
        val row =
            TagWithCountRow(
                tag = TagEntity(tagId = 3L, name = "diet", normalizedName = "diet"),
                diaryCount = 0,
            )

        assertEquals(0, row.toDomain().diaryCount)
    }
}
