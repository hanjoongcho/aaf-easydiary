package me.blog.korn123.easydiary.helper

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.blog.korn123.easydiary.domain.model.History

@Singleton
class PhotoHighlightManager @Inject constructor() {
    private val _historyList = MutableStateFlow<List<History>>(emptyList())
    val historyList: StateFlow<List<History>> = _historyList.asStateFlow()

    private var isCalculated = false

    fun isCalculated(): Boolean = isCalculated

    fun updateHistory(histories: List<History>) {
        _historyList.value = histories
        isCalculated = true
    }

    fun invalidateCache() {
        isCalculated = false
        _historyList.value = emptyList()
    }
}
