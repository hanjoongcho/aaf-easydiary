package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.ui.models.DiaryUiModel

class DiaryViewModel : ViewModel() {
    private val _parentDiaries =
        MutableStateFlow<List<DiaryUiModel>>(emptyList())

    val parentDiaries: StateFlow<List<DiaryUiModel>> =
        _parentDiaries.asStateFlow()

    fun updateParentDiaries(diaries: List<Diary>) {
        _parentDiaries.value = diaries.map { it.toUiModel() }
    }

    private val _childDiaries =
        MutableStateFlow<List<DiaryUiModel>>(emptyList())

    val childDiaries: StateFlow<List<DiaryUiModel>> =
        _childDiaries.asStateFlow()

    fun updateChildDiaries(diaries: List<Diary>) {
        _childDiaries.value = diaries.map { it.toUiModel() }
    }
}