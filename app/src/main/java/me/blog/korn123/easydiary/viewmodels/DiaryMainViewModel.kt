package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.models.Diary

class DiaryMainViewModel : ViewModel() {
    private val _symbol: MutableStateFlow<Int> = MutableStateFlow(SYMBOL_SELECT_ALL)
    val symbol: StateFlow<Int> = _symbol.asStateFlow()

    fun updateSymbolSequence(symbolSequence: Int) {
        _symbol.value = symbolSequence
    }

    private val _isReady: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    fun markAsReady() {
        _isReady.value = true
    }

    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()

    fun setCurrentQuery(currentQuery: String) {
        _currentQuery.value = currentQuery
    }

    private val _diaryItems = MutableStateFlow<List<Diary>>(EasyDiaryDbHelper.findDiary(null))
    val diaryItems: StateFlow<List<Diary>> = _diaryItems.asStateFlow()

    fun setDiaryItems(items: List<Diary>) {
        _diaryItems.value = items
    }

    fun findDiary(query: String? = null) {
        setCurrentQuery(query ?: "")
        setDiaryItems(
            EasyDiaryDbHelper.findDiary(query),
        )
    }
}
