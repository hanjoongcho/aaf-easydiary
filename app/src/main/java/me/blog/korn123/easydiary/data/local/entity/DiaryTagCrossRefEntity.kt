package me.blog.korn123.easydiary.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * The N:M attachment between a diary and a tag (issue #270).
 *
 * Both sides cascade: deleting an entry or a tag removes its attachments, so no row here can point at a
 * diary or a tag that is gone. The composite primary key already makes one attachment unique, and the
 * index on [tagId] serves the tag filter, which looks the tag up from this side.
 */
@Entity(
    tableName = "diary_tag_cross_ref",
    primaryKeys = ["diaryId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntity::class,
            parentColumns = ["diaryId"],
            childColumns = ["diaryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["diaryId"]), Index(value = ["tagId"])],
)
data class DiaryTagCrossRefEntity(
    var diaryId: Long = 0L,
    var tagId: Long = 0L,
)
