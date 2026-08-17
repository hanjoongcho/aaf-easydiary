package me.blog.korn123.easydiary.domain.model

import me.blog.korn123.easydiary.helper.CONTENT_URI_PREFIX
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils

data class PhotoUri(
    val photoUri: String? = null,
    val mimeType: String? = null,
) {
    fun isContentUri(): Boolean = StringUtils.startsWith(photoUri, CONTENT_URI_PREFIX)

    fun getFilePath(): String = "$DIARY_PHOTO_DIRECTORY${FilenameUtils.getBaseName(photoUri)}"

    fun isEncrypt(): Boolean = photoUri?.isEmpty() ?: false
}
