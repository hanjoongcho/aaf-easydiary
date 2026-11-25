package me.blog.korn123.easydiary.domain.model

/**
 * A row of the ranking with the share of the ranking's total it holds, as a whole percent (issue #270).
 *
 * [percent] is counted inside the ranking rather than inside every tag of the writer, so the slices of the chart
 * of a top ten add up to 100% and a slice can be read as "this much of what the top ten tags are".
 */
data class TagRankShare(
    val ranked: TagWithCount,
    val percent: Int,
)

/**
 * The ranking the statistics screen draws from the tags of the entries (issue #270).
 *
 * A tag no entry carries is left out: the tag manager lists those so the writer can find and delete the names
 * left behind, but a chart of what the entries are about has nothing to draw for them. The ranking is ordered by
 * the number of entries that carry a tag, and a tie by name without case, which is the order the tag screens list
 * their names in, so the same counts always come out in the same order instead of moving between two openings of
 * the screen.
 *
 * Nothing here touches the database or the framework: the DAO hands the counts over and the screen draws what
 * this returns, which keeps the rule testable on its own.
 */
object TagStatistics {
    /** How many tags a ranking shows, as the symbol chart next to it shows its own top ten. */
    const val TOP_RANK_COUNT = 10

    private val RANK_ORDER: Comparator<TagWithCount> =
        compareByDescending<TagWithCount> { it.diaryCount }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.tag.name }
            .thenBy { it.tag.tagId }

    /**
     * The [limit] tags of [tags] that the most entries carry, the one with the most first.
     *
     * Tags no entry carries are dropped, so the result is empty when the writer has not put a tag on an entry
     * yet and the screen can say so instead of drawing an empty ring.
     */
    fun topRanked(
        tags: List<TagWithCount>,
        limit: Int = TOP_RANK_COUNT,
    ): List<TagWithCount> =
        tags.asSequence()
            .filter { it.diaryCount > 0 }
            .sortedWith(RANK_ORDER)
            .take(limit.coerceAtLeast(0))
            .toList()

    /**
     * The entries the tags of [ranked] carry together, i.e. what the chart in the middle of the ring states.
     *
     * An entry that carries two of the ranked tags adds one to both rows, so this is the number of taggings of
     * the ranking and not the number of entries the writer keeps.
     */
    fun totalCount(ranked: List<TagWithCount>): Int = ranked.sumOf { it.diaryCount }

    /**
     * The share each row of [ranked] holds of the ranking's total, as a whole percent that adds up to 100.
     *
     * Rounding every row on its own leaves a ring that stops short of 100% or passes it, so the percent of a row
     * is first cut down (`diaryCount` * 100 / total) and the percent points left over go to the rows with the
     * largest cut-off part, the way vote counts are turned into seats. A row keeps the ranking's order when two
     * rows are cut off by the same part, so the same counts always give the same percents.
     *
     * A ranking with no counted row has no share to state, so the result is empty.
     */
    fun shares(ranked: List<TagWithCount>): List<TagRankShare> {
        val total = totalCount(ranked)
        if (total <= 0) return emptyList()

        val floors = ranked.map { row -> (row.diaryCount.toLong() * PERCENT / total).toInt() }
        val remainders = ranked.map { row -> row.diaryCount.toLong() * PERCENT % total }
        val pointsLeft = PERCENT - floors.sum()
        val byRemainder =
            ranked.indices.sortedWith(
                compareByDescending<Int> { remainders[it] }.thenBy { it },
            )
        val percents = floors.toMutableList()
        byRemainder.take(pointsLeft.coerceIn(0, ranked.size)).forEach { index -> percents[index] += 1 }

        return ranked.mapIndexed { index, row -> TagRankShare(ranked = row, percent = percents[index]) }
    }

    private const val PERCENT = 100
}
