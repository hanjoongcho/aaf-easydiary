package me.blog.korn123.easydiary.presentation.streak.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.darkenColor
import me.blog.korn123.easydiary.extensions.innerCardDarkenFactor
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.streak.WritingStreakUiState
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.StreakHeatmapDay
import me.blog.korn123.easydiary.utils.StreakStatus
import me.blog.korn123.easydiary.utils.StreakSummary
import me.blog.korn123.easydiary.utils.StreakUtils
import java.time.LocalDate
import java.time.ZoneId

/**
 * The writing streak card of the dashboard (issue #88).
 *
 * The card answers two questions at a glance: how long the writer has kept writing without breaking the
 * chain, and whether today still needs an entry to keep it. The header carries the two rules the writer can
 * switch between, the numbers show the current and the best chain, the badge says what today means for the
 * chain, and the grid at the bottom is the chain itself, a square for every day of the recent weeks.
 *
 * The card is drawn inside the dashboard's own card, so it paints no background of its own and takes its
 * colors from the theme the rest of the app uses: the primary color marks the days with an entry and the
 * current number, the text color the labels, and a faint tone of the background the days without one.
 */
@Composable
fun WritingStreakCard(
    uiState: WritingStreakUiState,
    onRuleSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val config = context.config
    val primaryColor = Color(config.primaryColor)
    val textColor = Color(config.textColor)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SimpleText(
                text = stringResource(R.string.writing_streak_title),
                fontSize = TITLE_FONT_SIZE,
                fontColor = textColor,
            )
            Spacer(modifier = Modifier.weight(1f))
            StreakRuleChips(
                isTwoDayRule = uiState.isTwoDayRule,
                primaryColor = primaryColor,
                textColor = textColor,
                onRuleSelected = onRuleSelected,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            SimpleText(
                text = "🔥 " + stringResource(R.string.writing_streak_current_days, uiState.summary.currentStreak),
                fontSize = (config.settingFontSize * 1.6F).coerceAtMost(MAX_CURRENT_FONT_SIZE),
                fontWeight = FontWeight.Bold,
                fontColor = primaryColor,
            )
            Spacer(modifier = Modifier.width(10.dp))
            SimpleText(
                text = stringResource(R.string.writing_streak_longest_days, uiState.summary.longestStreak),
                fontSize = (config.settingFontSize * 0.8F).coerceAtMost(MAX_LONGEST_FONT_SIZE),
                alpha = 0.7F,
                fontColor = textColor,
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        StreakStatusBadge(
            summary = uiState.summary,
            primaryColor = primaryColor,
            textColor = textColor,
            fontSize = (config.settingFontSize * 0.85F).coerceAtMost(MAX_STATUS_FONT_SIZE),
        )

        if (uiState.summary.heatmap.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            StreakHeatmap(
                heatmap = uiState.summary.heatmap,
                primaryColor = primaryColor,
                emptyColor = textColor.copy(alpha = EMPTY_DAY_ALPHA),
            )
        }
    }
}

/** The two rules of the card, the selected one filled with the primary color. */
@Composable
private fun StreakRuleChips(
    isTwoDayRule: Boolean,
    primaryColor: Color,
    textColor: Color,
    onRuleSelected: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp)

    Row(
        modifier =
            Modifier
                .clip(shape)
                .background(Color(context.config.backgroundColor.darkenColor(context.innerCardDarkenFactor())))
                .padding(2.dp),
    ) {
        StreakRuleChip(
            label = stringResource(R.string.writing_streak_rule_daily),
            selected = !isTwoDayRule,
            primaryColor = primaryColor,
            textColor = textColor,
            onClick = { onRuleSelected(false) },
        )
        StreakRuleChip(
            label = stringResource(R.string.writing_streak_rule_two_day),
            selected = isTwoDayRule,
            primaryColor = primaryColor,
            textColor = textColor,
            onClick = { onRuleSelected(true) },
        )
    }
}

@Composable
private fun StreakRuleChip(
    label: String,
    selected: Boolean,
    primaryColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp))
                .background(if (selected) primaryColor else Color.Transparent)
                .selectable(selected = selected, role = Role.Button, onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        SimpleText(
            text = label,
            fontSize = RULE_FONT_SIZE,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontColor = if (selected) Color.White else textColor,
            alpha = if (selected) 1.0F else 0.75F,
            maxLines = 1,
        )
    }
}

/**
 * What today means for the chain, as one rounded label.
 *
 * The two day rule with yesterday skipped is the one state the writer can still lose, so it is drawn in the
 * primary color to stand apart from the states that only inform.
 */
@Composable
private fun StreakStatusBadge(
    summary: StreakSummary,
    primaryColor: Color,
    textColor: Color,
    fontSize: Float,
) {
    val atRisk = summary.status == StreakStatus.AT_RISK_TWO_DAY
    val message =
        when (summary.status) {
            StreakStatus.WRITTEN_TODAY -> stringResource(R.string.writing_streak_status_written_today)
            StreakStatus.DUE_TODAY -> stringResource(R.string.writing_streak_status_due_today, summary.streakIfWrittenToday)
            StreakStatus.AT_RISK_TWO_DAY -> stringResource(R.string.writing_streak_status_at_risk)
            StreakStatus.BROKEN -> stringResource(R.string.writing_streak_status_broken)
        }
    val shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp)

    Box(
        modifier =
            Modifier
                .clip(shape)
                .background(
                    if (atRisk) primaryColor.copy(alpha = AT_RISK_BACKGROUND_ALPHA) else textColor.copy(alpha = BADGE_BACKGROUND_ALPHA),
                )
                .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        SimpleText(
            text = message,
            fontSize = fontSize,
            fontWeight = if (atRisk) FontWeight.Bold else FontWeight.Normal,
            fontColor = if (atRisk) primaryColor else textColor,
            maxLines = 2,
        )
    }
}

/**
 * The recent weeks as a grid: one square per day, a column per week, Sunday on top, the week of today last.
 *
 * A day with an entry takes the primary color, a day without one a faint tone of the text color, and a day
 * that has not come yet stays empty so the chain is not read as broken at its end.
 */
@Composable
private fun StreakHeatmap(
    heatmap: List<List<StreakHeatmapDay>>,
    primaryColor: Color,
    emptyColor: Color,
) {
    val shape = RoundedCornerShape(HEATMAP_CELL_RADIUS)

    Column(verticalArrangement = Arrangement.spacedBy(HEATMAP_CELL_SPACING)) {
        repeat(DAYS_IN_WEEK) { dayIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(HEATMAP_CELL_SPACING)) {
                heatmap.forEach { week ->
                    val day = week[dayIndex]
                    Box(
                        modifier =
                            Modifier
                                .weight(1F)
                                .aspectRatio(1F)
                                .clip(shape)
                                .background(
                                    when {
                                        day.isFuture -> Color.Transparent
                                        day.count > 0 -> primaryColor
                                        else -> emptyColor
                                    },
                                ),
                    )
                }
            }
        }
    }
}

private const val DAYS_IN_WEEK = 7

/** The card title keeps the size of the dashboard card titles of the app, whatever the text size is set to. */
private const val TITLE_FONT_SIZE = 17F

/** The rule chips stay the size of the tag chips of the app. */
private const val RULE_FONT_SIZE = 12F

private const val MAX_CURRENT_FONT_SIZE = 40F
private const val MAX_LONGEST_FONT_SIZE = 20F
private const val MAX_STATUS_FONT_SIZE = 18F

private const val EMPTY_DAY_ALPHA = 0.12F
private const val BADGE_BACKGROUND_ALPHA = 0.08F
private const val AT_RISK_BACKGROUND_ALPHA = 0.16F

private val HEATMAP_CELL_SPACING = 3.dp
private val HEATMAP_CELL_RADIUS = 2.dp

@Preview(showBackground = true)
@Composable
private fun WritingStreakCardPreview() {
    // The last weeks written every day except yesterday and today, so the preview shows the day at risk.
    val today = LocalDate.now()
    val diaries =
        (2L..70L)
            .filter { offset -> offset % 8L != 0L }
            .map { offset ->
                val date = today.minusDays(offset)
                Diary(
                    diaryId = offset,
                    currentTimeMillis = date.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                )
            }

    AppTheme {
        WritingStreakCard(
            uiState =
                WritingStreakUiState(
                    summary = StreakUtils.calculate(diaries, today, isTwoDayRule = true),
                    isTwoDayRule = true,
                ),
            onRuleSelected = {},
            modifier = Modifier.padding(10.dp),
        )
    }
}
