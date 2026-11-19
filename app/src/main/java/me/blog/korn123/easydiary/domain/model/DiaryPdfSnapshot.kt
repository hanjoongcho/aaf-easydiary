package me.blog.korn123.easydiary.domain.model

/**
 * Immutable copy of the diary fields that the PDF renderer needs.
 *
 * The values are copied from a [Diary] on the main thread before PDF generation moves to a
 * background dispatcher, so the export never reads the repository model (or its photo list)
 * while the document is being written, and the document stays consistent even if the
 * underlying entry changes during the export.
 */
data class DiaryPdfSnapshot(
    val diaryId: Long,
    val currentTimeMillis: Long,
    val dateString: String,
    val title: String,
    val contents: String,
    val symbolSequence: Int,
    val symbolLabel: String,
    val photoPaths: List<String>,
    val isEncrypt: Boolean,
)
