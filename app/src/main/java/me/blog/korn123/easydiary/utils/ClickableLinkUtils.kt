package me.blog.korn123.easydiary.utils

import java.net.URI
import java.net.URISyntaxException

/**
 * Where a link written in a diary has to open.
 */
sealed class DiaryLinkTarget {
    /** Another diary of this app, opened inside the reading screen. */
    data class Diary(val sequence: Long) : DiaryLinkTarget()

    /** A phone number, handed to the dialer so the user still decides to call. */
    data class Phone(val number: String) : DiaryLinkTarget()

    /** A web page, opened in a browser. */
    data class Web(val url: String) : DiaryLinkTarget()

    /** Any other address, opened by whichever app the system finds for it. */
    data class External(val address: String) : DiaryLinkTarget()
}

/**
 * Rules behind the clickable URL and phone number support (#167).
 *
 * The rules are kept as pure Kotlin so they can be unit tested without the Android
 * framework; the views and intents built from them live in the extensions.
 */
object ClickableLinkUtils {
    private const val SCHEME_TEL = "tel"
    private const val SCHEME_HTTP = "http"
    private const val SCHEME_HTTPS = "https"

    // 2021-10-23, 2021. 10. 23 or 2021/10/23 at the start of a match.
    private val DATE_LIKE = Regex("""^\d{4}\s*[-./]\s*\d{1,2}\s*[-./]\s*\d{1,2}""")

    // 05:14 or 05:14:33
    private val TIME_LIKE = Regex("""^\d{1,2}:\d{2}(:\d{2})?$""")

    /**
     * Whether the contents of a diary may be turned into clickable links.
     *
     * Encrypted contents stay cipher text until the user unlocks the diary, and a card in
     * a list is tapped as a whole to open the reading screen, so link spans would only
     * fight that tap. Both are therefore left plain.
     */
    fun shouldApplyTextLinks(
        isEncrypt: Boolean,
        isRecyclerItem: Boolean,
    ): Boolean = !isEncrypt && !isRecyclerItem

    /**
     * Resolves a link of a diary into the app that has to open it.
     * Returns null for a blank link.
     */
    fun resolveTarget(link: String?): DiaryLinkTarget? {
        val address = link?.trim().orEmpty()
        if (address.isEmpty()) return null

        DiaryLinkUtils.parseDiarySequence(address)?.let { sequence -> return DiaryLinkTarget.Diary(sequence) }

        return when (schemeOf(address)) {
            SCHEME_TEL -> {
                val number = address.substringAfter(':').trimStart('/').trim()
                if (number.isEmpty()) null else DiaryLinkTarget.Phone(number)
            }

            SCHEME_HTTP, SCHEME_HTTPS -> DiaryLinkTarget.Web(address)
            else -> DiaryLinkTarget.External(address)
        }
    }

    /**
     * A date or a time also looks like a phone number to Linkify, which only counts the
     * digits of a match. Such a match is dropped so that 2021-10-23 does not become a
     * number to call.
     */
    fun isDateOrTimeLike(candidate: CharSequence?): Boolean {
        val text = candidate?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return false
        return DATE_LIKE.containsMatchIn(text) || TIME_LIKE.matches(text)
    }

    private fun schemeOf(address: String): String? =
        try {
            URI(address).scheme?.lowercase()
        } catch (_: URISyntaxException) {
            // Some addresses such as "tel:02 123 4567" are not valid URIs, so the part
            // in front of the colon is read as the scheme instead.
            address.substringBefore(':', "").trim().lowercase().ifEmpty { null }
        }
}
