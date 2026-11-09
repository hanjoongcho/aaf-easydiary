package me.blog.korn123.easydiary.presentation.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.adapters.GalleryAdapter
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import java.io.File
import javax.inject.Inject

data class GalleryViewPagerUiState(
    val attachedPhotos: List<GalleryAdapter.AttachedPhoto> = emptyList(),
    val initialPosition: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class GalleryViewPagerViewModel @Inject constructor(
    application: Application,
    private val diaryRepository: DiaryRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GalleryViewPagerUiState())
    val uiState: StateFlow<GalleryViewPagerUiState> = _uiState.asStateFlow()

    fun initData(initialPosition: Int) {
        _uiState.update { it.copy(initialPosition = initialPosition) }
        loadAttachedPhotos()
    }

    private fun loadAttachedPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val photos = fetchAttachedPhotos()
            _uiState.update {
                it.copy(
                    attachedPhotos = photos,
                    isLoading = false,
                )
            }
        }
    }

    private suspend fun fetchAttachedPhotos(): List<GalleryAdapter.AttachedPhoto> =
        withContext(Dispatchers.IO) {
            val photoDirectory =
                File(EasyDiaryUtils.getApplicationDataDirectory(getApplication()) + DIARY_PHOTO_DIRECTORY)
            val files = photoDirectory.listFiles() ?: return@withContext emptyList()

            val diaryMap = mutableMapOf<String, Diary>()
            diaryRepository.getDiariesWithPhotos().forEach { diary ->
                diary.photoUris.forEach { photo ->
                    photo.photoUri?.let { uri ->
                        val fileName = uri.substringAfterLast('/')
                        if (!diaryMap.containsKey(fileName)) {
                            diaryMap[fileName] = diary
                        }
                    }
                }
            }

            files
                .map { file ->
                    val diary = diaryMap[file.name]
                    GalleryAdapter.AttachedPhoto(file, false, diary)
                }
                .sortedByDescending { item ->
                    item.diary?.currentTimeMillis ?: 0
                }
        }
}
