package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.Location
import me.blog.korn123.easydiary.domain.model.PhotoUri

/**
 * Autosave policy of the diary editor (issue #261).
 *
 * A draft is written after the user stops editing ([DEBOUNCE_MILLIS]) and, while the user keeps typing,
 * at least once every [INTERVAL_MILLIS]. The interval is the bound on how much writing a process that is
 * killed without pausing the editor (a failed lock/PIN entry, a crash, low memory reclaim) can lose,
 * and the debounce keeps a single write from landing on every keystroke.
 */
object DiaryAutoSavePolicy {
    const val DEBOUNCE_MILLIS = 4_000L

    const val INTERVAL_MILLIS = 20_000L
}

/**
 * Immutable snapshot of everything the editor persists into a temporary (draft) diary.
 *
 * `BaseDiaryEditingActivity` captures one of these from the editor state, on the main thread, and writes
 * it through `DiaryRepository.insertTemporaryDiary`. Comparing two snapshots is the dirty check that
 * keeps an unchanged editor from writing to the database: the snapshot last written is kept as the
 * baseline and any later state is compared against it.
 */
data class DiaryDraftSnapshot(
    val title: String = "",
    val contents: String = "",
    val symbolSequence: Int = 0,
    val isAllDay: Boolean = false,
    val currentTimeMillis: Long = 0L,
    val photoUris: List<PhotoUri> = emptyList(),
    val location: Location? = null,
) {
    /**
     * The write policy the editor has always had: an editor with no title, no contents and no photo has
     * nothing worth recovering, so it never leaves a draft behind.
     */
    val isWritable: Boolean
        get() = title.isNotEmpty() || contents.isNotEmpty() || photoUris.isNotEmpty()

    /**
     * True when both snapshots describe the same edit, so writing the draft again would store exactly
     * what is already stored.
     */
    fun isSameEditableStateAs(other: DiaryDraftSnapshot?): Boolean =
        other != null &&
            title == other.title &&
            contents == other.contents &&
            symbolSequence == other.symbolSequence &&
            isAllDay == other.isAllDay &&
            currentTimeMillis == other.currentTimeMillis &&
            photoUris == other.photoUris &&
            location == other.location

    /** Builds the domain diary that is persisted for the draft of [originDiaryId]. */
    fun toDiary(originDiaryId: Long): Diary =
        Diary(
            originDiaryId = originDiaryId,
            currentTimeMillis = currentTimeMillis,
            title = title,
            contents = contents,
            symbolSequence = symbolSequence,
            isAllDay = isAllDay,
            photoUris = photoUris.toList(),
            location = location,
        )

    companion object {
        /** The snapshot of an editor state already loaded from a diary (opening, or restoring a draft). */
        fun of(diary: Diary): DiaryDraftSnapshot =
            DiaryDraftSnapshot(
                title = diary.title,
                contents = diary.contents,
                symbolSequence = diary.symbolSequence,
                isAllDay = diary.isAllDay,
                currentTimeMillis = diary.currentTimeMillis,
                photoUris = diary.photoUris.toList(),
                location = diary.location,
            )
    }
}

/**
 * Remembers the editor state that is already stored and answers whether the current state still has to
 * be written (issue #261, requirement 3).
 *
 * Not thread safe on its own: the editor only touches it from the main thread.
 */
class DiaryDraftTracker {
    private var baseline: DiaryDraftSnapshot? = null

    /** True only when the editor has something worth recovering and it differs from the baseline. */
    fun isDirty(snapshot: DiaryDraftSnapshot): Boolean = snapshot.isWritable && !snapshot.isSameEditableStateAs(baseline)

    /** Records [snapshot] as the state the database now holds, after a write or after loading the editor. */
    fun markStored(snapshot: DiaryDraftSnapshot) {
        baseline = snapshot
    }

    /** Forgets the baseline, so the next writable state is dirty again. */
    fun clear() {
        baseline = null
    }
}
