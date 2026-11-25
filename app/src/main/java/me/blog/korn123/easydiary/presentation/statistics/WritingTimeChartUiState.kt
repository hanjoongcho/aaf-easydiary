package me.blog.korn123.easydiary.presentation.statistics

import me.blog.korn123.easydiary.domain.model.WritingTimeShare

/**
 * What the writing time chart draws: the eight spans of the day with the entries written in them, in the order of
 * the clock, and whether the entries are counted yet.
 *
 * The spans are always the eight of [me.blog.korn123.easydiary.domain.model.WritingTimeRange], so an empty list
 * only means the chart has not been given its counts yet.
 */
data class WritingTimeChartUiState(
    val shares: List<WritingTimeShare> = emptyList(),
    val isLoading: Boolean = true,
)
