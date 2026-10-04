package me.blog.korn123.easydiary.compose

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.extensions.applyFullScreenStatusBarTheme
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.theme.AppTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class CalendarDemoActivity : EasyDiaryComposeBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarDemoScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CalendarDemoScreen() {
        val activity = LocalActivity.current
        val context = LocalContext.current
        val currentMonth = remember { YearMonth.now() }
        val startMonth = remember { currentMonth.minusMonths(12) }
        val endMonth = remember { currentMonth.plusMonths(12) }
        var selection by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

        val sampleEvents =
            remember {
                val eventsMap = mutableMapOf<LocalDate, List<String>>()
                val today = LocalDate.now()

                // Specific events
                eventsMap[today] = listOf("Team Meeting @ 10:00", "Review PR #123")
                eventsMap[today.plusDays(1)] = listOf("Dentist Appointment", "Gym Workout")
                eventsMap[today.minusDays(2)] = listOf("Project Kickoff", "Grocery Shopping")
                eventsMap[today.plusDays(3)] = listOf("Birthday Party 🎉")
                eventsMap[today.plusDays(5)] = listOf("Code Refactoring", "Unit Testing")
                eventsMap[today.plusDays(7)] = listOf("Weekly Summary", "Client Call")
                eventsMap[today.minusDays(5)] = listOf("Write EasyDiary Entry 📝")
                eventsMap[today.plusDays(10)] = listOf("Vacation Start ✈️")
                eventsMap[today.plusDays(12)] = listOf("Family Dinner 🍽️")

                // Generate sample events for every day across a wider range (past and future months)
                val baseDate = LocalDate.now().minusMonths(6)
                for (i in 0..365) {
                    val date = baseDate.plusDays(i.toLong())
                    if (!eventsMap.containsKey(date)) {
                        eventsMap[date] = listOf("Daily Schedule #${date.dayOfMonth}", "Routine Check")
                    }
                }
                eventsMap
            }

        val daysOfWeek =
            remember {
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

        val state =
            rememberCalendarState(
                startMonth = startMonth,
                endMonth = endMonth,
                firstDayOfWeek = daysOfWeek.first(),
                firstVisibleMonth = currentMonth,
            )

        LaunchedEffect(Unit) {
            activity?.applyFullScreenStatusBarTheme()
        }

        AppTheme {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                containerColor = Color(context.config.screenBackgroundColor),
                topBar = {
                    EasyDiaryActionBar(title = "Calendar Demo (Grid Events)") {
                        finishActivityWithTransition()
                    }
                },
            ) { innerPadding ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                ) {
                    key(selection) {
                        HorizontalCalendar(
                            state = state,
                            dayContent = { day ->
                                val events = sampleEvents[day.date] ?: emptyList()
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
                                MonthHeader(month = month.yearMonth)
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Schedules for ${selection ?: LocalDate.now()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    val events = selection?.let { sampleEvents[it] } ?: emptyList()
                    Log.i(AAF_TEST, "selection: $selection, events: $events, sampleEvents: $sampleEvents")
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                    ) {
                        if (events.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Text(
                                    text = "No schedules for this date.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                items(events) { event ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Text(
                                            text = "• $event",
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MonthHeader(month: YearMonth) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "${month.month.name} ${month.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    @Composable
    fun DayContent(
        day: CalendarDay,
        isSelected: Boolean,
        events: List<String>,
        onClick: (LocalDate) -> Unit,
    ) {
        val isCurrentMonth = day.position == DayPosition.MonthDate
        val hasEvents = events.isNotEmpty()

        Box(
            modifier =
                Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            hasEvents && isCurrentMonth -> MaterialTheme.colorScheme.secondaryContainer
                            else -> Color.Transparent
                        },
                    ).clickable(enabled = isCurrentMonth) { onClick(day.date) }
                    .padding(4.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color =
                        when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary

                            //                            hasEvents && isCurrentMonth -> MaterialTheme.colorScheme.onSecondaryContainer
                            isCurrentMonth -> MaterialTheme.colorScheme.onSurface

                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected || hasEvents) FontWeight.Bold else FontWeight.Normal,
                )

                if (hasEvents && isCurrentMonth) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 2.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = events.first(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            fontSize = 8.sp,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
