package me.blog.korn123.easydiary.utils

import android.net.Uri
import me.blog.korn123.easydiary.helper.DiaryLinkConstants
import java.net.URI
import java.net.URISyntaxException

/**
 * Builds and parses the diary deep link. A link holds only the diary sequence,
 * for example easydiary://diary/221
 */
object DiaryLinkUtils {
    fun createDiaryLinkUrl(sequence: Long): String = "${DiaryLinkConstants.URI_PREFIX}$sequence"

    fun parseDiarySequence(uri: Uri?): Long? = uri?.let { parseDiarySequence(it.toString()) }

    fun parseDiarySequence(link: String?): Long? {
        val address = link?.trim().orEmpty()
        if (address.isEmpty()) return null

        val uri =
            try {
                URI(address)
            } catch (_: URISyntaxException) {
                return null
            }

        if (!uri.scheme.equals(DiaryLinkConstants.SCHEME, ignoreCase = true)) return null
        if (!(uri.host ?: uri.authority).equals(DiaryLinkConstants.HOST_DIARY, ignoreCase = true)) return null

        return uri.path?.trim('/')?.toLongOrNull()?.takeIf { it > 0 }
    }
}
