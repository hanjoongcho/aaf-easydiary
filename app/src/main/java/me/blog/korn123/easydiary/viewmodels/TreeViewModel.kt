package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.blog.korn123.commons.utils.FileNode

class TreeViewModel : ViewModel() {
    /***************************************************************************************************
     *   Tree Component
     *
     ***************************************************************************************************/
    private val _treeData: MutableLiveData<List<Pair<FileNode, Int>>> = MutableLiveData()
    val treeData: LiveData<List<Pair<FileNode, Int>>> get() = _treeData
    fun setTreeData(treeData: List<Pair<FileNode, Int>>) { _treeData.value = treeData }

    private val _total: MutableLiveData<Int> = MutableLiveData()
    val total: LiveData<Int> get() = _total
    fun setTotal(total: Int) { _total.value = total }

    private val _currentQuery: MutableLiveData<String> = MutableLiveData()
    val currentQuery: LiveData<String> get() = _currentQuery
    fun setCurrentQuery(currentQuery: String) { _currentQuery.value = currentQuery }
}