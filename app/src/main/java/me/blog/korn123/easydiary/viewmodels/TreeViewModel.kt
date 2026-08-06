package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.TreeUtils.buildFileTreeFromDomain
import me.blog.korn123.commons.utils.TreeUtils.flattenTree
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_FICS
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSDAQ
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSPI
import me.blog.korn123.easydiary.helper.TreeConstants
import javax.inject.Inject

@HiltViewModel
class TreeViewModel
    @Inject
    constructor(
        private val diaryRepository: DiaryRepository,
    ) : ViewModel() {
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

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        fun setIsLoading(isLoading: Boolean) {
            _isLoading.value = isLoading
        }

        var isSelfDevelopmentRepository: Boolean = false

        val allDiaries: StateFlow<List<Diary>> =
            diaryRepository
                .getAllDiaries()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        init {
            combine(allDiaries, _currentQuery) { diaries, query ->
                diaries to query
            }.onEach { (diaries, query) ->
                if (isSelfDevelopmentRepository) fetchSelfDevelopmentRepoDiary(diaries, isLaunchedEffect = true) else fetchTimeLineDiary(diaries, isLaunchedEffect = true)
            }.launchIn(viewModelScope)
        }

        fun fetchSelfDevelopmentRepoDiary(
            diaries: List<Diary> = allDiaries.value,
            isLaunchedEffect: Boolean = false,
        ) {
            setIsLoading(true)
            viewModelScope.launch(Dispatchers.Default) {
                if (isLaunchedEffect) delay(500)
                val syncSymbols =
                    listOf(
                        DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS,
                        DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSPI,
                        DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSDAQ,
                        DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_FICS,
                        DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF,
                    )
                val query = _currentQuery.value
                val filteredDiaries =
                    diaries
                        .filter { diary ->
                            diary.symbolSequence in syncSymbols &&
                                (
                                    query.isEmpty() ||
                                        diary.title?.contains(
                                            query,
                                            ignoreCase = true,
                                        ) == true ||
                                        diary.contents?.contains(
                                            query,
                                            ignoreCase = true,
                                        ) == true
                                )
                        }.sortedBy { it.title }

                val fileNode =
                    buildFileTreeFromDomain(
                        items = filteredDiaries,
                    ) { diary ->
                        diary.title!!.split("/").toMutableList()
                    }
                val newTreeData = flattenTree(fileNode)
                val originTreeData = _treeData.value
                val mappedTreeData =
                    newTreeData.map { pair ->
                        pair.apply {
                            if (second == TreeConstants.LEVEL_START) first.isShow = true

                            // 이전 상태 유지
                            val originNode = originTreeData.find { it.first.fullPath == first.fullPath }
                            if (originNode != null) {
                                first.isFolderOpen = originNode.first.isFolderOpen
                                first.isShow = originNode.first.isShow
                                first.isParentFolderOpen = originNode.first.isParentFolderOpen
                            }
                        }
                    }
                withContext(Dispatchers.Main) {
                    setTreeData(mappedTreeData)
                    setTotal(filteredDiaries.size)
                    setIsLoading(false)
                }
            }
        }

        fun fetchTimeLineDiary(
            diaries: List<Diary> = allDiaries.value,
            isLaunchedEffect: Boolean = false,
        ) {
            setIsLoading(true)
            viewModelScope.launch(Dispatchers.Default) {
                if (isLaunchedEffect) delay(500)
                val query = _currentQuery.value
                val filteredDiaries =
                    diaries
                        .filter { diary ->
                            (
                                query.isEmpty() ||
                                    diary.title?.contains(query, ignoreCase = true) == true ||
                                    diary.contents?.contains(query, ignoreCase = true) == true
                            )
                        }.sortedBy { it.title }

                val fileNode =
                    buildFileTreeFromDomain(
                        items = filteredDiaries,
                        addOptionalTitle = true,
                        addOptionalSortPrefix = true,
                    ) { diary ->
                        "${diary.dateString}".split("-").toMutableList()
                    }
                val newTreeData =
                    flattenTree(node = fileNode, sortOption = TreeConstants.SORT_OPTION_ASC)
                val originTreeData = _treeData.value
                val mappedTreeData =
                    newTreeData.map { pair ->
                        pair.apply {
                            if (second == TreeConstants.LEVEL_START) first.isShow = true

                            // 이전 상태 유지
                            val originNode = originTreeData.find { it.first.fullPath == first.fullPath }
                            if (originNode != null) {
                                first.isFolderOpen = originNode.first.isFolderOpen
                                first.isShow = originNode.first.isShow
                                first.isParentFolderOpen = originNode.first.isParentFolderOpen
                            }
                        }
                    }
                withContext(Dispatchers.Main) {
                    setTreeData(mappedTreeData)
                    setTotal(filteredDiaries.size)
                    setIsLoading(false)
                }
            }
        }
    }
