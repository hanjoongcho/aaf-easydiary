package me.blog.korn123.easydiary.domain.model

import me.blog.korn123.easydiary.helper.DiaryEditingConstants

data class Diary(
    val diaryId: Int = DiaryEditingConstants.DIARY_SEQUENCE_INIT,
    val originDiaryId: Int = DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT,
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val title: String? = null,
    val contents: String? = null,
    val dateString: String? = null,
    val symbolSequence: Int = 0,
    val photoUris: List<PhotoUri> = emptyList(),
    val linkedDiaries: List<Int> = emptyList(),
    val fontName: String? = null,
    val fontSize: Float = 0f,
    val isAllDay: Boolean = false,
    val isEncrypt: Boolean = false,
    val encryptKeyHash: String? = null,
    val isSelected: Boolean = false,
    val location: Location? = null,
    val isHoliday: Boolean = false
)
