package me.blog.korn123.easydiary.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.blog.korn123.easydiary.domain.model.TagStatistics
import me.blog.korn123.easydiary.domain.model.TagWithCount
import me.blog.korn123.easydiary.domain.repository.TagRepository
import javax.inject.Inject

/**
 * What the tag chart draws: the tags the most entries carry, strongest first, and whether the counts are read yet.
 *
 * [tags] holds at most [TagStatistics.TOP_RANK_COUNT] rows and no tag of zero entries, so an empty list means the
 * writer has not put a tag on an entry yet and the screen says that instead of drawing.
 */
data class TagTopTenChartUiState(
    val tags: List<TagWithCount> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * The tag chart of the statistics screen (issue #270).
 *
 * The counts follow the database, so tagging an entry or deleting a tag shows up in the chart without the screen
 * reading everything again. The counting and the ordering stay in [TagStatistics], which the DAO hands its rows
 * to, so the view model only turns the stored rows into the state the chart draws.
 */
@HiltViewModel
class TagTopTenChartViewModel
    @Inject
    constructor(
        private val tagRepository: TagRepository,
    ) : ViewModel() {
        val uiState: StateFlow<TagTopTenChartUiState> =
            tagRepository.observeTagsWithCount()
                .map { rows -> TagTopTenChartUiState(tags = TagStatistics.topRanked(rows), isLoading = false) }
                .flowOn(Dispatchers.Default)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = TagTopTenChartUiState(),
                )

        companion object {
            private const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
