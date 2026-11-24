package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.dateAnchorTimeMillis
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * How the dashboard shows the writing streak of issue #88.
 */
enum class StreakStatus {
    /** An entry of today exists, so the chain counts today. */
    WRITTEN_TODAY,

    /** Today has no entry yet and yesterday has one: writing today extends the chain. */
    DUE_TODAY,

    /** Two day rule only: yesterday was skipped, so today is the last chance to keep the chain. */
    AT_RISK_TWO_DAY,

    /** The chain is gone. Today starts a new one with a first entry. */
    BROKEN,
}

/** One square of the streak heatmap: the day, how many entries it holds, and whether it has happened yet. */
data class StreakHeatmapDay(
    val date: LocalDate,
    val count: Int,
    val isFuture: Boolean = false,
)

/**
 * What the dashboard streak card draws.
 *
 * [currentStreak] is the number the card puts next to the flame, [longestStreak] the best the writer has ever
 * done, and [streakIfWrittenToday] the number the badge promises when the chain is still alive but today has
 * no entry yet.
 */
data class StreakSummary(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val status: StreakStatus = StreakStatus.BROKEN,
    val streakIfWrittenToday: Int = 1,
    /** The heatmap columns, oldest first; each column holds the seven days of a week, Sunday first. */
    val heatmap: List<List<StreakHeatmapDay>> = emptyList(),
)

/**
 * The writing streak of the dashboard (issue #88): "Don't Break the Chain" and its looser form, the
 * "Two Day Rule".
 *
 * Nothing is stored for this: the streak is read from the days the entries were written on, so an entry the
 * writer edits or deletes moves the numbers on the next look without a database change. The day of an entry is
 * the one it was written on ([Diary.dateAnchorTimeMillis] read in the device time zone), the same day the rest
 * of the app shows it under, and a day counts once however many entries carry it.
 *
 * **Don't Break the Chain** (the default) keeps a chain of consecutive days with an entry. A single day
 * without an entry ends it, and today does not have to be written yet for the chain to still stand until the
 * day is over: a chain whose last entry is yesterday is still alive and today extends it.
 *
 * **Two Day Rule** allows one missed day between entries. The chain stands as long as no two days in a row
 * pass without an entry, so entries of the 1st and the 3rd of a month belong to one chain.
 *
 * Both rules count the same thing: the days the chain holds an entry. A chain of the 1st, the 3rd and the 4th
 * is three days under the two day rule, because those are the three days an entry was written, and the
 * skipped 2nd is only the day the rule allowed to pass. Counting the entries rather than the days that have
 * passed keeps the current number and the best number of the card on the same scale, and a number can always
 * be checked against the green squares of the heatmap.
 *
 * Entries dated after today do not count; a day that has not come cannot extend a chain.
 */
object StreakUtils {
    /** How many weeks the heatmap grid covers: 16 columns of seven days, the current week last. */
    const val HEATMAP_WEEKS = 16

    /** The Sunday a week starts on in the grid, as [LocalDate.dayOfWeek] counts it (`DayOfWeek.SUNDAY.value`). */
    private const val DAYS_IN_WEEK = 7

    /** A gap of two days between two entries is exactly one skipped day, which the two day rule allows. */
    private const val TWO_DAY_RULE_MAX_GAP_DAYS = 2L

    /** A gap of one day is two entries on days next to each other. */
    private const val DAILY_MAX_GAP_DAYS = 1L

    fun calculate(
        diaries: List<Diary>,
        today: LocalDate = LocalDate.now(),
        isTwoDayRule: Boolean = false,
        zoneId: ZoneId = ZoneId.systemDefault(),
        heatmapWeeks: Int = HEATMAP_WEEKS,
    ): StreakSummary {
        val counts = writtenCountsByDate(diaries, today, zoneId)
        val writtenDates = counts.keys.sorted()
        val current = currentStreak(writtenDates, today, isTwoDayRule)

        return StreakSummary(
            currentStreak = current.days,
            longestStreak = longestStreak(writtenDates, isTwoDayRule),
            status = current.status,
            streakIfWrittenToday = current.streakIfWrittenToday,
            heatmap = heatmap(counts, today, heatmapWeeks),
        )
    }

    /** The number of entries of every day up to [today]; a later day cannot be part of a chain yet. */
    private fun writtenCountsByDate(
        diaries: List<Diary>,
        today: LocalDate,
        zoneId: ZoneId,
    ): Map<LocalDate, Int> =
        diaries
            .mapNotNull { diary -> dateOf(diary, zoneId) }
            .filter { date -> !date.isAfter(today) }
            .groupingBy { date -> date }
            .eachCount()

    private fun dateOf(
        diary: Diary,
        zoneId: ZoneId,
    ): LocalDate? =
        runCatching {
            Instant
                .ofEpochMilli(diary.dateAnchorTimeMillis())
                .atZone(zoneId)
                .toLocalDate()
        }.getOrNull()

    /**
     * The chain that reaches today, and how the card should describe it.
     *
     * The chain is alive when the latest entry is today, yesterday, or - under the two day rule - the day
     * before yesterday, which means yesterday was the one skipped day the rule allows. The days it holds are
     * the entries of that chain, so a chain the rule kept across a skipped day counts the entries alone.
     */
    private fun currentStreak(
        writtenDates: List<LocalDate>,
        today: LocalDate,
        isTwoDayRule: Boolean,
    ): CurrentStreak {
        val latestIndex = writtenDates.indexOfLast { date -> !date.isAfter(today) }
        if (latestIndex < 0) return CurrentStreak(days = 0, status = StreakStatus.BROKEN)

        val latest = writtenDates[latestIndex]
        val daysSinceLatest = ChronoUnit.DAYS.between(latest, today)
        val allowedGap = if (isTwoDayRule) TWO_DAY_RULE_MAX_GAP_DAYS else DAILY_MAX_GAP_DAYS
        if (daysSinceLatest > allowedGap) return CurrentStreak(days = 0, status = StreakStatus.BROKEN)

        val status =
            when (daysSinceLatest) {
                0L -> StreakStatus.WRITTEN_TODAY
                1L -> StreakStatus.DUE_TODAY
                // Only the two day rule lets the chain survive to here: yesterday was the skipped day.
                else -> StreakStatus.AT_RISK_TWO_DAY
            }
        val startIndex = chainStartIndex(writtenDates, latestIndex, allowedGap)

        // The days the chain holds an entry: today when today is written, the entries before it otherwise.
        val chainDays = latestIndex - startIndex + 1

        return CurrentStreak(
            days = chainDays,
            status = status,
            streakIfWrittenToday = if (status == StreakStatus.WRITTEN_TODAY) chainDays else chainDays + 1,
        )
    }

    /** The index of the earliest entry of the chain that ends at `writtenDates[latestIndex]`. */
    private fun chainStartIndex(
        writtenDates: List<LocalDate>,
        latestIndex: Int,
        allowedGap: Long,
    ): Int {
        var startIndex = latestIndex
        while (startIndex > 0 &&
            ChronoUnit.DAYS.between(writtenDates[startIndex - 1], writtenDates[startIndex]) <= allowedGap
        ) {
            startIndex--
        }
        return startIndex
    }

    /** The longest chain of the whole history, counted the way the selected rule counts a chain. */
    private fun longestStreak(
        writtenDates: List<LocalDate>,
        isTwoDayRule: Boolean,
    ): Int {
        if (writtenDates.isEmpty()) return 0

        val allowedGap = if (isTwoDayRule) TWO_DAY_RULE_MAX_GAP_DAYS else DAILY_MAX_GAP_DAYS
        var longest = 1
        var chainDays = 1

        for (index in 1 until writtenDates.size) {
            val gap = ChronoUnit.DAYS.between(writtenDates[index - 1], writtenDates[index])
            chainDays = if (gap > allowedGap) 1 else chainDays + 1
            longest = maxOf(longest, chainDays)
        }

        return longest
    }

    /**
     * The grid of the recent weeks, oldest week first, so the card can draw the last column right beside
     * today. Every column holds the seven days of a week starting on Sunday, and the days of the current week
     * that have not come yet are marked as future so the card leaves them out.
     */
    private fun heatmap(
        counts: Map<LocalDate, Int>,
        today: LocalDate,
        weeks: Int,
    ): List<List<StreakHeatmapDay>> {
        val currentWeekStart = today.minusDays((today.dayOfWeek.value % DAYS_IN_WEEK).toLong())
        val firstWeekStart = currentWeekStart.minusWeeks((weeks - 1).toLong())

        return (0 until weeks).map { weekIndex ->
            val weekStart = firstWeekStart.plusWeeks(weekIndex.toLong())
            (0 until DAYS_IN_WEEK).map { dayIndex ->
                val date = weekStart.plusDays(dayIndex.toLong())
                val isFuture = date.isAfter(today)
                StreakHeatmapDay(
                    date = date,
                    count = if (isFuture) 0 else counts[date] ?: 0,
                    isFuture = isFuture,
                )
            }
        }
    }

    private data class CurrentStreak(
        val days: Int,
        val status: StreakStatus,
        val streakIfWrittenToday: Int = 1,
    )
}
