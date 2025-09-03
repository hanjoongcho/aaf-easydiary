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
        mSettingsViewModel = initSettingsViewModel()
        LocalActivity.current?.updateSystemStatusBarColor()
        val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp
        var showOptionDialog by remember { mutableStateOf(false) }
        var visibleSubTitle by remember { mutableStateOf(false) }

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
                    val context = LocalContext.current
                    val settingCardModifier = Modifier.fillMaxWidth()
                    val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
                        context.config.enableCardViewPolicy
                    )
                    var currentQuery by remember { mutableStateOf("") }
                    fun findDiary(): List<Diary> {
//                        return EasyDiaryDbHelper.findDiary( currentQuery).sortedBy { diary -> diary.title }
                        return EasyDiaryDbHelper.findDiary(currentQuery)
                    }
//                    fun findDiaryByTitle(): List<Diary> {
//                        return findDiary().filter { diary ->  diary.title!!.contains(currentQuery, ignoreCase= true) } .sortedBy { diary -> diary.title }
//                    }
                    var treeData by remember { mutableStateOf(emptyList<Pair<FileNode, Int>>()) }
                    var total by remember { mutableIntStateOf(0) }

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

                    OptionDialog (
                        showDialog = showOptionDialog,
                        optionEnabled = visibleSubTitle,
                        onOptionChangeVisibleSubTitle = { visibleSubTitle = it },
                        onDismiss = { showOptionDialog = false }
                    )

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
                            items(items = treeData.filter { data -> data.first.isRootShow && data.first.isShow }, key = { "${it.first.sequence}-${it.first.fullPath}"}) { (node, level) ->
                                TreeCard(
                                    sequence = node.sequence,
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
                                        fun isRootNodeVisible (data: Pair<FileNode, Int>): Boolean {
                                            if (data.second == 1) return true

                                            var isShow = false
                                            val parentNode = data.first.fullPath.split("/")
                                            var currentPath = ""
                                            for (i in 0 until parentNode.size.minus(1)) {
                                                currentPath += if (currentPath.isEmpty()) parentNode[i] else "/${parentNode[i]}"
                                                isShow = treeData.find { it -> it.first.fullPath == currentPath }?.first?.isFolderOpen ?: false
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

                                        if (node.isFile) {
                                            // 파일인 경우, 해당 다이어리 읽기 화면으로 이동
                                            val detailIntent = Intent(
                                                this@TreeTimelineActivity,
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
//                                        detailIntent.putExtra(
//                                            SELECTED_SYMBOL_SEQUENCE,
//                                            DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS
//                                        )
                                            TransitionHelper.startActivityWithTransition(
                                                this@TreeTimelineActivity,
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
                                        makeToast(node.fullPath)
                                    }
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(bottomPadding.plus(72.dp)))
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
                                this@TreeTimelineActivity,
                                Intent(
                                    this@TreeTimelineActivity,
                                    DiaryWritingActivity::class.java
                                )
                            )
                        }
                    )
                },
                floatingActionButtonPosition = FabPosition.Center,
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
                        ) { query ->
                        }
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(context.config.screenBackgroundColor)),
                        ) {
                            items(items = treeData.filter { data -> data.first.isRootShow && data.first.isShow }, key = { "${it.first.sequence}-${it.first.fullPath}"}) { (node, level) ->
                                TreeCard(
                                    sequence = node.sequence,
                                    title = node.name,
                                    subTitle = node.fullPath,
                                    level = level,
                                    isFile = node.isFile,
                                    currentQuery = "dummy",
                                    isRootShow = node.isRootShow,
                                    isShow = node.isShow,
                                    isFolderOpen = node.isFolderOpen,
                                    visibleSubTitle = false,
                                    modifier = Modifier.padding(
                                        0.dp,
                                        0.dp,
                                        0.dp,
                                        0.dp
                                    ),
                                    onClick = {}
                                ) {}
                            }
                            item {
                                Spacer(modifier = Modifier.height(bottomPadding.plus(72.dp)))
                            }
                        }
                    }
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


