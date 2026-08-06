package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel
    @Inject
    constructor(
        private val diaryRepository: DiaryRepository,
    ) : ViewModel() {
        val allDiaries: StateFlow<List<Diary>> =
            diaryRepository
                .getAllDiaries()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        val diaryCount: StateFlow<Int> =
            allDiaries
                .map { it.size }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0,
                )

        fun addDiary(diary: Diary) {
            viewModelScope.launch {
                diaryRepository.insertDiary(diary)
            }
        }

        suspend fun insertDiary(diary: Diary) {
            diaryRepository.insertDiary(diary)
        }

        suspend fun addAllDiaries(diaries: List<Diary>) {
            diaryRepository.addAllDiaries(diaries)
        }

        fun updateDiary(diary: Diary) {
            viewModelScope.launch {
                diaryRepository.updateDiary(diary)
            }
        }

        fun deleteDiary(diary: Diary) {
            viewModelScope.launch {
                diaryRepository.deleteDiary(diary)
            }
        }

        fun deleteDiaryById(seq: Int) {
            viewModelScope.launch {
                diaryRepository.deleteDiaryById(seq)
            }
        }

        fun deleteAllDiaries() {
            viewModelScope.launch {
                diaryRepository.deleteAllDiaries()
            }
        }

        suspend fun getDiaryCount(): Int = diaryRepository.getAllDiaries().first().size

        suspend fun getLatestDiary(): Diary? =
            diaryRepository
                .getAllDiaries()
                .first()
                .firstOrNull { diary -> diary.photoUris.isNotEmpty() }

        suspend fun getLatestDiaryWithPhotos(): Diary? =
            diaryRepository
                .getDiariesWithPhotos()
                .firstOrNull { diary -> diary.photoUris.isNotEmpty() }

        suspend fun getPhotoUriCount(): Int = diaryRepository.getPhotoUris().first().size
    }
