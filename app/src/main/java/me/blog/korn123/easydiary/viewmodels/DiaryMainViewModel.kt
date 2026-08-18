package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

class DiaryMainViewModel : ViewModel() {
    init {
        viewModelScope.launch {
            delay(500L)
            markAsReady()
        }
    }

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
}
