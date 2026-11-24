package me.blog.korn123.easydiary.presentation.tag

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.blog.korn123.easydiary.domain.model.TagRules
import javax.inject.Inject

/**
 * State of the tag picker of a diary entry (issue #270).
 *
 * [selectedNames] keeps the order the tags were picked in; [allNames] is every stored tag plus the ones this
 * picker already created, and [createCandidate] is the name typed in the search field that no tag has yet,
 * which the screen offers as a `+ ...` row.
 */
data class TagPickerUiState(
    val query: String = "",
    val allNames: List<String> = emptyList(),
    val selectedNames: List<String> = emptyList(),
) {
    val filteredNames: List<String>
        get() = allNames.filter { TagRules.containsQuery(it, query) }

    val createCandidate: String?
        get() =
            TagRules.sanitize(query)?.takeIf { candidate ->
                !TagRules.matchesAny(candidate, allNames)
            }

    fun isSelected(name: String): Boolean = TagRules.matchesAny(name, selectedNames)
}

@HiltViewModel
class TagPickerViewModel
    @Inject
    constructor() : ViewModel() {
        private val _uiState = MutableStateFlow(TagPickerUiState())
        val uiState: StateFlow<TagPickerUiState> = _uiState.asStateFlow()

        private var isInitialized = false

        /** Takes the tags of the database and of the entry in, once per picker (a rotation keeps them). */
        fun initialize(
            allNames: List<String>,
            selectedNames: List<String>,
        ) {
            if (isInitialized) return
            isInitialized = true
            _uiState.value =
                TagPickerUiState(
                    allNames = TagRules.sanitizeAll(allNames + selectedNames),
                    selectedNames = TagRules.sanitizeAll(selectedNames),
                )
        }

        fun updateQuery(query: String) {
            _uiState.update { it.copy(query = query) }
        }

        /** One tap either adds [name] to the selection or takes it out again. */
        fun toggle(name: String) {
            val tagName = TagRules.sanitize(name) ?: return
            _uiState.update { state ->
                val updatedNames =
                    when (state.isSelected(tagName)) {
                        true -> state.selectedNames.filterNot { TagRules.normalize(it) == TagRules.normalize(tagName) }
                        false -> state.selectedNames + tagName
                    }
                state.copy(selectedNames = updatedNames)
            }
        }

        /** Creates the tag typed in the search field and selects it. */
        fun addQueryAsTag() {
            val candidate = _uiState.value.createCandidate ?: return
            _uiState.update { state ->
                state.copy(
                    allNames = TagRules.sanitizeAll(state.allNames + candidate),
                    selectedNames = TagRules.sanitizeAll(state.selectedNames + candidate),
                    query = "",
                )
            }
        }
    }
