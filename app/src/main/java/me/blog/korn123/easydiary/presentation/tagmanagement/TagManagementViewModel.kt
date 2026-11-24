package me.blog.korn123.easydiary.presentation.tagmanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.domain.model.Tag
import me.blog.korn123.easydiary.domain.model.TagCreateResult
import me.blog.korn123.easydiary.domain.model.TagRenameResult
import me.blog.korn123.easydiary.domain.model.TagWithCount
import me.blog.korn123.easydiary.domain.repository.TagRepository
import javax.inject.Inject

/** The tag whose name is being edited, with the name typed so far. */
data class TagEditState(
    val tagId: Long,
    val name: String,
)

/**
 * A question the tag manager puts to the writer before a change that cannot simply be undone: deleting a tag
 * from its entries, or merging two tags because the new name already belongs to one.
 *
 * The state carries the tags rather than the text, so the screen builds the message with the strings of the
 * language the app runs in while the alert stays up.
 */
sealed interface TagManagementAlert {
    data object EmptyName : TagManagementAlert

    data class DuplicateName(val name: String) : TagManagementAlert

    data class DeleteConfirm(
        val tag: Tag,
        val diaryCount: Int,
    ) : TagManagementAlert

    data class MergeConfirm(
        val source: Tag,
        val target: Tag,
    ) : TagManagementAlert
}

/**
 * State of the tag manager the settings open (issue #270).
 *
 * [tags] keeps every stored tag with the number of saved entries that carry it and follows the database, so a
 * change made here or by a tag picked while writing an entry shows up without reloading the screen.
 */
data class TagManagementUiState(
    val tags: List<TagWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val newTagName: String = "",
    val editing: TagEditState? = null,
    val alert: TagManagementAlert? = null,
)

@HiltViewModel
class TagManagementViewModel
    @Inject
    constructor(
        private val tagRepository: TagRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TagManagementUiState())
        val uiState: StateFlow<TagManagementUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                tagRepository.observeTagsWithCount().collect { tags ->
                    _uiState.update { it.copy(tags = tags, isLoading = false) }
                }
            }
        }

        fun updateNewTagName(name: String) {
            _uiState.update { it.copy(newTagName = name) }
        }

        /** Adds the tag typed in the field; a name another tag holds is answered instead of being added twice. */
        fun addTag() {
            val name = _uiState.value.newTagName
            viewModelScope.launch {
                when (val result = tagRepository.createTag(name)) {
                    is TagCreateResult.Created -> _uiState.update { it.copy(newTagName = "") }
                    is TagCreateResult.AlreadyExists -> _uiState.update { it.copy(alert = TagManagementAlert.DuplicateName(result.tag.name)) }
                    TagCreateResult.EmptyName -> _uiState.update { it.copy(alert = TagManagementAlert.EmptyName) }
                }
            }
        }

        fun startEditing(tag: Tag) {
            _uiState.update { it.copy(editing = TagEditState(tagId = tag.tagId, name = tag.name), alert = null) }
        }

        fun updateEditingName(name: String) {
            _uiState.update { state -> state.copy(editing = state.editing?.copy(name = name)) }
        }

        fun cancelEditing() {
            _uiState.update { it.copy(editing = null) }
        }

        /**
         * Writes the name typed in the row. A name another tag already holds is not written: the writer is asked
         * whether the two tags should be merged, and [confirmMerge] is the step that agrees to it.
         */
        fun confirmRename() {
            val editing = _uiState.value.editing ?: return
            viewModelScope.launch {
                when (val result = tagRepository.renameTag(editing.tagId, editing.name)) {
                    is TagRenameResult.Renamed -> _uiState.update { it.copy(editing = null) }
                    is TagRenameResult.NameTaken -> {
                        // The row stays open with the name that was typed, so a writer who cancels the merge
                        // can correct it instead of typing it again.
                        val source = _uiState.value.tags.firstOrNull { it.tag.tagId == editing.tagId }?.tag
                        _uiState.update {
                            it.copy(
                                alert = source?.let { tag -> TagManagementAlert.MergeConfirm(source = tag, target = result.existing) },
                            )
                        }
                    }

                    TagRenameResult.Unchanged -> _uiState.update { it.copy(editing = null) }
                    TagRenameResult.NotFound -> _uiState.update { it.copy(editing = null) }
                    TagRenameResult.EmptyName -> _uiState.update { it.copy(alert = TagManagementAlert.EmptyName) }
                }
            }
        }

        fun requestDelete(tagWithCount: TagWithCount) {
            _uiState.update { it.copy(alert = TagManagementAlert.DeleteConfirm(tag = tagWithCount.tag, diaryCount = tagWithCount.diaryCount)) }
        }

        fun confirmDelete() {
            val alert = _uiState.value.alert as? TagManagementAlert.DeleteConfirm ?: return
            viewModelScope.launch {
                tagRepository.deleteTag(alert.tag.tagId)
                _uiState.update { it.copy(alert = null) }
            }
        }

        fun confirmMerge() {
            val alert = _uiState.value.alert as? TagManagementAlert.MergeConfirm ?: return
            viewModelScope.launch {
                tagRepository.mergeTag(alert.source.tagId, alert.target.tagId)
                _uiState.update { it.copy(alert = null, editing = null) }
            }
        }

        fun dismissAlert() {
            _uiState.update { it.copy(alert = null) }
        }
    }
