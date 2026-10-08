package me.blog.korn123.easydiary.presentation.dev

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.TreeUtils
import me.blog.korn123.easydiary.extensions.applyFullScreenStatusBarTheme
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.tree.TreeViewModel
import me.blog.korn123.easydiary.ui.components.LoadingScreen
import me.blog.korn123.easydiary.ui.components.TreeContent
import me.blog.korn123.easydiary.ui.theme.AppTheme

@AndroidEntryPoint
class SelfDevelopmentRepoActivity : EasyDiaryComposeBaseActivity() {
    private val treeViewModel: TreeViewModel by viewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SelfDevelopmentRepo()
        }
    }

    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @Composable
    fun SelfDevelopmentRepo() {
        LocalActivity.current?.applyFullScreenStatusBarTheme()

        val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.collectAsState()
        val currentQuery: String by treeViewModel.currentQuery.collectAsState()
        val treeData: List<Pair<FileNode, Int>> by treeViewModel.treeData.collectAsState()
        val total: Int by treeViewModel.total.collectAsState()
        val isLoading: Boolean by treeViewModel.isLoading.collectAsState()
        val allDiaries by treeViewModel.allDiaries.collectAsState()

        LaunchedEffect(Unit) {
            treeViewModel.isSelfDevelopmentRepository = true
        }

        SelfDevelopmentRepoContent(
            enableCardViewPolicy = enableCardViewPolicy,
            currentQuery = currentQuery,
            treeData = treeData,
            total = total,
            isLoading = isLoading,
            onRefresh = { treeViewModel.fetchSelfDevelopmentRepoDiary(allDiaries) },
            onQueryChange = { treeViewModel.setCurrentQuery(it) },
            backgroundColor = Color(config.screenBackgroundColor),
            onToggleWholeTree = { isExpand ->
                treeViewModel.setTreeData(TreeUtils.toggleWholeTree(treeData, isExpand))
            },
            onFolderClick = { node ->
                treeViewModel.setTreeData(TreeUtils.toggleChildren(treeData, node))
            },
        )
    }

    @Composable
    fun SelfDevelopmentRepoContent(
        enableCardViewPolicy: Boolean = false,
        currentQuery: String = "",
        treeData: List<Pair<FileNode, Int>> = emptyList(),
        total: Int = 0,
        isLoading: Boolean = false,
        backgroundColor: Color = Color.White,
        onRefresh: () -> Unit,
        onQueryChange: (String) -> Unit,
        onToggleWholeTree: (Boolean) -> Unit,
        onFolderClick: (FileNode) -> Unit,
    ) {
        AppTheme {
            Scaffold(
                // 하단 패딩은 수동 관리
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                containerColor = backgroundColor,
                content = { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        TreeContent(
                            innerPadding = innerPadding,
                            enableCardViewPolicy = enableCardViewPolicy,
                            total = total,
                            treeData = treeData,
                            currentQuery = currentQuery,
                            fetchDiary = onRefresh,
                            updateQuery = onQueryChange,
                            toggleWholeTree = onToggleWholeTree,
                            folderOnClick = onFolderClick,
                            resultAPICallback = {},
                        )

                        AnimatedVisibility(
                            visible = isLoading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            LoadingScreen()
                        }
                    }
                },
            )
        }
    }
}
