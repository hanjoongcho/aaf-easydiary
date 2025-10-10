package me.blog.korn123.easydiary.compose

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.snackbar.Snackbar
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.TreeUtils
import me.blog.korn123.commons.utils.TreeUtils.buildFileTree
import me.blog.korn123.commons.utils.TreeUtils.flattenTree
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.showBetaFeatureMessage
import me.blog.korn123.easydiary.extensions.updateSystemStatusBarColor
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
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
        setContent {
            mSettingsViewModel = initSettingsViewModel()
            TreeTimeline()
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
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TreeTimeline() {
        val context = LocalContext.current
        mSettingsViewModel = initSettingsViewModel()
        LocalActivity.current?.updateSystemStatusBarColor(LocalContext.current.config.primaryColor)

        val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
            context.config.enableCardViewPolicy
        )
        val currentQuery: String by treeViewModel.currentQuery.observeAsState("")
        val treeData: List<Pair<FileNode, Int>> by treeViewModel.treeData.observeAsState(emptyList())
        val total: Int by treeViewModel.total.observeAsState(0)

        fun toggleWholeTree(isExpand: Boolean) {
            treeViewModel.setTreeData(TreeUtils.toggleWholeTree(treeData, isExpand))
        }

        fun toggleChildren(fileNode: FileNode) {
            treeViewModel.setTreeData(TreeUtils.toggleChildren(treeData, fileNode))
        }

        fetchDiary()

        AppTheme {
            Scaffold(
                // 하단 패딩은 수동 관리
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                containerColor = Color(config.primaryColor),
                content = { innerPadding ->
                    TreeContent(
                        innerPadding = innerPadding,
                        enableCardViewPolicy = enableCardViewPolicy,
                        isReverseMode = true,
                        total = total,
                        treeData = treeData,
                        currentQuery = currentQuery,
                        fetchDiary = { fetchDiary() },
                        updateQuery = { treeViewModel.setCurrentQuery(it) },
                        toggleWholeTree = { toggleWholeTree(it) },
                        folderOnClick = { node ->
                            val newFirst =
                                node.copy(isFolderOpen = node.isFolderOpen.not())
                            // pair 객체가 리컴포지션 되도록
                            treeViewModel.setTreeData(treeData.map { data ->
                                if (data.first.fullPath == node.fullPath) {
                                    data.copy(first = newFirst)
                                } else {
                                    data
                                }
                            })
                            // 폴더인 경우, 열고 닫기 토글
                            toggleChildren(newFirst)
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
                list.add(Diary().apply { sequence = 1; dateString = "2023-01-01"; title = "New Year" })
                list.add(Diary().apply { sequence = 2; dateString = "2023-02-01"; title = "New Year Party" })
                return list
            }
            fun fetchDiary() {
                val diaryItems = findDiary()
                val fileNode = buildFileTree(diaryItems, addOptionalTitle = true) {
                        diary ->  "${diary.dateString}".split("-").toMutableList()
                }
                val originTreeData = flattenTree(fileNode, sortOption = "desc")
                treeData = originTreeData.map { pair ->
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
                    )
                },
            )
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/

    fun findDiary(): List<Diary> {
        return EasyDiaryDbHelper.findDiary(query = treeViewModel.currentQuery.value, checkFutureDiaryOption = true)
    }

    fun fetchDiary() {
        val diaryItems = findDiary()
        val fileNode = buildFileTree(diaryItems, addOptionalTitle = true) {
                diary ->  "${diary.dateString}".split("-").toMutableList()
        }
        val originTreeData = flattenTree(fileNode, sortOption = "asc")
        treeViewModel.setTreeData(treeData = originTreeData.map { pair ->
            if (pair.second == 1) pair.first.isShow = true
            pair
        })
        treeViewModel.setTotal(diaryItems.size)
    }
}


