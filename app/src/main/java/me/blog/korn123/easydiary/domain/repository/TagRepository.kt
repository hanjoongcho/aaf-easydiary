package me.blog.korn123.easydiary.domain.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.domain.model.DiaryTagLink
import me.blog.korn123.easydiary.domain.model.Tag
import me.blog.korn123.easydiary.domain.model.TagCreateResult
import me.blog.korn123.easydiary.domain.model.TagRenameResult
import me.blog.korn123.easydiary.domain.model.TagWithCount

/**
 * Reads and writes the tags of diary entries (issue #270).
 *
 * Writing a tag together with the entry that carries it stays in `DiaryRepository`, because the entry and
 * its tags are one write. This repository serves the places that look at tags on their own: the reading
 * screen's picker, the main screen's filter bar, the tag manager of the settings and the backup file.
 */
interface TagRepository {
    /** Every stored tag, for the picker, which offers tags that are not in use yet as well. */
    fun observeTags(): Flow<List<Tag>>

    /** The tags at least one entry carries, for the main screen's filter bar. */
    fun observeTagsInUse(): Flow<List<Tag>>

    /**
     * Every stored tag with the number of saved entries that carry it, for the tag manager of the settings,
     * which lists the tags no entry uses as well so the writer can delete them.
     */
    fun observeTagsWithCount(): Flow<List<TagWithCount>>

    suspend fun getAllTags(): List<Tag>

    /** Every attachment of a tag to an entry, as the backup file stores them. */
    suspend fun getTagLinks(): List<DiaryTagLink>

    /**
     * The stored tag of each of [names], creating the ones that do not exist yet.
     *
     * Names are cleaned and deduplicated by `TagRules`, so `'Trip'` and `'trip'` resolve to the one row the
     * writer already has.
     */
    suspend fun saveTags(names: List<String>): List<Tag>

    /** Restores tags from a backup, keeping the ids the entries of that backup refer to. */
    suspend fun addAllTags(tags: List<Tag>)

    /**
     * Restores attachments from a backup. Links whose entry or tag is not in the database (a partial
     * restore) are skipped instead of violating the foreign keys.
     */
    suspend fun addAllTagLinks(links: List<DiaryTagLink>)

    suspend fun deleteAllTags()

    /**
     * Adds the tag of [name] for the tag manager, or reports the tag that already holds that name so the screen
     * can say so instead of adding a second row that differs only in case or spaces.
     */
    suspend fun createTag(name: String): TagCreateResult

    /**
     * Renames the tag of [tagId] to [newName].
     *
     * A name another tag already holds is answered as `TagRenameResult.NameTaken` with that tag: the manager
     * asks the writer whether to merge, and [mergeTag] is the step that then removes the source tag.
     */
    suspend fun renameTag(
        tagId: Long,
        newName: String,
    ): TagRenameResult

    /**
     * Attaches every entry of [sourceTagId] to [targetTagId] and removes the source tag, leaving the entries
     * themselves as they are.
     */
    suspend fun mergeTag(
        sourceTagId: Long,
        targetTagId: Long,
    )

    /** Removes the tag of [tagId] from every entry and deletes the tag, keeping the entries. */
    suspend fun deleteTag(tagId: Long)
}
