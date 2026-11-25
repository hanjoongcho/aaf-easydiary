package me.blog.korn123.easydiary.domain.model

/**
 * The eight spans of the day the writing time statistics counts entries in, always in the order of the clock.
 *
 * The spans follow the labels of the chart (`range_a` to `range_h`), so they are named by the hours they hold
 * rather than by a rank: a span keeps its place in the chart whatever the writer's habits are, which is what lets
 * the ring be read as a day going round from one o'clock to midnight. The last span takes midnight with it, so
 * every hour of the day belongs to exactly one span.
 */
enum class WritingTimeRange(
    val firstHour: Int,
    val lastHour: Int,
) {
    HOUR_1_TO_3(1, 3),
    HOUR_4_TO_6(4, 6),
    HOUR_7_TO_9(7, 9),
    HOUR_10_TO_12(10, 12),
    HOUR_13_TO_15(13, 15),
    HOUR_16_TO_18(16, 18),
    HOUR_19_TO_21(19, 21),
    HOUR_22_TO_0(22, 24),
    ;

    /** Whether an entry written at [hour] of the day (0 to 23) is counted in this span. */
    fun holds(hour: Int): Boolean = if (this == HOUR_22_TO_0) hour >= firstHour || hour == MIDNIGHT else hour in firstHour..lastHour

    private companion object {
        const val MIDNIGHT = 0
    }
}

/**
 * A span of the writing time chart with the entries it holds and its share of the whole, as a whole percent.
 *
 * A span no entry falls in stays in the list with a count and a percent of zero, so the chart states the hours the
 * writer does not write at instead of hiding them and letting the ring start elsewhere.
 */
data class WritingTimeShare(
    val range: WritingTimeRange,
    val diaryCount: Int,
    val percent: Int,
)

/**
 * The entries counted by the hour they were written at, for the writing time chart of the statistics screen.
 *
 * The chart has eight spans that never move, so the result always holds the eight spans of [WritingTimeRange] in
 * the order of the clock. The percents add up alike, so the ring closes: a span's percent is cut down and the
 * percent points left over go to the spans with the largest cut-off part, as the tag ranking does it, which keeps
 * a span of zero entries at zero.
 *
 * Nothing here touches the database or the framework: the caller hands the hours of the entries over, which keeps
 * the counting testable on its own.
 */
object WritingTimeStatistics {
    /** The spans of the chart, in the order they are drawn from one o'clock round to midnight. */
    val ranges: List<WritingTimeRange> = WritingTimeRange.entries

    /**
     * The span an entry written at [hour] belongs to.
     *
     * An hour outside the day is counted in the last span, the way the bar chart this chart replaces counted it,
     * so a caller that could not read an hour still counts its entry somewhere instead of losing it.
     */
    fun rangeOf(hour: Int): WritingTimeRange = ranges.firstOrNull { it.holds(hour) } ?: WritingTimeRange.HOUR_22_TO_0

    /**
     * The eight spans of the chart with the entries of [hours] counted in them and their share of those entries.
     *
     * [hours] holds the hour of the day of every entry to count, so its size is the total the chart states in the
     * middle of the ring. An empty [hours] gives the eight spans at zero, which the chart shows as its empty state.
     */
    fun shares(hours: List<Int>): List<WritingTimeShare> {
        val counts = ranges.associateWith { 0 }.toMutableMap()
        hours.forEach { hour ->
            val range = rangeOf(hour)
            counts[range] = (counts[range] ?: 0) + 1
        }

        val percents = largestRemainderPercents(ranges.map { range -> counts[range] ?: 0 }, hours.size)
        return ranges.mapIndexed { index, range -> WritingTimeShare(range = range, diaryCount = counts[range] ?: 0, percent = percents[index]) }
    }

    /**
     * The whole percent of each count in [counts] out of [total], adding up to 100.
     *
     * Every count is first cut down (`count` * 100 / total) and the percent points left over go to the counts with
     * the largest cut-off part, a tie going to the span earlier in the day, so the same counts always give the same
     * percents. A count of zero has no cut-off part and never receives a point, which keeps a span no entry falls
     * in at zero while the others close the ring.
     */
    private fun largestRemainderPercents(
        counts: List<Int>,
        total: Int,
    ): List<Int> {
        if (total <= 0) return counts.map { 0 }

        val floors = counts.map { count -> (count.toLong() * PERCENT / total).toInt() }
        val remainders = counts.map { count -> count.toLong() * PERCENT % total }
        val pointsLeft = PERCENT - floors.sum()
        val byRemainder =
            counts.indices.sortedWith(
                compareByDescending<Int> { remainders[it] }.thenBy { it },
            )
        val percents = floors.toMutableList()
        byRemainder.take(pointsLeft.coerceIn(0, counts.size)).forEach { index -> percents[index] += 1 }
        return percents
    }

    private const val PERCENT = 100
}
