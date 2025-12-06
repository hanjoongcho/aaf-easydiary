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
import androidx.compose.material3.FabPosition
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
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.TreeUtils
import me.blog.korn123.commons.utils.TreeUtils.buildFileTree
import me.blog.korn123.commons.utils.TreeUtils.flattenTree
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateSystemStatusBarColor
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_FICS
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSDAQ
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSPI
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.ui.components.TreeContent
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.TreeViewModel
import kotlin.getValue

class SelfDevelopmentRepoActivity : EasyDiaryComposeBaseActivity() {
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
            SelfDevelopmentRepo()
        }
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
    fun SelfDevelopmentRepo() {
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
                floatingActionButtonPosition = FabPosition.Center,
            )
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    fun findDiary(): List<Diary> {
        EasyDiaryDbHelper.forceRefresh()
        return EasyDiaryDbHelper.findDiary( treeViewModel.currentQuery.value,
            false,
            listOf(DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS,
                DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSPI,
                DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSDAQ,
                DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_FICS,
                DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF,
            )).sortedBy { diary -> diary.title }
    }

    fun fetchDiary() {
        val diaryItems = findDiary()
        val fileNode = buildFileTree(diaryItems) { diary ->
            diary.title!!.split("/").toMutableList()
        }
        val newTreeData = flattenTree(fileNode)
        val originTreeData = treeViewModel.treeData.value
        treeViewModel.setTreeData(treeData = newTreeData.map { pair ->
            if (pair.second == 1) pair.first.isShow = true

            // 이전 상태 유지
            val originNode = originTreeData?.find { it.first.fullPath == pair.first.fullPath }
            if (originNode != null) {
                pair.first.isFolderOpen = originNode.first.isFolderOpen
                pair.first.isShow = originNode.first.isShow
                pair.first.isRootShow = originNode.first.isRootShow
            }

            pair
        })
//                        total = diaryItems.filter { diary ->  diary.title!!.endsWith(".md") }.size
        treeViewModel.setTotal(diaryItems.size)
    }
}


