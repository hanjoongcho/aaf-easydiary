package me.blog.korn123.easydiary.presentation.calendar

import android.app.DatePickerDialog
import android.content.Intent
import android.widget.ImageView
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.findActivity
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SATURDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SUNDAY
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.SettingConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.components.CustomCircleIconButton
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.main.components.LegacyDiaryItemCard
import me.blog.korn123.easydiary.presentation.settings.components.SimpleCard
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@Composable
internal fun navigationBottomPadding(): Dp =
    if (isVanillaIceCreamPlus()) {
        WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()
    } else {
        0.dp
    }

@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel,
    enableScroll: Boolean = true,
) {
    val context = LocalContext.current
    val bottomPadding = navigationBottomPadding()

    AppTheme {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
            containerColor = Color(context.config.screenBackgroundColor),
            floatingActionButton = {
                Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                    FloatingActionButton(
                        onClick = {
                            val activity = context.findActivity()
                            if (activity is CalendarActivity) {
                                activity.finishActivityWithTransition()
                            } else {
                                activity?.finish()
                            }
                        },
                        containerColor = Color(context.config.primaryColor),
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "Finish Activity",
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { innerPadding ->
            CalendarContent(
                calendarViewModel = calendarViewModel,
                modifier = Modifier.padding(innerPadding),
                enableScroll = enableScroll,
            )
        }
    }
}

@Composable
fun CalendarContent(
    calendarViewModel: CalendarViewModel,
    modifier: Modifier = Modifier,
    enableScroll: Boolean = true,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val startMonth = calendarViewModel.startMonth
    val endMonth = calendarViewModel.endMonth
    val selection = calendarViewModel.selection
    val eventsMap = calendarViewModel.eventsMap

    val daysOfWeek =
        remember {
            when (context.config.calendarStartDay) {
                CALENDAR_START_DAY_SUNDAY -> {
                    arrayOf(
                        DayOfWeek.SUNDAY,
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                    )
                }

                CALENDAR_START_DAY_SATURDAY -> {
                    arrayOf(
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY,
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                    )
                }

                else -> {
                    arrayOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY,
                    )
                }
            }
        }

    val state =
        rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstDayOfWeek = daysOfWeek.first(),
            firstVisibleMonth = calendarViewModel.currentMonth,
        )

    val visibleMonth = state.firstVisibleMonth.yearMonth

    LaunchedEffect(visibleMonth) {
        calendarViewModel.observeEventsForMonth(visibleMonth)
    }

    val bottomPadding = navigationBottomPadding()

    Box(modifier = modifier) {
        val columnModifier =
            Modifier
                .fillMaxSize()
                .let {
                    if (enableScroll) it.verticalScroll(rememberScrollState()) else it
                }
        Column(
            modifier = columnModifier,
        ) {
            MonthHeader(
                daysOfWeek = daysOfWeek,
                month = visibleMonth,
                onHeaderClick = {
                    val calendar =
                        Calendar.getInstance().apply {
                            selection?.let {
                                set(it.year, it.monthValue - 1, it.dayOfMonth)
                            }
                        }
                    DatePickerDialog(
                        context.findActivity() ?: context,
                        { _, year, month, dayOfMonth ->
                            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                            calendarViewModel.setSelectedDate(selectedDate)
                            coroutineScope.launch {
                                state.scrollToMonth(YearMonth.of(year, month + 1))
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                    ).show()
                },
            ) {
//                val millis =
//                    selection
//                        ?.atStartOfDay(ZoneId.systemDefault())
//                        ?.toInstant()
//                        ?.toEpochMilli()
//                        ?: visibleMonth
//                            .atDay(1)
//                            .atStartOfDay(ZoneId.systemDefault())
//                            .toInstant()
//                            .toEpochMilli()
                TransitionHelper.startActivityWithTransition(
                    context.findActivity(),
                    Intent(context, DiaryWritingActivity::class.java).apply {
//                        putExtra(SettingConstants.INITIALIZE_TIME_MILLIS, millis)
                    },
                )
            }

            key(selection) {
                HorizontalCalendar(
                    modifier = Modifier.background(Color(context.config.backgroundColor)).padding(horizontal = 4.dp),
                    state = state,
                    dayContent = { day ->
                        val events = eventsMap[day.date] ?: emptyList()
                        DayContent(
                            day = day,
                            isSelected = selection == day.date,
                            events = events,
                            enableScroll = enableScroll,
                        ) { clicked ->
                            if (enableScroll) {
                                calendarViewModel.setSelectedDate(clicked)
                            } else {
                                val intent =
                                    Intent(context, SelectedDateEventsActivity::class.java).apply {
                                        putExtra("SELECTED_DATE", clicked.toString())
                                    }
                                TransitionHelper.startActivityWithTransition(context.findActivity(), intent)
                            }
                        }
                    },
                )
            }

            if (enableScroll) {
                Spacer(modifier = Modifier.height(VERTICAL_PADDING.dp))

                SelectedDateEventsSection(
                    events = selection?.let { eventsMap[it] } ?: emptyList(),
                    enableScroll = enableScroll,
                    bottomPadding = bottomPadding,
                )
            }
        }
    }
}

@Composable
internal fun SelectedDateEventsSection(
    events: List<Diary>,
    enableScroll: Boolean,
    bottomPadding: Dp,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    if (events.isEmpty()) {
        SimpleCard(
            title = stringResource(R.string.guide_message_4),
            description = null,
            modifier = Modifier.fillMaxWidth(),
            isBoldTitle = false,
        )
    } else {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (enableScroll) 72.dp.plus(bottomPadding) else 0.dp),
        ) {
            events.forEach { event ->
                key(event.diaryId) {
                    Card(
                        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
                        colors = CardDefaults.cardColors(Color(context.config.backgroundColor)),
                        modifier =
                            (
                                if (context.config.enableCardViewPolicy) {
                                    Modifier.padding(
                                        HORIZONTAL_PADDING.dp,
                                        VERTICAL_PADDING.dp,
                                    )
                                } else {
                                    Modifier
                                        .padding(1.dp, 1.dp)
                                }
                            ).combinedClickable(
                                onClick = {
                                    TransitionHelper.startActivityWithTransition(
                                        activity,
                                        Intent(context, DiaryReadingActivity::class.java).apply {
                                            putExtra(DIARY_SEQUENCE, event.diaryId)
                                        },
                                    )
                                },
                                onLongClick = { context.makeToast(EasyDiaryUtils.summaryDiaryLabel(event)) },
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
                    ) {
                        LegacyDiaryItemCard(
                            diary = event,
                            itemClickCallback = {
                                TransitionHelper.startActivityWithTransition(
                                    activity,
                                    Intent(context, DiaryReadingActivity::class.java).apply {
                                        putExtra(DIARY_SEQUENCE, event.diaryId)
                                    },
                                )
                            },
                            itemLongClickCallback = { context.makeToast(EasyDiaryUtils.summaryDiaryLabel(event)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: Array<DayOfWeek>) {
    val context = LocalContext.current
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(Color(context.config.backgroundColor)),
    ) {
        Spacer(modifier = Modifier.height(VERTICAL_PADDING.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            daysOfWeek.forEach { dayOfWeek ->
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale)
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color =
                        when (dayOfWeek) {
                            DayOfWeek.SUNDAY -> Color.Red
                            DayOfWeek.SATURDAY -> Color.Blue
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                )
            }
        }
    }
}

@Composable
fun MonthHeader(
    daysOfWeek: Array<DayOfWeek>,
    month: YearMonth,
    onHeaderClick: () -> Unit,
    onWriteClick: () -> Unit,
) {
    val context = LocalContext.current
    val formatter =
        remember {
            DateTimeFormatter.ofPattern(
                android.text.format.DateFormat
                    .getBestDateTimePattern(Locale.getDefault(), "yyyyMMMM"),
                Locale.getDefault(),
            )
        }
    val topPadding =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color(context.config.backgroundColor))
                .padding(top = topPadding),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp, end = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onHeaderClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = month.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(context.config.textColor),
                )
            }

            CustomCircleIconButton(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd),
                iconResourceId = R.drawable.ic_outline_list_alt_add,
                contentDescription = "Write Diary",
                iconPadding = 1.dp,
                elevation = 3.dp,
                onClick = { onWriteClick() },
            )
        }
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
    }
}

@Composable
fun DayContent(
    day: CalendarDay,
    isSelected: Boolean,
    events: List<Diary>,
    enableScroll: Boolean,
    onClick: (LocalDate) -> Unit,
) {
    val isCurrentMonth = day.position == DayPosition.MonthDate
    val displayEvents = events.take(3)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(0.dp)
                .clip(RoundedCornerShape(1.dp))
                .then(
                    if (isSelected && enableScroll) {
                        Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(1.dp),
                        )
                    } else {
                        Modifier.background(Color.Transparent)
                    },
                ).clickable(enabled = true) { onClick(day.date) }
                .padding(1.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp),
            ) {
                Box(
                    modifier = Modifier.align(Alignment.Center),
                    contentAlignment = Alignment.Center,
                ) {
                    val isToday = day.date == LocalDate.now()
                    Box(
                        modifier =
                            Modifier
                                .then(
                                    if (isToday) {
                                        Modifier
                                            .size(18.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                CircleShape,
                                            )
                                    } else {
                                        Modifier
                                    },
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            modifier = Modifier.offset(y = (-1).dp),
                            color =
                                when {
                                    isToday -> MaterialTheme.colorScheme.primary
                                    !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    day.date.dayOfWeek == DayOfWeek.SUNDAY -> Color.Red
                                    day.date.dayOfWeek == DayOfWeek.SATURDAY -> Color.Blue
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                        )
                    }
                }

                val extraCount = events.size - 3
                if (extraCount > 0) {
                    Text(
                        text = "+$extraCount",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
            }

            if (displayEvents.isNotEmpty()) {
                displayEvents.forEach { diary ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
//                                .background(Color.Yellow.copy(alpha = 0.4f))
                                .padding(horizontal = 1.dp, vertical = 2.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        val density = LocalDensity.current
                        val pxValue = with(density) { 10.sp.toPx() }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AndroidView(
                                modifier = Modifier.size(10.dp),
                                factory = { ctx -> ImageView(ctx) },
                                update = { FlavorUtils.initWeatherView(it.context, it, diary.symbolSequence) },
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            SimpleText(text = EasyDiaryUtils.summaryDiaryLabel(diary), maxLines = 1, fontSize = pxValue)
                        }
                    }
                }
            }
        }
    }
}
