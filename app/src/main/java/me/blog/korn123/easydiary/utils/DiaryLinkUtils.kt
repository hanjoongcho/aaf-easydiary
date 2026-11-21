package me.blog.korn123.easydiary.utils

import android.net.Uri
import me.blog.korn123.easydiary.helper.DiaryLinkConstants
import java.net.URI
import java.net.URISyntaxException

/**
 * A diary deep link written in the text of a diary, for example easydiary://diary/221.
 *
 * [start] is inclusive and [end] is exclusive, so the link is [link] itself and
 * `text.subSequence(start, end)` reads as [link]. [link] is the normalised form of the
 * address, which makes a link written in another case open the same diary.
 */
data class DiaryLinkRange(
    val start: Int,
    val end: Int,
    val link: String,
)

/**
 * Builds and parses the diary deep link. A link holds only the diary sequence,
 * for example easydiary://diary/221
 */
object DiaryLinkUtils {
    // The URI_PREFIX itself carries no regular expression character, and the sequence is a run
    // of digits, so a trailing punctuation such as ')' or '.' stays outside the link.
    private val DIARY_LINK =
        Regex(
            "(?i)${DiaryLinkConstants.SCHEME}://${DiaryLinkConstants.HOST_DIARY}/\\d+",
        )

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

    /**
     * Finds every diary link written in [text].
     *
     * A text view does not know this scheme, so its own linking rules read only the sequence
     * as a phone number. The ranges are handed to the views so that they can link the whole
     * address instead (#268).
     */
    fun findDiaryLinkRanges(text: CharSequence?): List<DiaryLinkRange> {
        if (text.isNullOrEmpty()) return emptyList()

        return DIARY_LINK.findAll(text).mapNotNull { match ->
            // A sequence that no diary can have, or one too large for a diary, is not a link
            // the app can open.
            val sequence =
                match.value
                    .substringAfterLast('/')
                    .toLongOrNull()
                    ?.takeIf { it > 0 }
                    ?: return@mapNotNull null
            DiaryLinkRange(
                start = match.range.first,
                end = match.range.last + 1,
                link = createDiaryLinkUrl(sequence),
            )
        }.toList()
    }
}
