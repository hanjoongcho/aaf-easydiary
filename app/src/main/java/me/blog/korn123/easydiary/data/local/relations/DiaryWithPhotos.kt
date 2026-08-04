package me.blog.korn123.easydiary.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import me.blog.korn123.easydiary.data.local.models.DiaryEntity
import me.blog.korn123.easydiary.data.local.models.PhotoUriEntity

data class DiaryWithPhotos(
    @Embedded
    val diary: DiaryEntity,
    @Relation(
        parentColumn = "diaryId",
        entityColumn = "diaryId"
    )
    val photoUris: List<PhotoUriEntity>
)
