package me.blog.korn123.easydiary.domain.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.domain.model.DiaryTagLink
import me.blog.korn123.easydiary.domain.model.Tag

/**
 * Reads and writes the tags of diary entries (issue #270).
 *
 * Writing a tag together with the entry that carries it stays in `DiaryRepository`, because the entry and
 * its tags are one write. This repository serves the places that look at tags on their own: the reading
 * screen's picker, the main screen's filter bar and the backup file.
 */
interface TagRepository {
    /** Every stored tag, for the picker, which offers tags that are not in use yet as well. */
    fun observeTags(): Flow<List<Tag>>

    /** The tags at least one entry carries, for the main screen's filter bar. */
    fun observeTagsInUse(): Flow<List<Tag>>

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
}
