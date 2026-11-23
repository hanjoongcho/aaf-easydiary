package me.blog.korn123.easydiary.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.DiaryTagLink
import me.blog.korn123.easydiary.domain.model.Tag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The backup file of the diary database (issue #270).
 *
 * The tags travel in two collections of their own (`TAGS`, `DIARY_TAG_CROSS_REF`), and a file written before
 * tags existed holds neither, so a restore has to read an older file without failing on the missing keys.
 */
class DiaryBackupTagTest {
    private val gson = Gson()

    @Test
    fun `a backup written before tags existed holds no tag keys`() {
        val json =
            """
            {
              "DIARY": [
                {"diaryId": 3, "title": "old entry", "contents": "body", "symbolSequence": 1}
              ]
            }
            """.trimIndent()

        val dataMap: Map<String, Any> = gson.fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)

        assertNull(dataMap["TAGS"])
        assertNull(dataMap["DIARY_TAG_CROSS_REF"])
    }

    @Test
    fun `a diary of an older file is read without tags`() {
        val json = """[{"diaryId": 3, "title": "old entry", "contents": "body", "symbolSequence": 1}]"""

        val diaries: List<Diary> =
            gson.fromJson(json, object : TypeToken<List<Diary>>() {}.type)

        assertEquals(1, diaries.size)
        assertEquals(3L, diaries[0].diaryId)
        assertEquals("old entry", diaries[0].title)
        assertTrue(diaries[0].tags.isEmpty())
    }

    @Test
    fun `a diary of a new file keeps the tags it carries`() {
        val json =
            """
            [{
              "diaryId": 7,
              "title": "with tags",
              "contents": "body",
              "tags": [{"tagId": 2, "name": "travel"}, {"tagId": 5, "name": "work"}]
            }]
            """.trimIndent()

        val diaries: List<Diary> =
            gson.fromJson(json, object : TypeToken<List<Diary>>() {}.type)

        assertEquals(2, diaries[0].tags.size)
        assertEquals("travel", diaries[0].tags[0].name)
        assertEquals(5L, diaries[0].tags[1].tagId)
    }

    @Test
    fun `tags and their attachments survive a backup round trip`() {
        val tags = listOf(Tag(tagId = 1, name = "travel"), Tag(tagId = 2, name = "work"))
        val links = listOf(DiaryTagLink(diaryId = 10, tagId = 1), DiaryTagLink(diaryId = 10, tagId = 2))

        val restoredTags: List<Tag> =
            gson.fromJson(gson.toJson(tags), object : TypeToken<List<Tag>>() {}.type)
        val restoredLinks: List<DiaryTagLink> =
            gson.fromJson(gson.toJson(links), object : TypeToken<List<DiaryTagLink>>() {}.type)

        assertEquals(tags, restoredTags)
        assertEquals(links, restoredLinks)
    }
}
