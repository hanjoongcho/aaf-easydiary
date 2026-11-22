package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.dateAnchorTimeMillis
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Diaries of the same day in earlier years, the 'On This Day' section of the dashboard (issue #140).
 *
 * The section answers 'what did I write on this day in the past years', so it keeps the diaries whose
 * month and day equal today's and whose year is earlier than the current one. A diary of this year is
 * not repeated here, and a diary of another day is not shown at all.
 *
 * The day a diary belongs to is the one reported on screen, taken from [Diary.dateAnchorTimeMillis]:
 * the stored date string for an 'All day' diary (issue #214) and the written moment otherwise, read in
 * the device time zone. The order is the nearest past year first, so the year right above today's comes
 * first and the oldest year last.
 */
object OnThisDayUtils {
    fun diariesOfPastYearsOn(
        diaries: List<Diary>,
        today: LocalDate = LocalDate.now(),
    ): List<Diary> =
        diaries
            .mapNotNull { diary ->
                dateOf(diary)?.let { date -> date to diary }
            }.filter { (date, _) ->
                date.monthValue == today.monthValue &&
                    date.dayOfMonth == today.dayOfMonth &&
                    date.year < today.year
            }.sortedWith(
                // Two diaries of the same day are ordered by their written moment, latest first.
                compareByDescending<Pair<LocalDate, Diary>> { it.first }
                    .thenByDescending { it.second.dateAnchorTimeMillis() },
            ).map { (_, diary) -> diary }

    private fun dateOf(diary: Diary): LocalDate? =
        runCatching {
            Instant
                .ofEpochMilli(diary.dateAnchorTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.getOrNull()
}
