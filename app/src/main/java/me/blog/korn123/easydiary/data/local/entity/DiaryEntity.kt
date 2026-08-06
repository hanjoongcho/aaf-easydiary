package me.blog.korn123.easydiary.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.blog.korn123.easydiary.helper.DiaryEditingConstants

@Entity(tableName = "diaries")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true)
    var diaryId: Int = 0,
    var originDiaryId: Int = DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT,
    var currentTimeMillis: Long = System.currentTimeMillis(),
    var title: String? = null,
    var contents: String? = null,
    var dateString: String? = null,
    var symbolSequence: Int = 0,
    var linkedDiaries: List<Int> = emptyList(),
    var fontName: String? = null,
    var fontSize: Float = 0f,
    var isAllDay: Boolean = false,
    var isEncrypt: Boolean = false,
    var encryptKeyHash: String? = null,
    var isSelected: Boolean = false,
    @Embedded(prefix = "loc_")
    var location: LocationEntity? = null,
    var isHoliday: Boolean = false,
)

data class LocationEntity(
    var address: String? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
)
