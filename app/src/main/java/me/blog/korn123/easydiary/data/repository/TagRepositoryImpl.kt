package me.blog.korn123.easydiary.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.blog.korn123.easydiary.data.local.dao.DiaryDao
import me.blog.korn123.easydiary.data.local.dao.TagCreateOutcome
import me.blog.korn123.easydiary.data.local.dao.TagDao
import me.blog.korn123.easydiary.data.local.dao.TagRenameOutcome
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.domain.model.DiaryTagLink
import me.blog.korn123.easydiary.domain.model.Tag
import me.blog.korn123.easydiary.domain.model.TagCreateResult
import me.blog.korn123.easydiary.domain.model.TagRenameResult
import me.blog.korn123.easydiary.domain.model.TagWithCount
import me.blog.korn123.easydiary.domain.repository.TagRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl
    @Inject
    constructor(
        private val tagDao: TagDao,
        private val diaryDao: DiaryDao,
    ) : TagRepository {
        override fun observeTags(): Flow<List<Tag>> = tagDao.observeTags().map { entities -> entities.map { it.toDomain() } }

        override fun observeTagsInUse(): Flow<List<Tag>> = tagDao.observeTagsInUse().map { entities -> entities.map { it.toDomain() } }

        override fun observeTagsWithCount(): Flow<List<TagWithCount>> =
            tagDao.observeTagsWithCount().map { rows -> rows.map { it.toDomain() } }

        override suspend fun getAllTags(): List<Tag> = tagDao.getTags().map { it.toDomain() }

        override suspend fun getTagLinks(): List<DiaryTagLink> = tagDao.getTagCrossRefs().map { it.toDomain() }

        override suspend fun saveTags(names: List<String>): List<Tag> = tagDao.ensureTags(names).map { it.toDomain() }

        override suspend fun addAllTags(tags: List<Tag>) {
            tagDao.insertTags(tags.map { it.toEntity() })
        }

        override suspend fun addAllTagLinks(links: List<DiaryTagLink>) {
            // A backup restores entries first, so the rows a link may point at are known here. Anything else
            // (a link of a tag or an entry that is not in this database) is dropped instead of failing the
            // whole restore on a foreign key.
            val diaryIds = diaryDao.getDiaryIds().toSet()
            val tagIds = tagDao.getTags().map { it.tagId }.toSet()
            val validLinks = links.filter { it.diaryId in diaryIds && it.tagId in tagIds }
            tagDao.insertTagCrossRefs(validLinks.map { it.toEntity() })
        }

        override suspend fun deleteAllTags() {
            tagDao.deleteAllTags()
        }

        override suspend fun createTag(name: String): TagCreateResult =
            when (val outcome = tagDao.createTag(name)) {
                is TagCreateOutcome.Created -> TagCreateResult.Created(outcome.tag.toDomain())
                is TagCreateOutcome.Exists -> TagCreateResult.AlreadyExists(outcome.tag.toDomain())
                TagCreateOutcome.EmptyName -> TagCreateResult.EmptyName
            }

        override suspend fun renameTag(
            tagId: Long,
            newName: String,
        ): TagRenameResult =
            when (val outcome = tagDao.renameTag(tagId, newName)) {
                is TagRenameOutcome.Renamed -> TagRenameResult.Renamed(outcome.tag.toDomain())
                is TagRenameOutcome.NameTaken -> TagRenameResult.NameTaken(outcome.tag.toDomain())
                TagRenameOutcome.Unchanged -> TagRenameResult.Unchanged
                TagRenameOutcome.NotFound -> TagRenameResult.NotFound
                TagRenameOutcome.EmptyName -> TagRenameResult.EmptyName
            }

        override suspend fun mergeTag(
            sourceTagId: Long,
            targetTagId: Long,
        ) {
            tagDao.mergeTag(sourceTagId, targetTagId)
        }

        override suspend fun deleteTag(tagId: Long) {
            tagDao.deleteTag(tagId)
        }
    }
