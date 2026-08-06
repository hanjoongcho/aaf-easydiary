package me.blog.korn123.easydiary.compose

import android.content.Intent
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.TreeUtils
import me.blog.korn123.easydiary.extensions.applyFullScreenStatusBarTheme
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.TreeConstants.IS_TREE_TIMELINE_LAUNCH_MODE_DEFAULT
import me.blog.korn123.easydiary.ui.components.LoadingScreen
import me.blog.korn123.easydiary.ui.components.TreeContent
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.TreeViewModel

@AndroidEntryPoint
class TreeTimelineActivity : EasyDiaryComposeBaseActivity() {
    val treeViewModel: TreeViewModel by viewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isResultAPI = intent.getBooleanExtra(IS_TREE_TIMELINE_LAUNCH_MODE_DEFAULT, true).not()
        setContent {
            TreeTimeline(isResultAPI = isResultAPI)
        }
    }

    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @Composable
    fun TreeTimeline(isResultAPI: Boolean = false) {
        LocalActivity.current?.applyFullScreenStatusBarTheme()

        val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.collectAsState()
        val currentQuery: String by treeViewModel.currentQuery.collectAsState()
        val treeData: List<Pair<FileNode, Int>> by treeViewModel.treeData.collectAsState()
        val total: Int by treeViewModel.total.collectAsState()
        val isLoading: Boolean by treeViewModel.isLoading.collectAsState()

        TreeTimelineContent(
            isResultAPI = isResultAPI,
            enableCardViewPolicy = enableCardViewPolicy,
            currentQuery = currentQuery,
            treeData = treeData,
            total = total,
            isLoading = isLoading,
            onRefresh = { treeViewModel.fetchTimeLineDiary() },
            onQueryChange = { treeViewModel.setCurrentQuery(it) },
            backgroundColor = Color(config.screenBackgroundColor),
            onToggleWholeTree = { isExpand ->
                treeViewModel.setTreeData(TreeUtils.toggleWholeTree(treeData, isExpand))
            },
            onFolderClick = { node ->
                treeViewModel.setTreeData(TreeUtils.toggleChildren(treeData, node))
            },
            onResultAPICallback = { sequence ->
                val resultIntent =
                    Intent().apply {
                        putExtra("sequence", sequence)
                    }
                setResult(RESULT_OK, resultIntent)
                finish()
            },
        )
    }

    @Composable
    fun TreeTimelineContent(
        isResultAPI: Boolean = false,
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
        onResultAPICallback: (Int) -> Unit = {},
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
                            showDebugCard = false,
                            total = total,
                            treeData = treeData,
                            currentQuery = currentQuery,
                            isResultAPI = isResultAPI,
                            fetchDiary = onRefresh,
                            updateQuery = onQueryChange,
                            toggleWholeTree = onToggleWholeTree,
                            folderOnClick = onFolderClick,
                            resultAPICallback = onResultAPICallback,
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
    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}
