package me.blog.korn123.easydiary.presentation.dday.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.darkenColor
import me.blog.korn123.easydiary.extensions.innerCardDarkenFactor
import me.blog.korn123.easydiary.presentation.components.SimpleText

/**
 * One card of the D-Day list of the dashboard.
 *
 * It is drawn the way `item_dday.xml` and `item_dday_add.xml` drew it inside the recycler: the same inner card of
 * the dashboard (the theme background a little darker, a 10dp corner, a 3dp shadow and a 3dp margin around it),
 * the same icon-tinted text color, the same font of the app and the same three lights — today orange, a day that
 * has passed red, a day still to come green — beside the remaining days. A tap opens the dialog of the fragment,
 * which adds a D-Day for the `ADD` card and changes or deletes one for a stored card.
 *
 * The content follows the minimum constraints the card is measured with: a card of the wrap mode is given the free
 * width of its line as a minimum ([DdayCardFlow]), and the box hands that width down to the content so the remaining
 * days of a stored D-Day end at the right edge of the card and not at the end of the text above them. A card of the
 * horizontal list has no minimum and keeps the width its content asks for.
 */
@Composable
fun DdayCard(
    item: DDayCardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val config = context.config
    val shape = RoundedCornerShape(CARD_CORNER_RADIUS)
    val cardColor = Color(config.backgroundColor.darkenColor(context.innerCardDarkenFactor()))
    val textColor = Color(config.textColor)

    Box(
        modifier =
            modifier
                // The margin the recycler item had around the compat padded card, and the room the shadow is
                // drawn in, so the cards of a line stay apart without a spacing of their own.
                .padding(CARD_MARGIN)
                .shadow(elevation = CARD_ELEVATION, shape = shape)
                .clip(shape)
                .background(cardColor)
                .clickable(onClick = onClick)
                .padding(if (item.isNewItem) NEW_ITEM_CONTENT_PADDING else CARD_CONTENT_PADDING),
        // Off, the content of a card widened by its line would keep the width it asks for and stay at the start of
        // the card, so `Modifier.align(Alignment.End)` of the remaining days would align to the text and not to the
        // card. On, the content is as wide as the card is, and a card with no minimum width is unchanged.
        propagateMinConstraints = true,
    ) {
        when (item.isNewItem) {
            true -> DdayAddLabel(textColor = textColor)
            false -> DdayContent(item = item, textColor = textColor)
        }
    }
}

/** The `ADD` card: one label, smaller padding and so a shorter card than a stored D-Day has. */
@Composable
private fun DdayAddLabel(textColor: Color) {
    val density = LocalDensity.current
    val textSize = with(density) { DDAY_TEXT_SIZE_SP.sp.toPx() }

    SimpleText(
        text = stringResource(R.string.dashboard_dday_add),
        fontSize = textSize,
        fontColor = textColor,
        maxLines = 1,
        lineSpacingScaleFactor = 1F,
    )
}

/**
 * The title, the target date and, under them on the right, the three lights with the remaining days.
 *
 * The title is the only line that may be cut short, and it is cut at the width the card got, so a long title of a
 * card that fills its line hides the end of the name instead of widening the card past the card of the dashboard.
 *
 * The three lines are set apart by one spacing of their own. The padding of the title is left to the sides only, so
 * it does not add to the spacing above and below it and the three lines of the card stay evenly spaced; the card
 * itself gives the room above the title and below the remaining days.
 */
@Composable
private fun DdayContent(
    item: DDayCardItem,
    textColor: Color,
) {
    val density = LocalDensity.current
    val textSize = with(density) { DDAY_TEXT_SIZE_SP.sp.toPx() }
    val titlePadding = (DDAY_TITLE_PADDING_SP * density.fontScale).dp

    Column(verticalArrangement = Arrangement.spacedBy(DDAY_LINE_SPACING)) {
        SimpleText(
            text = item.title,
            modifier = Modifier.padding(horizontal = titlePadding),
            fontSize = textSize,
            fontColor = textColor,
            maxLines = 1,
            lineSpacingScaleFactor = 1F,
        )
        SimpleText(
            text = item.targetDate,
            fontSize = textSize,
            fontColor = textColor,
            maxLines = 1,
            lineSpacingScaleFactor = 1F,
        )
        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DdayLight(color = LIGHT_RED, borderColor = LIGHT_RED_BORDER, isLit = item.highlight == DDayHighlight.ELAPSED, endPadding = 3.dp)
            DdayLight(color = LIGHT_ORANGE, borderColor = LIGHT_ORANGE_BORDER, isLit = item.highlight == DDayHighlight.TODAY, endPadding = 3.dp)
            DdayLight(color = LIGHT_GREEN, borderColor = LIGHT_GREEN_BORDER, isLit = item.highlight == DDayHighlight.UPCOMING, endPadding = 5.dp)
            SimpleText(
                text = item.dayRemaining,
                fontSize = textSize,
                fontWeight = FontWeight.Bold,
                fontColor = textColor,
                maxLines = 1,
                lineSpacingScaleFactor = 1F,
            )
        }
    }
}

/** One of the three lights: the color of its state when it is lit, and a faint dot of it when it is not. */
@Composable
private fun DdayLight(
    color: Color,
    borderColor: Color,
    isLit: Boolean,
    endPadding: Dp,
) {
    Box(
        modifier =
            Modifier
                .padding(end = endPadding)
                .size(DDAY_LIGHT_SIZE)
                .alpha(if (isLit) LIGHT_LIT_ALPHA else LIGHT_UNLIT_ALPHA)
                .background(color = color, shape = CircleShape)
                .border(width = LIGHT_BORDER_WIDTH, color = borderColor, shape = CircleShape),
    )
}

/** The inner card of `card_dashboard_inner`, the card the D-Day list has always been drawn in. */
private val CARD_CORNER_RADIUS = 10.dp

/** `android:cardElevation` and the `MarginLayoutParams` margin of the recycler item, as `updateCardViewPolicy` set them. */
private val CARD_ELEVATION = 3.dp
private val CARD_MARGIN = 3.dp

/** `card_contents_padding`: the padding of a stored D-Day card. */
private val CARD_CONTENT_PADDING = PaddingValues(15.dp)

/** `dashboard_card_padding` left and right and the 5dp of the card style above and below: the padding of the `ADD` card. */
private val NEW_ITEM_CONTENT_PADDING = PaddingValues(horizontal = 10.dp, vertical = 5.dp)

/** `basic_text_medium`, and `diary_card_text_padding` of the title, taken as `applyGlobalSize` left them. */
private const val DDAY_TEXT_SIZE_SP = 15F
private const val DDAY_TITLE_PADDING_SP = 3F

/**
 * The room between the title, the target date and the remaining days of a card.
 *
 * The recycler item had only the 3sp padding of its title between the first two lines and nothing at all between the
 * date and the remaining days, which read as one block of text on the device. The lines are a little further apart
 * here, evenly, and this value is what moves them.
 */
private val DDAY_LINE_SPACING = 4.dp

private val DDAY_LIGHT_SIZE = 10.dp
private val LIGHT_BORDER_WIDTH = 1.dp
private const val LIGHT_LIT_ALPHA = 1F
private const val LIGHT_UNLIT_ALPHA = 0.1F

/** `bg_circle_red`, `bg_circle_orange` and `bg_circle_green`. */
private val LIGHT_RED = Color(0xFFEF0808)
private val LIGHT_RED_BORDER = Color(0xFFFF0000)
private val LIGHT_ORANGE = Color(0xFFF46607)
private val LIGHT_ORANGE_BORDER = Color(0xFFF4840C)
private val LIGHT_GREEN = Color(0xFF098E09)
private val LIGHT_GREEN_BORDER = Color(0xFF008000)
