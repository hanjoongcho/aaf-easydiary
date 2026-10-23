package me.blog.korn123.easydiary.presentation.calendar

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import me.blog.korn123.easydiary.helper.TransitionHelper.Companion.finishActivityWithTransition
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class SelectedDateEventsActivity : EasyDiaryComposeBaseActivity() {
    private val viewModel: SelectedDateEventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SelectedDateEventsScreen(viewModel = viewModel)
        }
    }
}

@HiltViewModel
class SelectedDateEventsViewModel
    @Inject
    constructor(
        application: Application,
        diaryRepository: DiaryRepository,
        savedStateHandle: SavedStateHandle,
    ) : AndroidViewModel(application) {
        val dateString: String = savedStateHandle.get<String>("SELECTED_DATE") ?: LocalDate.now().toString()

        val diaries: StateFlow<List<Diary>> =
            diaryRepository
                .observeDiariesByDateString(
                    dateString,
                    getApplication<Application>().config.calendarSorting == CALENDAR_SORTING_ASC,
                ).stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        var isLoading by mutableStateOf(false)
            private set
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedDateEventsScreen(
    viewModel: SelectedDateEventsViewModel,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val bottomPadding = navigationBottomPadding()
    val diaries by viewModel.diaries.collectAsStateWithLifecycle()

    val formattedTitle =
        remember(viewModel.dateString) {
            try {
                val localDate = LocalDate.parse(viewModel.dateString)
                val millis = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                DateUtils.getDateStringFromTimeMillis(millis)
            } catch (_: Exception) {
                viewModel.dateString
            }
        }

    AppTheme {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
            topBar = {
                EasyDiaryActionBar(
                    title = formattedTitle,
                ) {
                    finishActivityWithTransition(activity)
                }
            },
            containerColor = Color(context.config.screenBackgroundColor),
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
            ) {
                SelectedDateEventsSection(
                    events = diaries,
                    enableScroll = true,
                    bottomPadding = bottomPadding,
                )
            }
        }
    }
}
