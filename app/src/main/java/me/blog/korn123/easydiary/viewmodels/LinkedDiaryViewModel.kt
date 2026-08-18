package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.blog.korn123.easydiary.domain.model.Diary

class LinkedDiaryViewModel : ViewModel() {
    private val _parentDiaries = MutableStateFlow<List<Diary>>(emptyList())
    val parentDiaries: StateFlow<List<Diary>> =
        _parentDiaries.asStateFlow()

    fun updateParentDiaries(diaries: List<Diary>) {
        _parentDiaries.value = diaries
    }

    private val _childDiaries = MutableStateFlow<List<Diary>>(emptyList())
    val childDiaries: StateFlow<List<Diary>> =
        _childDiaries.asStateFlow()

    fun updateChildDiaries(diaries: List<Diary>) {
        _childDiaries.value = diaries
    }
}
