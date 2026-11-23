package me.blog.korn123.easydiary.domain.model

import java.util.Locale

/**
 * A label the writer puts on their entries (issue #270).
 *
 * A diary can carry several tags and one tag can be carried by several diaries, so the tag itself holds
 * nothing but its name; the attachment between a tag and an entry is [DiaryTagLink].
 *
 * @property tagId id of the stored tag. A tag picked in the editor but not saved yet keeps 0 until the
 * entry is written.
 * @property name the name as the writer typed it, cleaned by [TagRules.displayName].
 */
data class Tag(
    val tagId: Long = 0L,
    val name: String = "",
) {
    /** What the screens show: the name with the tag marker in front, e.g. `#travel`. */
    val label: String
        get() = TagRules.label(name)
}

/**
 * One attachment of a tag to an entry. Restoring a backup keeps both ids, so the link can be written
 * without looking the tag up again.
 */
data class DiaryTagLink(
    val diaryId: Long = 0L,
    val tagId: Long = 0L,
)

/**
 * The single place that decides what a tag name is, so the editor, the picker, the filter bar and the
 * stored rows all agree on when two names mean the same tag.
 *
 * Names are compared case insensitively and ignore surrounding and repeated whitespace: `' Trip '`,
 * `'trip'` and `'TRIP'` are one tag, stored once and shown with the spelling of the first name entered.
 */
object TagRules {
    /** Longer names are cut, which keeps a chip readable and the stored name searchable. */
    const val MAX_NAME_LENGTH = 24

    private val WHITESPACE = Regex("\\s+")

    /** The name as it is stored and shown: trimmed, inner whitespace collapsed, length limited. */
    fun displayName(raw: String): String = raw.trim().replace(WHITESPACE, " ").take(MAX_NAME_LENGTH)

    /** The key two names are compared by, i.e. the unique index of the `tags` table. */
    fun normalize(raw: String): String = displayName(raw).lowercase(Locale.ROOT)

    /** The display name, or null when nothing but whitespace was entered. */
    fun sanitize(raw: String): String? = displayName(raw).ifEmpty { null }

    /**
     * The names worth storing from [rawNames]: cleaned, dropped when empty and deduplicated the same way
     * [normalize] compares them, keeping the first spelling and the order the writer entered them in.
     */
    fun sanitizeAll(rawNames: List<String>): List<String> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<String>()
        rawNames.forEach { raw ->
            sanitize(raw)?.let { name ->
                if (seen.add(normalize(name))) result.add(name)
            }
        }
        return result
    }

    /** True when [name] holds [query], the test the tag picker filters its list with. */
    fun containsQuery(
        name: String,
        query: String,
    ): Boolean {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return true
        return normalize(name).contains(normalizedQuery)
    }

    /** True when [query] already names one of [names], so the picker offers no `+ 'query'` row. */
    fun matchesAny(
        query: String,
        names: Collection<String>,
    ): Boolean {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return true
        return names.any { normalize(it) == normalizedQuery }
    }

    /** The text of a chip, e.g. `#travel`. */
    fun label(name: String): String = "#$name"
}
