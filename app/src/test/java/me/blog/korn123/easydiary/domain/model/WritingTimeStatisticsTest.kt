package me.blog.korn123.easydiary.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The writing time statistics of the statistics screen.
 *
 * The chart is a ring of the day, so the tests pin the two things its reader relies on: the eight spans always come
 * out in the order of the clock whatever the counts are, and their percents add up to 100 so the ring closes. They
 * pin the edges as well: midnight belongs to the last span, a span no entry falls in keeps its place at zero, and an
 * hour outside the day is still counted somewhere instead of being dropped from the total.
 */
class WritingTimeStatisticsTest {
    @Test
    fun `the eight spans always come out in the order of the clock`() {
        val shares = WritingTimeStatistics.shares(listOf(23, 22, 5, 2))

        assertEquals(WritingTimeRange.entries.toList(), shares.map { it.range })
    }

    @Test
    fun `an entry is counted in the span that holds its hour`() {
        val counts =
            WritingTimeStatistics
                .shares(listOf(0, 1, 3, 4, 6, 7, 9, 10, 12, 13, 15, 16, 18, 19, 21, 22, 23))
                .associate { share -> share.range to share.diaryCount }

        assertEquals(2, counts[WritingTimeRange.HOUR_1_TO_3]) // 1 and 3
        assertEquals(2, counts[WritingTimeRange.HOUR_4_TO_6]) // 4 and 6
        assertEquals(2, counts[WritingTimeRange.HOUR_7_TO_9]) // 7 and 9
        assertEquals(2, counts[WritingTimeRange.HOUR_10_TO_12]) // 10 and 12
        assertEquals(2, counts[WritingTimeRange.HOUR_13_TO_15]) // 13 and 15
        assertEquals(2, counts[WritingTimeRange.HOUR_16_TO_18]) // 16 and 18
        assertEquals(2, counts[WritingTimeRange.HOUR_19_TO_21]) // 19 and 21
        assertEquals(3, counts[WritingTimeRange.HOUR_22_TO_0]) // 22, 23 and midnight
    }

    @Test
    fun `midnight is counted with the hours before it`() {
        assertEquals(WritingTimeRange.HOUR_22_TO_0, WritingTimeStatistics.rangeOf(0))
        assertEquals(WritingTimeRange.HOUR_22_TO_0, WritingTimeStatistics.rangeOf(23))
        assertEquals(WritingTimeRange.HOUR_1_TO_3, WritingTimeStatistics.rangeOf(1))
        assertEquals(WritingTimeRange.HOUR_19_TO_21, WritingTimeStatistics.rangeOf(21))
    }

    @Test
    fun `the percents of the spans add up to 100`() {
        val hours = listOf(1, 1, 2, 8, 9, 9, 9, 14, 20, 20, 23)

        val percents = WritingTimeStatistics.shares(hours).map { share -> share.percent }

        assertEquals(100, percents.sum())
    }

    @Test
    fun `a percent left over goes to the span with the largest cut-off part`() {
        // Three entries in three spans: 33.3% each, so the point left over goes to the earliest span of the day.
        val percents = WritingTimeStatistics.shares(listOf(2, 8, 14)).map { share -> share.percent }

        assertEquals(listOf(34, 33, 33, 0, 0, 0, 0, 0), percents)
    }

    @Test
    fun `a span no entry falls in keeps its place at zero`() {
        val shares = WritingTimeStatistics.shares(listOf(1, 1, 1, 1))

        assertEquals(8, shares.size)
        assertEquals(4, shares[WritingTimeRange.HOUR_1_TO_3.ordinal].diaryCount)
        assertEquals(100, shares[WritingTimeRange.HOUR_1_TO_3.ordinal].percent)
        assertEquals(List(7) { 0 }, shares.drop(1).map { share -> share.diaryCount })
        assertEquals(List(7) { 0 }, shares.drop(1).map { share -> share.percent })
    }

    @Test
    fun `the shares of an empty day are the eight spans at zero`() {
        val shares = WritingTimeStatistics.shares(emptyList())

        assertEquals(8, shares.size)
        assertEquals(0, shares.sumOf { share -> share.diaryCount })
        assertEquals(0, shares.sumOf { share -> share.percent })
    }

    @Test
    fun `an hour outside the day is counted with the last span`() {
        assertEquals(WritingTimeRange.HOUR_22_TO_0, WritingTimeStatistics.rangeOf(24))
        assertEquals(WritingTimeRange.HOUR_22_TO_0, WritingTimeStatistics.rangeOf(-1))
    }

    @Test
    fun `the same counts always give the same shares`() {
        val hours = listOf(3, 9, 9, 15, 21, 22, 0)

        assertEquals(WritingTimeStatistics.shares(hours), WritingTimeStatistics.shares(hours.reversed()))
    }
}
