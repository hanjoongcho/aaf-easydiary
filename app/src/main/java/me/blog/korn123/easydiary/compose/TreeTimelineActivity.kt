package me.blog.korn123.easydiary.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.TreeUtils
import me.blog.korn123.commons.utils.TreeUtils.buildFileTree
import me.blog.korn123.commons.utils.TreeUtils.flattenTree
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.updateSystemStatusBarColor
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.ui.components.BottomToolBar
import me.blog.korn123.easydiary.ui.components.OptionDialog
import me.blog.korn123.easydiary.ui.components.TreeCard
import me.blog.korn123.easydiary.ui.components.TreeContent
import me.blog.korn123.easydiary.ui.components.TreeToolbar
import me.blog.korn123.easydiary.ui.theme.AppTheme

class TreeTimelineActivity : EasyDiaryComposeBaseActivity() {


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
        LocalActivity.current?.updateSystemStatusBarColor()

        val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
            context.config.enableCardViewPolicy
        )
        var currentQuery by remember { mutableStateOf("") }
        var treeData by remember { mutableStateOf(emptyList<Pair<FileNode, Int>>()) }
        var total by remember { mutableIntStateOf(0) }

        fun toggleWholeTree(isExpand: Boolean) {
            treeData = TreeUtils.toggleWholeTree(treeData, isExpand)
        }

        fun toggleChildren(fileNode: FileNode) {
            treeData = TreeUtils.toggleChildren(treeData, fileNode)
        }

        fun findDiary(): List<Diary> {
            return EasyDiaryDbHelper.findDiary(currentQuery)
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

        AppTheme {
            Scaffold(
                // 하단 패딩은 수동 관리
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                containerColor = Color(config.screenBackgroundColor),
                content = { innerPadding ->
                    TreeContent(
                        innerPadding = innerPadding,
                        enableCardViewPolicy = enableCardViewPolicy,
                        total = total,
                        treeData = treeData,
                        currentQuery = currentQuery,
                        fetchDiary = { fetchDiary() },
                        updateQuery = { currentQuery = it },
                        toggleWholeTree = { toggleWholeTree(it) },
                        folderOnClick = { node ->
                            val newFirst =
                                node.copy(isFolderOpen = node.isFolderOpen.not())
                            // pair 객체가 리컴포지션 되도록
                            treeData = treeData.map { data ->
                                if (data.first.fullPath == node.fullPath) {
                                    data.copy(first = newFirst)
                                } else {
                                    data
                                }
                            }
                            // 폴더인 경우, 열고 닫기 토글
                            toggleChildren(newFirst)
                        },
                    )
                },
//                floatingActionButton = {
//                    FloatingActionButton(
//                        onClick = { finishActivityWithTransition() },
//                        containerColor = Color(LocalContext.current.config.primaryColor),
//                        contentColor = Color.White,
//                        shape = RoundedCornerShape(12.dp),
//                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
//                        modifier = Modifier.size(40.dp)
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_cross),
//                            contentDescription = "Finish Activity"
//                        )
//                    }
//                },
//                floatingActionButtonPosition = FabPosition.Center,
            )
        }
    }

    @Composable
    @Preview(heightDp = 600)
    private fun TreeTimelinePreview() {
        AppTheme {
            val context = LocalContext.current
            val configuration = LocalConfiguration.current
            val settingCardModifier = Modifier.fillMaxWidth()
            val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
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
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {  },
                        containerColor = Color(LocalContext.current.config.primaryColor),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        elevation = FloatingActionButtonDefaults.elevation(8.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "Finish Activity"
                        )
                    }
                },
                bottomBar = {
                    BottomToolBar(
                        bottomPadding = bottomPadding,
                        showOptionDialog = {  },
                    )
                },
                floatingActionButtonPosition = FabPosition.Center,
            )
        }
    }
}


