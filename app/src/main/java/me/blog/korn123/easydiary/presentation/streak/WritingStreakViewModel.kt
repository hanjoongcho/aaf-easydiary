package me.blog.korn123.easydiary.presentation.streak

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.utils.StreakSummary
import me.blog.korn123.easydiary.utils.StreakUtils
import java.time.LocalDate
import javax.inject.Inject

/**
 * What the dashboard streak card draws: the numbers of [summary] counted under the rule the writer picked,
 * and which rule that is so the two chips of the card can show the selection.
 *
 * [summary] carries the entries of every square of the heatmap as well, so a tap on a square can open what
 * was written that day without reading the stored entries again.
 */
data class WritingStreakUiState(
    val summary: StreakSummary = StreakSummary(),
    val isTwoDayRule: Boolean = false,
)

/**
 * The writing streak card of the dashboard (issue #88).
 *
 * The card follows the stored entries, so writing or deleting one changes the numbers the next time the card
 * is drawn without any reload, and the selected rule is kept in the settings so the card comes back the way it
 * was left. [refreshToday] is called when the dashboard returns to the front, because a card left open
 * overnight would otherwise keep counting the day it was drawn on.
 */
@HiltViewModel
class WritingStreakViewModel
    @Inject
    constructor(
        application: Application,
        private val diaryRepository: DiaryRepository,
    ) : AndroidViewModel(application) {
        private val app = application
        private val isTwoDayRule = MutableStateFlow(app.config.enableStreakTwoDayRule)
        private val today = MutableStateFlow(LocalDate.now())

        val uiState: StateFlow<WritingStreakUiState> =
            combine(
                diaryRepository.observeDiariesWithPhotos(),
                isTwoDayRule,
                today,
            ) { diaries, twoDayRule, todayDate ->
                WritingStreakUiState(
                    summary = StreakUtils.calculate(diaries, todayDate, twoDayRule),
                    isTwoDayRule = twoDayRule,
                )
            }.flowOn(Dispatchers.Default)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue =
                        WritingStreakUiState(
                            summary = StreakUtils.calculate(emptyList(), LocalDate.now(), app.config.enableStreakTwoDayRule),
                            isTwoDayRule = app.config.enableStreakTwoDayRule,
                        ),
                )

        /** Switches the rule from the chips of the card and keeps the choice for the next time it is drawn. */
        fun setTwoDayRule(enabled: Boolean) {
            if (isTwoDayRule.value == enabled) return
            app.config.enableStreakTwoDayRule = enabled
            isTwoDayRule.value = enabled
        }

        /** Counts the numbers again for the day the card is drawn on, after a night the dashboard stayed open. */
        fun refreshToday() {
            today.value = LocalDate.now()
        }

        companion object {
            private const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
