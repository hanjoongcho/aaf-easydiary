package me.blog.korn123.easydiary.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.DiaryTagCrossRefEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.entity.TagEntity

data class DiaryWithPhotos(
    @Embedded
    val diary: DiaryEntity,
    @Relation(
        parentColumn = "diaryId",
        entityColumn = "diaryId"
    )
    val photoUris: List<PhotoUriEntity>,
    /**
     * The tags of the entry (issue #270). Loaded through the N:M table, so a diary read anywhere in the app
     * (main list, reading screen, editor, backup) carries its tags without a second query from the caller.
     */
    @Relation(
        parentColumn = "diaryId",
        entityColumn = "tagId",
        associateBy = Junction(
            value = DiaryTagCrossRefEntity::class,
            parentColumn = "diaryId",
            entityColumn = "tagId",
        ),
    )
    val tags: List<TagEntity> = emptyList(),
)
