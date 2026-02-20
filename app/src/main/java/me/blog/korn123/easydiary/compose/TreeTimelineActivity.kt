package me.blog.korn123.easydiary.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.TreeUtils
import me.blog.korn123.commons.utils.TreeUtils.buildFileTree
import me.blog.korn123.commons.utils.TreeUtils.flattenTree
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.showBetaFeatureMessage
import me.blog.korn123.easydiary.extensions.updateSystemStatusBarColor
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.IS_TREE_TIMELINE_LAUNCH_MODE_DEFAULT
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.ui.components.TreeContent
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.TreeViewModel

class TreeTimelineActivity : EasyDiaryComposeBaseActivity() {
    val treeViewModel: TreeViewModel by viewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isResultAPI = !intent.getBooleanExtra(IS_TREE_TIMELINE_LAUNCH_MODE_DEFAULT, true)
        setContent {
            TreeTimeline(isResultAPI = isResultAPI)
        }
        showBetaFeatureMessage()
    }

    override fun onResume() {
        super.onResume()
        fetchDiary()
    }

    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @Composable
    fun TreeTimeline(isResultAPI: Boolean = false) {
        val context = LocalContext.current
        LocalActivity.current?.updateSystemStatusBarColor()

        val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
            context.config.enableCardViewPolicy,
        )
        val currentQuery: String by treeViewModel.currentQuery.collectAsState()
        val treeData: List<Pair<FileNode, Int>> by treeViewModel.treeData.collectAsState()
        val total: Int by treeViewModel.total.collectAsState()

        fun toggleWholeTree(isExpand: Boolean) {
            treeViewModel.setTreeData(TreeUtils.toggleWholeTree(treeData, isExpand))
        }

        fun toggleChildren(selectedNode: FileNode) {
            treeViewModel.setTreeData(TreeUtils.toggleChildren(treeData, selectedNode))
        }

        fetchDiary()

        AppTheme {
            Scaffold(
                // 하단 패딩은 수동 관리
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                containerColor = Color(config.screenBackgroundColor),
                content = { innerPadding ->
                    TreeContent(
                        innerPadding = innerPadding,
                        enableCardViewPolicy = enableCardViewPolicy,
                        isReverseMode = true,
                        showDebugCard = false,
                        total = total,
                        treeData = treeData,
                        currentQuery = currentQuery,
                        isResultAPI = isResultAPI,
                        fetchDiary = { fetchDiary() },
                        updateQuery = { treeViewModel.setCurrentQuery(it) },
                        toggleWholeTree = { toggleWholeTree(it) },
                        folderOnClick = { node ->
                            // 폴더인 경우, 열고 닫기 토글
                            toggleChildren(node)
                        },
                        resultAPICallback = { sequence ->
                            val resultIntent =
                                Intent().apply {
                                    putExtra("sequence", sequence)
                                }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        },
                    )
                },
            )
        }
    }

    @Composable
    @Preview(heightDp = 800)
    private fun TreeTimelinePreview() {
        AppTheme {
            var total by remember { mutableIntStateOf(0) }
            var treeData by remember { mutableStateOf(emptyList<Pair<FileNode, Int>>()) }

            fun findDiary(): List<Diary> {
                val list = mutableListOf<Diary>()
                list.add(
                    Diary().apply {
                        sequence = 1
                        dateString = "2023-01-01"
                        title = "New Year"
                    },
                )
                list.add(
                    Diary().apply {
                        sequence = 2
                        dateString = "2023-02-01"
                        title = "New Year Party"
                    },
                )
                return list
            }

            fun fetchDiary() {
                val diaryItems = findDiary()
                val fileNode =
                    buildFileTree(diaryItems, addOptionalTitle = true) { diary ->
                        "${diary.dateString}".split("-").toMutableList()
                    }
                val originTreeData = flattenTree(fileNode, sortOption = "desc")
                treeData =
                    originTreeData.map { pair ->
                        if (pair.second == 1) pair.first.isShow = true
                        pair
                    }
                total = diaryItems.size
            }
            fetchDiary()
            Scaffold(
                // 하단 패딩은 수동 관리
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                content = { innerPadding ->
                    TreeContent(
                        innerPadding = innerPadding,
                        total = total,
                        treeData = treeData,
                        currentQuery = "",
                        fetchDiary = { fetchDiary() },
                        updateQuery = {},
                        toggleWholeTree = {},
                        folderOnClick = {},
                        resultAPICallback = { /* no-op */ },
                    )
                },
            )
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/

    fun findDiary(): List<Diary> =
        EasyDiaryDbHelper.findDiary(
            query = treeViewModel.currentQuery.value,
            checkFutureDiaryOption = true,
        )

    fun fetchDiary() {
        val diaryItems = findDiary()
        val fileNode =
            buildFileTree(
                diaryItems,
                addOptionalTitle = true,
                addOptionalSortPrefix = true,
            ) { diary ->
                "${diary.dateString}".split("-").toMutableList()
            }
        val newTreeData = flattenTree(fileNode, sortOption = "asc")
        val originTreeData = treeViewModel.treeData.value
        treeViewModel.setTreeData(
            treeData =
                newTreeData.map { pair ->
                    if (pair.second == 1) pair.first.isShow = true

                    // 이전 상태 유지
                    val originNode = originTreeData?.find { it.first.fullPath == pair.first.fullPath }
                    if (originNode != null) {
                        pair.first.isFolderOpen = originNode.first.isFolderOpen
                        pair.first.isShow = originNode.first.isShow
                        pair.first.isRootShow = originNode.first.isRootShow
                    }

                    pair
                },
        )
        treeViewModel.setTotal(diaryItems.size)
    }
}
