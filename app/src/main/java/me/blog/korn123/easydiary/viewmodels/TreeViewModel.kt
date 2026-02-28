package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.blog.korn123.commons.utils.FileNode

class TreeViewModel : ViewModel() {
    /***************************************************************************************************
     *   Tree Component
     *
     ***************************************************************************************************/
    private val _treeData = MutableStateFlow<List<Pair<FileNode, Int>>>(emptyList())
    val treeData: StateFlow<List<Pair<FileNode, Int>>> = _treeData.asStateFlow()

    fun setTreeData(treeData: List<Pair<FileNode, Int>>) {
        _treeData.value = treeData
    }

    private val _total = MutableStateFlow(0)
    val total: StateFlow<Int> = _total.asStateFlow()

    fun setTotal(total: Int) {
        _total.value = total
    }

    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()

    fun setCurrentQuery(currentQuery: String) {
        _currentQuery.value = currentQuery
    }
}
