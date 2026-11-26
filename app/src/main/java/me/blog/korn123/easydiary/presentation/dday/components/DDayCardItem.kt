package me.blog.korn123.easydiary.presentation.dday.components

import me.blog.korn123.easydiary.domain.model.DDay

/** Which of the three lights of a D-Day card is lit: today, a day that has passed, or a day still to come. */
enum class DDayHighlight { TODAY, ELAPSED, UPCOMING }

/**
 * One card of the dashboard's D-Day list: a stored D-Day with everything the card writes on it, or one of the two
 * `New D-Day` cards that only open the add dialog.
 *
 * The texts are made when the list is built, not while the card is drawn, so a card that is drawn again (a
 * recomposition, a rotation of the screen) keeps the D-Day it was built with and a list rebuilt by `onResume`
 * picks up the new day.
 */
data class DDayCardItem(
    val dDay: DDay,
    val isNewItem: Boolean,
    val title: String,
    val targetDate: String,
    val dayRemaining: String,
    val highlight: DDayHighlight,
) {
    /** Every card but the two `New D-Day` ones is a stored D-Day. */
    val isStored: Boolean get() = !isNewItem
}
