package me.blog.korn123.easydiary.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photo_uris",
    foreignKeys = [
        ForeignKey(
            entity = DiaryEntity::class,
            parentColumns = ["diaryId"],
            childColumns = ["diaryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("diaryId")]
)
data class PhotoUriEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var diaryId: Int = 0,
    var photoUri: String? = null,
    var mimeType: String? = null
)
