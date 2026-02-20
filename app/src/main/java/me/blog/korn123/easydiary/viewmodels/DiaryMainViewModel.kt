package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL

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
}
