package me.blog.korn123.easydiary.presentation.photoviewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import javax.inject.Inject

data class PhotoViewPagerUiState(
    val diary: Diary? = null,
    val initialPosition: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class PhotoViewPagerViewModel @Inject constructor(
    application: Application,
    private val diaryRepository: DiaryRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PhotoViewPagerUiState())
    val uiState: StateFlow<PhotoViewPagerUiState> = _uiState.asStateFlow()

    fun initData(diarySequence: Long, initialPosition: Int) {
        _uiState.update { it.copy(initialPosition = initialPosition) }
        loadDiary(diarySequence)
    }

    private fun loadDiary(diarySequence: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val diary = diaryRepository.observeDiaryWithPhotosById(diarySequence).firstOrNull()
            _uiState.update {
                it.copy(
                    diary = diary,
                    isLoading = false,
                )
            }
        }
    }
}
