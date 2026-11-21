package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.domain.model.Location
import me.blog.korn123.easydiary.domain.model.PhotoUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the dirty check and the draft write policy of the diary editor autosave (issue #261).
 *
 * The editor captures a [DiaryDraftSnapshot] of the state it would persist, and [DiaryDraftTracker] answers
 * whether that state still differs from what the database already holds. The issue requires no write while
 * nothing changed, no draft for an editor with nothing written in it, and an unchanged draft after a restore.
 */
class DiaryDraftAutoSaveTest {
    private val photoA = PhotoUri("/storage/emulated/0/Android/data/me.blog.korn123.easydiary/files/Photos/aaaa", "image/jpeg")
    private val photoB = PhotoUri("/storage/emulated/0/Android/data/me.blog.korn123.easydiary/files/Photos/bbbb", "image/jpeg")
    private val location = Location("Seoul", 37.5665, 126.9780)

    private fun snapshot(
        title: String = "",
        contents: String = "",
        symbolSequence: Int = 0,
        isAllDay: Boolean = false,
        currentTimeMillis: Long = 1_796_000_000_000L,
        photoUris: List<PhotoUri> = emptyList(),
        location: Location? = null,
    ) = DiaryDraftSnapshot(
        title = title,
        contents = contents,
        symbolSequence = symbolSequence,
        isAllDay = isAllDay,
        currentTimeMillis = currentTimeMillis,
        photoUris = photoUris,
        location = location,
    )

    private fun writtenSnapshot() = snapshot(title = "Draft title", contents = "Draft contents")

    @Test
    fun `an editor with nothing written in it never leaves a draft`() {
        assertFalse(snapshot().isWritable)
        assertFalse(snapshot(symbolSequence = 3, isAllDay = true, location = location).isWritable)
    }

    @Test
    fun `a title, contents or photo alone is worth a draft`() {
        assertTrue(snapshot(title = "title").isWritable)
        assertTrue(snapshot(contents = "contents").isWritable)
        assertTrue(snapshot(photoUris = listOf(photoA)).isWritable)
    }

    @Test
    fun `an unwritten editor is not dirty even without a baseline`() {
        assertFalse(DiaryDraftTracker().isDirty(snapshot()))
    }

    @Test
    fun `the state the editor loaded is not written again`() {
        val tracker = DiaryDraftTracker()
        val loaded = writtenSnapshot()
        tracker.markStored(loaded)

        assertFalse(tracker.isDirty(loaded))
    }

    @Test
    fun `an edit after the stored state is dirty`() {
        val tracker = DiaryDraftTracker()
        tracker.markStored(writtenSnapshot())

        assertTrue(tracker.isDirty(snapshot(title = "Draft title", contents = "Draft contents!")))
    }

    @Test
    fun `the state just written becomes the next baseline`() {
        val tracker = DiaryDraftTracker()
        val edited = snapshot(title = "Draft title", contents = "Draft contents, edited")
        tracker.markStored(writtenSnapshot())

        assertTrue(tracker.isDirty(edited))
        tracker.markStored(edited)
        assertFalse(tracker.isDirty(edited))
    }

    @Test
    fun `every field the editor persists makes the draft dirty`() {
        val baseline = snapshot(contents = "contents")
        val tracker = DiaryDraftTracker().apply { markStored(baseline) }

        assertTrue("title", tracker.isDirty(baseline.copy(title = "title")))
        assertTrue("contents", tracker.isDirty(baseline.copy(contents = "contents!")))
        assertTrue("symbol", tracker.isDirty(baseline.copy(symbolSequence = 7)))
        assertTrue("all day", tracker.isDirty(baseline.copy(isAllDay = true)))
        assertTrue("date and time", tracker.isDirty(baseline.copy(currentTimeMillis = 1_796_000_100_000L)))
        assertTrue("attached photo", tracker.isDirty(baseline.copy(photoUris = listOf(photoA))))
        assertTrue("location", tracker.isDirty(baseline.copy(location = location)))
    }

    @Test
    fun `removing an attached photo makes the draft dirty`() {
        val tracker = DiaryDraftTracker()
        tracker.markStored(snapshot(title = "title", photoUris = listOf(photoA, photoB)))

        assertTrue(tracker.isDirty(snapshot(title = "title", photoUris = listOf(photoA))))
        assertTrue(tracker.isDirty(snapshot(title = "title", photoUris = listOf(photoB, photoA))))
    }

    @Test
    fun `a draft keeps exactly what the editor will restore`() {
        val edited = snapshot(
            title = "Draft title",
            contents = "Draft contents",
            symbolSequence = 12,
            isAllDay = true,
            photoUris = listOf(photoA, photoB),
            location = location,
        )

        val draft = edited.toDiary(originDiaryId = 42L)

        assertEquals(42L, draft.originDiaryId)
        assertEquals("Draft title", draft.title)
        assertEquals("Draft contents", draft.contents)
        assertEquals(12, draft.symbolSequence)
        assertTrue(draft.isAllDay)
        assertEquals(edited.currentTimeMillis, draft.currentTimeMillis)
        assertEquals(listOf(photoA, photoB), draft.photoUris)
        assertEquals(location, draft.location)
        assertEquals(edited, DiaryDraftSnapshot.of(draft))
    }

    @Test
    fun `clearing the baseline makes the same state dirty again`() {
        val tracker = DiaryDraftTracker()
        val state = writtenSnapshot()
        tracker.markStored(state)
        assertFalse(tracker.isDirty(state))

        tracker.clear()

        assertTrue(tracker.isDirty(state))
    }

    @Test
    fun `autosave waits for a pause in typing but not longer than the interval`() {
        assertTrue(
            "The debounce must be long enough to skip keystrokes and short enough to be useful",
            DiaryAutoSavePolicy.DEBOUNCE_MILLIS in 3_000L..5_000L,
        )
        assertTrue(
            "The periodic write must bound how much typing an abrupt termination can lose",
            DiaryAutoSavePolicy.INTERVAL_MILLIS in 15_000L..30_000L,
        )
        assertTrue(DiaryAutoSavePolicy.DEBOUNCE_MILLIS < DiaryAutoSavePolicy.INTERVAL_MILLIS)
    }
}
