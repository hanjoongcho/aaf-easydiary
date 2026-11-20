package me.blog.korn123.easydiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for the photo policy of [Diary].
 *
 * An encrypted diary must not expose the file path of its attached photos: every screen keeps the
 * number of photos but replaces each path with an empty one, which the UI renders as a padlock.
 */
class DiaryEncryptionPolicyTest {
    private val photoUris =
        listOf(
            PhotoUri("/storage/emulated/0/Android/data/me.blog.korn123.easydiary/files/Photos/aaaa"),
            PhotoUri("/storage/emulated/0/Android/data/me.blog.korn123.easydiary/files/Photos/bbbb"),
        )

    @Test
    fun `plain diary keeps every photo path`() {
        val diary = Diary(photoUris = photoUris, isEncrypt = false)

        assertEquals(photoUris, diary.photoUrisWithEncryptionPolicy())
        assertTrue(diary.photoUrisWithEncryptionPolicy()?.none { it.isEncrypt() } ?: false)
    }

    @Test
    fun `encrypted diary keeps the photo count but hides every path`() {
        val photoUrisWithEncryptionPolicy =
            Diary(photoUris = photoUris, isEncrypt = true).photoUrisWithEncryptionPolicy()

        assertEquals(photoUris.size, photoUrisWithEncryptionPolicy?.size)
        assertTrue(photoUrisWithEncryptionPolicy?.all { it.isEncrypt() } ?: false)
        assertTrue(photoUrisWithEncryptionPolicy?.all { it.photoUri == "" } ?: false)
    }

    @Test
    fun `diary without photos has no photo to hide`() {
        assertEquals(emptyList<PhotoUri>(), Diary(isEncrypt = false).photoUrisWithEncryptionPolicy())
        assertEquals(emptyList<PhotoUri>(), Diary(isEncrypt = true).photoUrisWithEncryptionPolicy())
    }

    @Test
    fun `decrypting a diary restores its photo paths`() {
        val encryptedDiary = Diary(photoUris = photoUris, isEncrypt = true)
        val decryptedDiary = encryptedDiary.copy(isEncrypt = false)

        assertTrue(encryptedDiary.photoUrisWithEncryptionPolicy()?.all { it.isEncrypt() } ?: false)
        assertEquals(photoUris, decryptedDiary.photoUrisWithEncryptionPolicy())
    }
}
