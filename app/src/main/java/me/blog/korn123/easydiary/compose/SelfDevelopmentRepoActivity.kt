package me.blog.korn123.easydiary.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.FileNode
import me.blog.korn123.commons.utils.TreeUtils.buildFileTree
import me.blog.korn123.commons.utils.TreeUtils.flattenTree
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.syncMarkDown
import me.blog.korn123.easydiary.extensions.updateSystemStatusBarColor
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_FICS
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSDAQ
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSPI
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.ui.components.BottomToolBar
import me.blog.korn123.easydiary.ui.components.OptionDialog
import me.blog.korn123.easydiary.ui.components.TreeCard
import me.blog.korn123.easydiary.ui.components.TreeToolbar
import me.blog.korn123.easydiary.ui.theme.AppTheme

class SelfDevelopmentRepoActivity : EasyDiaryComposeBaseActivity() {


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


    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SelfDevelopmentRepo() {
        mSettingsViewModel = initSettingsViewModel()
        LocalActivity.current?.updateSystemStatusBarColor()
        val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
        var showOptionDialog by remember { mutableStateOf(false) }
        var visibleSubTitle by remember { mutableStateOf(false) }

        var isLoading by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val settingCardModifier = Modifier.fillMaxWidth()
        val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
            context.config.enableCardViewPolicy
        )
        var currentQuery by remember { mutableStateOf("") }
        fun findDiary(): List<Diary> {
            return EasyDiaryDbHelper.findDiary( currentQuery,
                false,
                listOf(DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS,
                    DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSPI,
                    DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSDAQ,
                    DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_FICS,
                    DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF,
                )).sortedBy { diary -> diary.title }
        }
//                    fun findDiaryByTitle(): List<Diary> {
//                        return findDiary().filter { diary ->  diary.title!!.contains(currentQuery, ignoreCase= true) } .sortedBy { diary -> diary.title }
//                    }
        var treeData by remember { mutableStateOf(emptyList<Pair<FileNode, Int>>())}
        var total by remember { mutableIntStateOf(0) }

        fun toggleWholeTree(isExpand: Boolean) {
            treeData = treeData.map { data ->
                if (data.second == 1) {
                    data.copy(first = data.first.copy(isFolderOpen = isExpand))
                } else if (data.first.isFile) {
                    data.copy(first = data.first.copy(isShow = isExpand, isRootShow = isExpand))
                } else {
                    data.copy(first = data.first.copy(isFolderOpen = isExpand, isShow = isExpand, isRootShow = isExpand))
                }
            }
        }

        fun isRootNodeVisible (data: Pair<FileNode, Int>): Boolean {
            if (data.second == 1) return true

            var isShow = false
            val parentNode = data.first.fullPath.split("/")
            var currentPath = ""
            for (i in 0 until parentNode.size.minus(1)) {
                currentPath += if (currentPath.isEmpty()) parentNode[i] else "/${parentNode[i]}"
                isShow = treeData.find { it -> it.first.fullPath == currentPath }!!.first.isFolderOpen
                if (!isShow) break
            }
            return isShow
        }

        fun toggleChildren(fileNode: FileNode) {
            treeData = treeData.map { data ->
                if (data.first.fullPath.startsWith(fileNode.fullPath) && data.first.fullPath != fileNode.fullPath) {
                    val isFirstChildNode = fileNode.children.any {child -> child.fullPath == data.first.fullPath}
                    if (isFirstChildNode) {
                        data.copy(first = data.first.copy(isShow = fileNode.isFolderOpen, isRootShow = isRootNodeVisible(data)))
                    } else {
                        data.copy(first = data.first.copy(isRootShow = isRootNodeVisible(data)))
                    }
                } else {
//                                                data.copy(first = data.first.copy(isRootShow = isRootNodeVisible(data)))
                    data
                }
            }
        }

        fun fetchDiary() {
            val diaryItems = findDiary()
            val fileNode = buildFileTree(diaryItems) { diary ->
                diary.title!!.split("/").toMutableList()
            }
            val originTreeData = flattenTree(fileNode)
            treeData = originTreeData.map { pair ->
                if (pair.second == 1) pair.first.isShow = true
                pair
            }
//                        total = diaryItems.filter { diary ->  diary.title!!.endsWith(".md") }.size
            total = diaryItems.size
        }
        fetchDiary()

        AppTheme {
            Scaffold(
                // 하단 패딩은 수동 관리
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
//                topBar = {
//                    EasyDiaryActionBar(
//                        title = "QuickSettings",
//                    ) {
//                        finishActivityWithTransition()
//                    }
//                },
                containerColor = Color(config.screenBackgroundColor),
                content = { innerPadding ->
                    OptionDialog (
                        showDialog = showOptionDialog,
                        optionEnabled = visibleSubTitle,
                        onOptionChangeVisibleSubTitle = { visibleSubTitle = it },
                        onDismiss = { showOptionDialog = false }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)) {
                            TreeToolbar(
                                title = "[Total: $total] category or title",
                                modifier = settingCardModifier.padding(
                                    0.dp,
                                    0.dp,
                                    0.dp,
                                    0.dp
                                ),
                                enableCardViewPolicy = enableCardViewPolicy,
                            ) { query ->
                                currentQuery = query.trim()
                                fetchDiary()
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color(context.config.screenBackgroundColor)),
                            ) {
                                items(treeData) { (node, level) ->
                                    TreeCard(
                                        title = node.name,
                                        subTitle = node.fullPath,
                                        level = level,
                                        isFile = node.isFile,
                                        currentQuery = currentQuery,
                                        isRootShow = node.isRootShow,
                                        isShow = node.isShow,
                                        isFolderOpen = node.isFolderOpen,
                                        visibleSubTitle = visibleSubTitle,
                                        modifier = Modifier.padding(
                                            0.dp,
                                            0.dp,
                                            0.dp,
                                            0.dp
                                        ),
                                        onClick = {
                                            if (node.isFile) {
                                                // 파일인 경우, 해당 다이어리 읽기 화면으로 이동
                                                val detailIntent = Intent(
                                                    this@SelfDevelopmentRepoActivity,
                                                    DiaryReadingActivity::class.java
                                                )
                                                detailIntent.putExtra(
                                                    DIARY_SEQUENCE,
                                                    node.sequence
                                                )
                                                detailIntent.putExtra(
                                                    SELECTED_SEARCH_QUERY,
                                                    currentQuery
                                                )
//                                                detailIntent.putExtra(
//                                                    SELECTED_SYMBOL_SEQUENCE,
//                                                    DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS
//                                                )
                                                TransitionHelper.startActivityWithTransition(
                                                    this@SelfDevelopmentRepoActivity,
                                                    detailIntent
                                                )
                                            } else {
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
                                            }
                                        }
                                    ) {
                                        if (!node.isFile) {
//                                        makeToast(node.fullPath)
                                            isLoading = true
                                            syncMarkDown(null, node.fullPath) {
                                                isLoading = false
                                                fetchDiary()
                                            }
                                        }
                                    }
                                }
                                item {
                                    Spacer(modifier = Modifier.height(bottomPadding.plus(72.dp)))
                                }
                            }
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
//                                    .background(Color(0x88624747))
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }


                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { finishActivityWithTransition() },
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
                        showOptionDialog = { showOptionDialog = true },
                        writeDiaryCallback = {
                            TransitionHelper.startActivityWithTransition(
                                this@SelfDevelopmentRepoActivity,
                                Intent(
                                    this@SelfDevelopmentRepoActivity,
                                    DiaryWritingActivity::class.java
                                )
                            )
                        },
                        expandTreeCallback = {
                            toggleWholeTree(true)
                        },
                        collapseTreeCallback = {
                            toggleWholeTree(false)
                        }
                    )
                },
                floatingActionButtonPosition = FabPosition.Center,
            )
        }
    }
}


