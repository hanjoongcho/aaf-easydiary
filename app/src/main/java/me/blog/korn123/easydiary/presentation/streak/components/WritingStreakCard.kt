package me.blog.korn123.easydiary.presentation.streak.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.enums.TimeFormatMode
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.darkenColor
import me.blog.korn123.easydiary.extensions.dateAnchorTimeMillis
import me.blog.korn123.easydiary.extensions.innerCardDarkenFactor
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.storedTimeFormat
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.presentation.components.EasyDiaryAlertDialog
import me.blog.korn123.easydiary.presentation.components.EasyDiaryDialogButton
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.streak.WritingStreakUiState
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.StreakHeatmapDay
import me.blog.korn123.easydiary.utils.StreakStatus
import me.blog.korn123.easydiary.utils.StreakSummary
import me.blog.korn123.easydiary.utils.StreakUtils
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

/**
 * The writing streak card of the dashboard (issue #88).
 *
 * The card answers two questions at a glance: how long the writer has kept writing without breaking the
 * chain, and whether today still needs an entry to keep it. The header carries the two rules the writer can
 * switch between, the numbers show the current and the best chain, the badge says what today means for the
 * chain, and the grid at the bottom is the chain itself, a square for every day of the recent weeks.
 *
 * A square also leads back to what was written that day: the day it belongs to is written in it, the month it
 * belongs to above its column, and a tap opens the entry itself - the one entry of the day at once, or the
 * entries of a day that holds several as a list to pick from ([onDiarySelected] opens the one picked).
 *
 * The card is drawn inside the dashboard's own card, so it paints no background of its own and takes its
 * colors from the theme the rest of the app uses: the primary color marks the days with an entry and the
 * current number, the text color the labels, and a faint tone of the background the days without one.
 *
 * Every size the card asks of [SimpleText] is handed over in pixels: the sizes below are written in sp, as the
 * screen designs are, and turned into pixels with the screen density, because [SimpleText] reads its `fontSize`
 * as a pixel value.
 */
@Composable
fun WritingStreakCard(
    uiState: WritingStreakUiState,
    onRuleSelected: (Boolean) -> Unit,
    onDiarySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val config = context.config
    val density = LocalDensity.current
    val primaryColor = Color(config.primaryColor)
    val textColor = Color(config.textColor)
    val titleFontSize = with(density) { TITLE_FONT_SIZE_SP.sp.toPx() }
    val maxCurrentFontSize = with(density) { MAX_CURRENT_FONT_SIZE_SP.sp.toPx() }
    val maxLongestFontSize = with(density) { MAX_LONGEST_FONT_SIZE_SP.sp.toPx() }
    val maxStatusFontSize = with(density) { MAX_STATUS_FONT_SIZE_SP.sp.toPx() }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SimpleText(
                text = stringResource(R.string.writing_streak_title),
                fontSize = titleFontSize,
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
                fontSize = (config.settingFontSize * 1.6F).coerceAtMost(maxCurrentFontSize),
                fontWeight = FontWeight.Bold,
                fontColor = primaryColor,
            )
            Spacer(modifier = Modifier.width(10.dp))
            SimpleText(
                text = stringResource(R.string.writing_streak_longest_days, uiState.summary.longestStreak),
                modifier = Modifier.weight(1F, fill = false),
                fontSize = (config.settingFontSize * 0.85F).coerceAtMost(maxLongestFontSize),
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
            fontSizePx = (config.settingFontSize * 0.9F).coerceAtMost(maxStatusFontSize),
        )

        if (uiState.summary.heatmap.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            StreakHeatmap(
                heatmap = uiState.summary.heatmap,
                months = uiState.summary.heatmapMonths,
                primaryColor = primaryColor,
                emptyColor = textColor.copy(alpha = EMPTY_DAY_ALPHA),
                textColor = textColor,
                onDiarySelected = onDiarySelected,
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
    val density = LocalDensity.current
    val ruleFontSize = with(density) { RULE_FONT_SIZE_SP.sp.toPx() }

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
            fontSize = ruleFontSize,
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
    fontSizePx: Float,
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
            fontSize = fontSizePx,
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
 * that has not come yet stays empty so the chain is not read as broken at its end. Every square that has come
 * writes its day of the month in itself, in white on an entry and faintly on a day without one, and the column
 * of a month writes the month above the grid, so a square says which day it is without a tap.
 *
 * A square that has come can be tapped: the day without an entry says so, the day with one entry opens it, and
 * the day with several asks which one to open.
 */
@Composable
private fun StreakHeatmap(
    heatmap: List<List<StreakHeatmapDay>>,
    months: List<YearMonth?>,
    primaryColor: Color,
    emptyColor: Color,
    textColor: Color,
    onDiarySelected: (Long) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val shape = RoundedCornerShape(HEATMAP_CELL_RADIUS)
    val monthFontSize = with(density) { MONTH_FONT_SIZE_SP.sp.toPx() }
    val dayNumberFontSize = with(density) { DAY_NUMBER_FONT_SIZE_SP.sp.toPx() }
    var openedDay by remember { mutableStateOf<StreakHeatmapDay?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(HEATMAP_CELL_SPACING)) {
        StreakMonthLabels(
            months = if (months.size == heatmap.size) months else List(heatmap.size) { null },
            textColor = textColor,
            fontSizePx = monthFontSize,
        )

        repeat(DAYS_IN_WEEK) { dayIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HEATMAP_CELL_SPACING),
            ) {
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
                                ).then(
                                    if (day.isFuture) {
                                        Modifier
                                    } else {
                                        Modifier.clickable {
                                            when {
                                                day.count == 0 -> context.makeToast(context.getString(R.string.guide_message_4))
                                                day.count == 1 -> onDiarySelected(day.diaries.first().diaryId)
                                                else -> openedDay = day
                                            }
                                        }
                                    },
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!day.isFuture) {
                            SimpleText(
                                text = day.date.dayOfMonth.toString(),
                                fontSize = dayNumberFontSize,
                                fontWeight = if (day.count > 0) FontWeight.Bold else FontWeight.Normal,
                                fontColor = if (day.count > 0) Color.White else textColor,
                                alpha = if (day.count > 0) 1F else EMPTY_DAY_NUMBER_ALPHA,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }

    openedDay?.let { day ->
        StreakDayDiaryDialog(
            day = day,
            textColor = textColor,
            onDismiss = { openedDay = null },
            onDiarySelected = { diaryId ->
                openedDay = null
                onDiarySelected(diaryId)
            },
        )
    }
}

/**
 * The month of each column above the grid.
 *
 * A name is wider than a column of the grid, so each name is measured with no width limit of its own and laid
 * over the column it belongs to: the name of a month starts at its first column and runs right, over the
 * columns of the days that share the month, which are labelled only once.
 */
@Composable
private fun StreakMonthLabels(
    months: List<YearMonth?>,
    textColor: Color,
    fontSizePx: Float,
) {
    val locale = Locale.getDefault()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HEATMAP_CELL_SPACING),
    ) {
        months.forEach { month ->
            Box(modifier = Modifier.weight(1F)) {
                if (month != null) {
                    SimpleText(
                        modifier = Modifier.wrapContentWidth(unbounded = true),
                        text = StreakUtils.monthLabel(month, locale),
                        fontSize = fontSizePx,
                        fontColor = textColor,
                        alpha = MONTH_LABEL_ALPHA,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * The entries of one day, to pick the one to open from.
 *
 * The dialog lists every entry of the day with its time and the title or the first line it was written with,
 * newest first, the order the diary list of the app uses. An entry is opened by tapping its line, and the
 * entry of a day that holds a single one never reaches this dialog: the card opens it at once.
 *
 * It is drawn with the app dialog ([EasyDiaryAlertDialog]) rather than a Material 3 `AlertDialog`, so it reads
 * as every other dialog of the app: the title bar in the primary color carries the day it lists, and the
 * entries and the cancel button take the text color of the theme.
 */
@Composable
private fun StreakDayDiaryDialog(
    day: StreakHeatmapDay,
    textColor: Color,
    onDismiss: () -> Unit,
    onDiarySelected: (Long) -> Unit,
) {
    val context = LocalContext.current
    val locale = Locale.getDefault()
    val timeFormatMode = remember(context) { context.storedTimeFormat() }
    val dayLabel = remember(day.date, locale) { day.date.mediumDateLabel(locale) }

    EasyDiaryAlertDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.writing_streak_day_diaries_title, dayLabel),
        buttons = {
            EasyDiaryDialogButton(text = stringResource(R.string.cancel), onClick = onDismiss)
        },
    ) {
        // A day can hold many entries, so the list scrolls inside the dialog instead of growing off the screen.
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = DAY_LIST_MAX_HEIGHT)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            day.diaries.forEach { diary ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp))
                            .clickable { onDiarySelected(diary.diaryId) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                ) {
                    SimpleText(
                        text = diary.entryLabel(context, timeFormatMode, locale),
                        fontColor = textColor,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

/** The line the dialog writes for an entry: its time, and the title or the first line it was written with. */
private fun Diary.entryLabel(
    context: Context,
    timeFormatMode: TimeFormatMode,
    locale: Locale,
): String {
    val time =
        if (isAllDay) {
            context.getString(R.string.all_day)
        } else {
            DateUtils.getTimeStringFromTimeMillis(
                timeMillis = dateAnchorTimeMillis(),
                timeFormat = SimpleDateFormat.SHORT,
                locale = locale,
                timeFormatMode = timeFormatMode,
            )
        }
    // An encrypted entry has no readable title or body until the pin is entered, so it only says what it is.
    val summary =
        when {
            isEncrypt -> context.getString(R.string.writing_streak_encrypted_diary)
            title.isNotBlank() -> title.trim()
            else ->
                contents
                    .lineSequence()
                    .firstOrNull { line -> line.isNotBlank() }
                    ?.trim()
                    .orEmpty()
        }.take(SUMMARY_MAX_LENGTH)

    return if (summary.isEmpty()) time else "$time · $summary"
}

/** The day of the grid as the dialog names it, in the date form of the device language. */
private fun LocalDate.mediumDateLabel(locale: Locale): String =
    DateUtils.getDateStringFromTimeMillis(
        timeMillis = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        dateFormat = SimpleDateFormat.MEDIUM,
        locale = locale,
    )

private const val DAYS_IN_WEEK = 7

// The text sizes of the card, in sp. SimpleText takes its fontSize in pixels, not in sp, so each of these is
// turned into pixels with the screen density of the screen the card is drawn on.

/** The card title keeps the size of the dashboard card titles of the app, whatever the text size is set to. */
private const val TITLE_FONT_SIZE_SP = 17F

/** The rule chips stay the size of the tag chips of the app. */
private const val RULE_FONT_SIZE_SP = 12F

/** The upper bounds of the sizes that follow the text size setting, so a large setting keeps the card in shape. */
private const val MAX_CURRENT_FONT_SIZE_SP = 36F
private const val MAX_LONGEST_FONT_SIZE_SP = 18F
private const val MAX_STATUS_FONT_SIZE_SP = 16F

private const val EMPTY_DAY_ALPHA = 0.12F
private const val BADGE_BACKGROUND_ALPHA = 0.08F
private const val AT_RISK_BACKGROUND_ALPHA = 0.16F

/** The month above a column and the day inside a square are small and quiet beside the numbers of the card. */
private const val MONTH_FONT_SIZE_SP = 9F
private const val DAY_NUMBER_FONT_SIZE_SP = 8F
private const val MONTH_LABEL_ALPHA = 0.6F

/** The day of a square without an entry is written faintly, so the square still reads as an empty one. */
private const val EMPTY_DAY_NUMBER_ALPHA = 0.45F

/** How much of an entry the list of a day shows before the line is cut short. */
private const val SUMMARY_MAX_LENGTH = 120

/** A day can hold many entries; past this the list of the dialog scrolls instead of growing off the screen. */
private val DAY_LIST_MAX_HEIGHT = 320.dp

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
            onDiarySelected = {},
            modifier = Modifier.padding(10.dp),
        )
    }
}
