package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.EMPTY

/**
 * Builds the photo list of the postcard screen.
 *
 * A diary looks the same everywhere else in the app: an encrypted entry keeps the number of its
 * attached photos but not their content (`Diary.photoUrisWithEncryptionPolicy()`), so the reading
 * screen, the diary cards and the photo viewer show padlock placeholders instead of the pictures.
 * The postcard screen is built from the same policy, here, so a photo of an encrypted diary is
 * never handed to the postcard layout (and therefore never exported as a postcard image).
 */
object PostcardPhotoUtils {
    data class PostcardPhoto(
        val filePath: String,
        val isEncrypt: Boolean,
    )

    fun buildPhotos(
        diary: Diary,
        applicationDataDirectory: String,
    ): List<PostcardPhoto> =
        diary.photoUrisWithEncryptionPolicy()
            ?.map { photoUri ->
                when {
                    photoUri.photoUri.isNullOrEmpty() -> PostcardPhoto(String.EMPTY, true)
                    else -> PostcardPhoto("$applicationDataDirectory${photoUri.getFilePath()}", false)
                }
            } ?: emptyList()
}
