package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.PhotoUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite for the photos the postcard screen lays out (issue #246).
 *
 * The postcard screen must hide the attached photos of an encrypted diary the same way the reading
 * screen and the diary cards do, so a postcard image cannot contain them.
 */
class PostcardPhotoUtilsTest {
    private val applicationDataDirectory = "/data/user/0/me.blog.korn123.easydiary"
    private val photoDirectory = "/AAFactory/EasyDiary/Photos"
    private val photoUris =
        listOf(
            PhotoUri("/storage/emulated/0$photoDirectory/1c9b5b2e-aaaa"),
            PhotoUri("/storage/emulated/0$photoDirectory/4f7a0c11-bbbb"),
        )

    @Test
    fun `plain diary keeps its photos for the postcard`() {
        val photos = PostcardPhotoUtils.buildPhotos(Diary(photoUris = photoUris, isEncrypt = false), applicationDataDirectory)

        assertEquals(photoUris.size, photos.size)
        assertTrue(photos.none { it.isEncrypt })
        assertEquals("$applicationDataDirectory$photoDirectory/1c9b5b2e-aaaa", photos[0].filePath)
        assertEquals("$applicationDataDirectory$photoDirectory/4f7a0c11-bbbb", photos[1].filePath)    }

    @Test
    fun `encrypted diary hides its photos in the postcard`() {
        val photos = PostcardPhotoUtils.buildPhotos(Diary(photoUris = photoUris, isEncrypt = true), applicationDataDirectory)

        assertEquals(photoUris.size, photos.size)
        assertTrue(photos.all { it.isEncrypt })
        assertTrue(photos.all { it.filePath.isEmpty() })
    }

    @Test
    fun `decrypted diary shows its photos in the postcard again`() {
        val diary = Diary(photoUris = photoUris, isEncrypt = true).copy(isEncrypt = false)

        val photos = PostcardPhotoUtils.buildPhotos(diary, applicationDataDirectory)

        assertTrue(photos.none { it.isEncrypt })
        assertFalse(photos.first().filePath.isEmpty())
    }

    @Test
    fun `diary without photos adds no photo to the postcard`() {
        assertEquals(emptyList<PostcardPhotoUtils.PostcardPhoto>(), PostcardPhotoUtils.buildPhotos(Diary(), applicationDataDirectory))
    }
}
