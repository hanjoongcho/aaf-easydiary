package me.blog.korn123.easydiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.DiaryTagCrossRefEntity
import me.blog.korn123.easydiary.data.local.entity.TagEntity
import me.blog.korn123.easydiary.data.local.relations.TagWithCountRow
import me.blog.korn123.easydiary.domain.model.TagRules

/**
 * Stores the tags of the diary entries (issue #270).
 *
 * Tag rows are created from the name the writer typed, so the write side of an entry is [ensureTags]: it returns
 * the existing row of a name instead of adding a second one that differs only in case or spaces. The tag manager
 * of the settings writes names on their own ([createTag], [renameTag], [mergeTag], [deleteTag]) and each of those
 * runs in one transaction, so a tag and its attachments are never half changed. Attachments are written together
 * with the entry they belong to (see `DiaryDao.insertDiaryWithPhotos` and `DiaryDao.updateDiaryWithPhotos`), so an
 * entry and its tags are never half saved.
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

    @Query("SELECT * FROM tags WHERE tagId = :tagId LIMIT 1")
    suspend fun getTagById(tagId: Long): TagEntity?

    /**
     * Every tag with the number of saved entries that carry it, for the tag manager of the settings.
     *
     * A tag no entry carries is listed as well, with a count of 0, so the writer can find and delete the names
     * left behind by tags that were picked but never written. Draft rows are skipped for the reason
     * [observeTagsInUse] gives.
     */
    @Query(
        """
        SELECT t.tagId AS tagId, t.name AS name, t.normalizedName AS normalizedName, COUNT(d.diaryId) AS diaryCount
        FROM tags t
        LEFT JOIN diary_tag_cross_ref r ON r.tagId = t.tagId
        LEFT JOIN diaries d ON d.diaryId = r.diaryId AND d.originDiaryId = 0
        GROUP BY t.tagId
        ORDER BY t.name COLLATE NOCASE ASC
        """,
    )
    fun observeTagsWithCount(): Flow<List<TagWithCountRow>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    @Query("SELECT * FROM diary_tag_cross_ref ORDER BY diaryId ASC, tagId ASC")
    suspend fun getTagCrossRefs(): List<DiaryTagCrossRefEntity>

    @Query("SELECT * FROM diary_tag_cross_ref WHERE tagId = :tagId")
    suspend fun getTagCrossRefsByTagId(tagId: Long): List<DiaryTagCrossRefEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRefs(crossRefs: List<DiaryTagCrossRefEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRef(crossRef: DiaryTagCrossRefEntity)

    @Query("DELETE FROM diary_tag_cross_ref WHERE tagId = :tagId")
    suspend fun deleteTagCrossRefsByTagId(tagId: Long)

    @Query("UPDATE tags SET name = :name, normalizedName = :normalizedName WHERE tagId = :tagId")
    suspend fun updateTagName(
        tagId: Long,
        name: String,
        normalizedName: String,
    )

    @Query("DELETE FROM tags WHERE tagId = :tagId")
    suspend fun deleteTagById(tagId: Long)

    /**
     * The stored tag of every name in [names], creating the ones that do not exist yet.
     *
     * Names are cleaned and deduplicated by [TagRules], so the result holds one row per distinct tag and
     * keeps the order the names were given in. A tag created in the picker but never written can therefore be
     * stored with the entry that finally carries it, without a separate "create tag" step.
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

    /**
     * Adds the tag the tag manager typed, or reports the tag that already holds that name.
     *
     * The manager adds a tag on its own, so this is unlike [ensureTags], which resolves the names of an entry:
     * a name that is already stored is an answer the writer has to see, not something to reuse silently.
     */
    @Transaction
    suspend fun createTag(rawName: String): TagCreateOutcome {
        val name = TagRules.sanitize(rawName) ?: return TagCreateOutcome.EmptyName
        val normalizedName = TagRules.normalize(name)
        getTagByNormalizedName(normalizedName)?.let { return TagCreateOutcome.Exists(it) }
        val rowId = insertTag(TagEntity(name = name, normalizedName = normalizedName))
        // Another row with the same key can win the insert (IGNORE keeps it and returns -1), so the stored row is
        // read back and handed on with whatever spelling it has.
        val stored = getTagByNormalizedName(normalizedName)
        return when {
            stored == null -> TagCreateOutcome.Created(TagEntity(tagId = rowId, name = name, normalizedName = normalizedName))
            rowId == -1L -> TagCreateOutcome.Exists(stored)
            else -> TagCreateOutcome.Created(stored)
        }
    }

    /**
     * Renames the tag of [tagId] to [rawName], or reports the tag that already holds that name.
     *
     * Only the name row changes here: the entries keep their attachments, and a name another tag holds is not
     * merged into it. [mergeTag] is the separate step the writer agrees to when the manager asks.
     */
    @Transaction
    suspend fun renameTag(
        tagId: Long,
        rawName: String,
    ): TagRenameOutcome {
        val name = TagRules.sanitize(rawName) ?: return TagRenameOutcome.EmptyName
        val tag = getTagById(tagId) ?: return TagRenameOutcome.NotFound
        val normalizedName = TagRules.normalize(name)
        // A name that differs only in case or spaces is the same tag, so it is checked before the key changes:
        // writing it would break the unique index against the row itself.
        if (normalizedName != tag.normalizedName) {
            getTagByNormalizedName(normalizedName)?.let { return TagRenameOutcome.NameTaken(it) }
        }
        if (name == tag.name) return TagRenameOutcome.Unchanged
        updateTagName(tagId, name, normalizedName)
        return TagRenameOutcome.Renamed(tag.copy(name = name, normalizedName = normalizedName))
    }

    /**
     * Merges [sourceTagId] into [targetTagId] and removes the source tag (issue #270).
     *
     * Every entry the source tag carried is attached to the target instead. An entry that already carries both
     * keeps the one attachment (IGNORE drops the copy), and the entries themselves are not touched, so a merge
     * only changes which name their tag shows.
     */
    @Transaction
    suspend fun mergeTag(
        sourceTagId: Long,
        targetTagId: Long,
    ) {
        if (sourceTagId == targetTagId) return
        getTagCrossRefsByTagId(sourceTagId).forEach { crossRef ->
            insertTagCrossRef(DiaryTagCrossRefEntity(diaryId = crossRef.diaryId, tagId = targetTagId))
        }
        deleteTagCrossRefsByTagId(sourceTagId)
        deleteTagById(sourceTagId)
    }

    /**
     * Deletes the tag of [tagId] and its attachments (issue #270).
     *
     * The attachments are removed before the tag row so the delete never relies on the foreign key cascade, and
     * nothing of the entries themselves is removed: an entry that carried the tag simply carries one tag fewer.
     */
    @Transaction
    suspend fun deleteTag(tagId: Long) {
        deleteTagCrossRefsByTagId(tagId)
        deleteTagById(tagId)
    }
}
