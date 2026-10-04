package me.blog.korn123.easydiary.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.applyFullScreenStatusBarTheme
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.findActivity
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.updateNavigationBarAppearance
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SATURDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SUNDAY
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.helper.SettingConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.ui.components.LegacyDiaryItemCard
import me.blog.korn123.easydiary.ui.components.LoadingScreen
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SimpleText
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.DiaryViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class CalendarActivity : EasyDiaryComposeBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarScreen()
        }
    }

    @Composable
    fun CalendarScreen(
        diaryViewModel: DiaryViewModel = hiltViewModel(),
    ) {
        val context = LocalContext.current
        val currentMonth = remember { YearMonth.now() }
        var startMonth by remember { mutableStateOf(currentMonth.minusYears(2)) }
        var endMonth by remember { mutableStateOf(currentMonth.plusYears(2)) }
        var selection by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
        var eventsMap by remember { mutableStateOf<Map<LocalDate, List<Diary>>>(emptyMap()) }
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
                firstVisibleMonth = currentMonth,
            )

        val visibleMonth = state.firstVisibleMonth.yearMonth
        val formatter =
            remember {
                DateTimeFormatter.ofPattern(
                    android.text.format.DateFormat
                        .getBestDateTimePattern(Locale.getDefault(), "yyyyMMMM"),
                    Locale.getDefault(),
                )
            }
        val currentMonthTitle = visibleMonth.format(formatter)

        LaunchedEffect(visibleMonth) {
            diaryViewModel.isLoading = true
            if (!visibleMonth.isAfter(startMonth.plusMonths(3))) {
                startMonth = startMonth.minusYears(2)
            }
            if (!visibleMonth.isBefore(endMonth.minusMonths(3))) {
                endMonth = endMonth.plusYears(2)
            }

            diaryViewModel.observeDateStringMap(visibleMonth.monthValue, visibleMonth.year).collect { dateStringMap ->
                val newMap = mutableMapOf<LocalDate, List<Diary>>()
                for ((dateStr, diaries) in dateStringMap) {
                    if (diaries.isNotEmpty()) {
                        try {
                            val localDate = LocalDate.parse(dateStr)
                            newMap[localDate] = diaries
                        } catch (_: Exception) {
                            diaryViewModel.isLoading = false
                        }
                    }
                }
                eventsMap = newMap
                diaryViewModel.isLoading = false
            }
        }

        val bottomPadding =
            if (isVanillaIceCreamPlus()) {
                WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            } else {
                0.dp
            }

        AppTheme {
            enableEdgeToEdge()
            applyFullScreenStatusBarTheme()
            updateNavigationBarAppearance()
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
                containerColor = Color(context.config.screenBackgroundColor),
                floatingActionButton = {
                    Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                        FloatingActionButton(
                            onClick = { finishActivityWithTransition() },
                            containerColor = Color(config.primaryColor),
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
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
//                            .background(Color(context.config.backgroundColor))
                                .verticalScroll(rememberScrollState()),
                    ) {
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
                                    ) { clicked ->
                                        if (day.position == DayPosition.MonthDate) {
                                            selection = clicked
                                        }
                                    }
                                },
                                monthHeader = { month ->
                                    MonthHeader(daysOfWeek = daysOfWeek, month = month.yearMonth) {
                                        val millis =
                                            selection
                                                ?.atStartOfDay(ZoneId.systemDefault())
                                                ?.toInstant()
                                                ?.toEpochMilli()
                                                ?: month.yearMonth
                                                    .atDay(1)
                                                    .atStartOfDay(ZoneId.systemDefault())
                                                    .toInstant()
                                                    .toEpochMilli()
                                        TransitionHelper.startActivityWithTransition(
                                            context.findActivity(),
                                            Intent(context, DiaryWritingActivity::class.java).apply {
                                                putExtra(SettingConstants.INITIALIZE_TIME_MILLIS, millis)
                                            },
                                        )
                                    }
                                },
                            )
                        }

                        Spacer(modifier = Modifier.height(VERTICAL_PADDING.dp))

                        val events = selection?.let { eventsMap[it] } ?: emptyList()
//                    Log.i(
//                        AAF_TEST,
//                        "selection: $selection, events: $events, eventsMap: $eventsMap",
//                    )
                        if (events.isEmpty()) {
                            SimpleCard(title = getString(R.string.guide_message_4), description = null, modifier = Modifier.fillMaxWidth())
                        } else {
                            Column(
//                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 72.dp.plus(bottomPadding)),
                            ) {
                                events.forEach { event ->
                                    key(event.diaryId) {
                                        Card(
                                            shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
                                            colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
                                            modifier = (
                                                if (LocalContext.current.config.enableCardViewPolicy) {
                                                    Modifier.padding(
                                                        HORIZONTAL_PADDING.dp,
                                                        VERTICAL_PADDING.dp,
                                                    )
                                                } else {
                                                    Modifier
                                                        .padding(1.dp, 1.dp)
                                                }
                                            ),
                                            elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
                                        ) {
                                            LegacyDiaryItemCard(
                                                diary = event,
                                                itemClickCallback = { context.makeToast(EasyDiaryUtils.summaryDiaryLabel(event)) },
                                                itemLongClickCallback = { context.makeToast(EasyDiaryUtils.summaryDiaryLabel(event)) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = diaryViewModel.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        LoadingScreen()
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
                        .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = month.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(context.config.textColor),
                )

                IconButton(
                    onClick = onWriteClick,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Write Diary",
                        tint = Color(context.config.textColor),
                    )
                }
            }
            DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        }
    }

    @Composable
    fun DayContent(
        day: CalendarDay,
        isSelected: Boolean,
        events: List<Diary>,
        onClick: (LocalDate) -> Unit,
    ) {
        val isCurrentMonth = day.position == DayPosition.MonthDate
        val hasEvents = events.isNotEmpty()
        val displayEvents = events.take(3)

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(0.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(1.dp),
                            )
                        } else {
                            Modifier.background(Color.Transparent)
                        },
                    ).clickable(enabled = isCurrentMonth) { onClick(day.date) }
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp),
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
                    if (extraCount > 0 && isCurrentMonth) {
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

                if (displayEvents.isNotEmpty() && isCurrentMonth) {
//                    Spacer(modifier = Modifier.height(1.dp))
                    displayEvents.forEach { diary ->
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.Yellow.copy(alpha = 0.4f))
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            val density = LocalDensity.current
                            val pxValue = with(density) { 10.sp.toPx() }
                            SimpleText(text = EasyDiaryUtils.summaryDiaryLabel(diary), maxLines = 1, fontSize = pxValue)
                        }
//                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}
