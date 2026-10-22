package me.blog.korn123.easydiary.domain.model

import me.blog.korn123.easydiary.extensions.getBaseName
import me.blog.korn123.easydiary.helper.CONTENT_URI_PREFIX
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY

data class PhotoUri(
    val photoUri: String? = null,
    val mimeType: String? = null,
) {
    fun isContentUri(): Boolean = photoUri?.startsWith(CONTENT_URI_PREFIX) == true

    fun getFilePath(): String = "$DIARY_PHOTO_DIRECTORY${photoUri?.getBaseName()}"

    fun isEncrypt(): Boolean = photoUri?.isEmpty() ?: false
}
