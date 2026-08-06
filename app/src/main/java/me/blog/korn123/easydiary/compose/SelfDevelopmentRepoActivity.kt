package me.blog.korn123.easydiary.compose

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
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.TreeUtils
import me.blog.korn123.easydiary.extensions.applyFullScreenStatusBarTheme
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.ui.components.LoadingScreen
import me.blog.korn123.easydiary.ui.components.TreeContent
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.TreeViewModel

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
        val activity = LocalActivity.current
        val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.collectAsState()
        val currentQuery: String by treeViewModel.currentQuery.collectAsState()
        val treeData: List<Pair<FileNode, Int>> by treeViewModel.treeData.collectAsState()
        val total: Int by treeViewModel.total.collectAsState()
        val isLoading: Boolean by treeViewModel.isLoading.collectAsState()
        val allDiaries by treeViewModel.allDiaries.collectAsState()
        treeViewModel.isSelfDevelopmentRepository = true

        LaunchedEffect(Unit) {
            activity?.applyFullScreenStatusBarTheme()
        }

        SelfDevelopmentRepoContent(
            enableCardViewPolicy = enableCardViewPolicy,
            currentQuery = currentQuery,
            treeData = treeData,
            total = total,
            isLoading = isLoading,
            onRefresh = { treeViewModel.fetchSelfDevelopmentRepoDiary(allDiaries) },
            onQueryChange = { treeViewModel.setCurrentQuery(it) },
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
        enableCardViewPolicy: Boolean,
        currentQuery: String,
        treeData: List<Pair<FileNode, Int>>,
        total: Int,
        isLoading: Boolean,
        onRefresh: () -> Unit,
        onQueryChange: (String) -> Unit,
        onToggleWholeTree: (Boolean) -> Unit,
        onFolderClick: (FileNode) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val context = LocalContext.current
        AppTheme {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                containerColor = Color(context.config.screenBackgroundColor),
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
                            resultAPICallback = { /* no-op */ },
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
                floatingActionButtonPosition = FabPosition.Center,
                modifier = modifier,
            )
        }
    }
}
