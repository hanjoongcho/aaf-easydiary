package me.blog.korn123.easydiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.DiaryTagCrossRefEntity
import me.blog.korn123.easydiary.data.local.entity.TagEntity
import me.blog.korn123.easydiary.domain.model.TagRules

/**
 * Stores the tags of the diary entries (issue #270).
 *
 * Tag rows are created from the name the writer typed, so the write side is [ensureTags]: it returns the
 * existing row of a name instead of adding a second one that differs only in case or spaces. Attachments
 * are written together with the entry they belong to (see `DiaryDao.insertDiaryWithPhotos` and
 * `DiaryDao.updateDiaryWithPhotos`), so an entry and its tags are never half saved.
 */
@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    fun observeTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    suspend fun getTags(): List<TagEntity>

    /**
     * The tags at least one entry carries, which is what the main screen offers as a filter.
     *
     * A draft row (a temporary save of an editor) is skipped: the main list never shows it, so a tag only a
     * leftover draft carries would offer a filter that leads to an empty list.
     */
    @Query(
        """
        SELECT DISTINCT t.* FROM tags t
        INNER JOIN diary_tag_cross_ref r ON t.tagId = r.tagId
        INNER JOIN diaries d ON d.diaryId = r.diaryId
        WHERE d.originDiaryId = 0
        ORDER BY t.name COLLATE NOCASE ASC
        """,
    )
    fun observeTagsInUse(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE normalizedName = :normalizedName LIMIT 1")
    suspend fun getTagByNormalizedName(normalizedName: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    @Query("SELECT * FROM diary_tag_cross_ref ORDER BY diaryId ASC, tagId ASC")
    suspend fun getTagCrossRefs(): List<DiaryTagCrossRefEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRefs(crossRefs: List<DiaryTagCrossRefEntity>)

    /**
     * The stored tag of every name in [names], creating the ones that do not exist yet.
     *
     * Names are cleaned and deduplicated by [TagRules], so the result holds one row per distinct tag and
     * keeps the order the names were given in. A tag the writer picked in an editor but never saved can
     * therefore be written with the entry that finally carries it, without a separate "create tag" step.
     */
    @Transaction
    suspend fun ensureTags(names: List<String>): List<TagEntity> {
        val tags = mutableListOf<TagEntity>()
        TagRules.sanitizeAll(names).forEach { name ->
            val normalizedName = TagRules.normalize(name)
            val existing = getTagByNormalizedName(normalizedName)
            val tag =
                existing ?: run {
                    insertTag(TagEntity(name = name, normalizedName = normalizedName))
                    // Another row with the same key can win the insert (IGNORE keeps it), so the row is read
                    // back instead of being built from the id the insert returned.
                    getTagByNormalizedName(normalizedName)
                }
            if (tag != null && tags.none { it.tagId == tag.tagId }) tags.add(tag)
        }
        return tags
    }
}
