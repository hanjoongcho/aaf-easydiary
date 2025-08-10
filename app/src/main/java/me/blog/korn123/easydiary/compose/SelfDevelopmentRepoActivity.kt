package me.blog.korn123.easydiary.compose

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_FICS
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSDAQ
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_KOSPI
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.SELECTED_SYMBOL_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Diary
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
        val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp

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
                    val diaryItems = findDiary()
                    val fileNode = buildFileTree(diaryItems)
                    var treeData by remember { mutableStateOf(flattenTree(fileNode)) }
                    var total by remember { mutableIntStateOf(diaryItems.filter { diary ->  diary.title!!.endsWith(".md") }.size) }

                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                            val diaryItems = findDiary()
                            val fileNode = buildFileTree(diaryItems)
                            total = diaryItems.filter { diary ->  diary.title!!.endsWith(".md") }.size
                            treeData = flattenTree(fileNode)
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
                                    level = level,
                                    isFile = node.isFile,
                                    currentQuery = currentQuery,
                                    modifier = Modifier.padding(
                                        0.dp,
                                        0.dp,
                                        0.dp,
                                        0.dp
                                    ),
                                ) {
                                    makeSnackBar("Clicked on ${node.name} at level $level")
                                    val detailIntent = Intent(this@SelfDevelopmentRepoActivity, DiaryReadingActivity::class.java)
                                    detailIntent.putExtra(DIARY_SEQUENCE, node.sequence)
                                    detailIntent.putExtra(SELECTED_SEARCH_QUERY, currentQuery)
                                    detailIntent.putExtra(SELECTED_SYMBOL_SEQUENCE, DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS)
                                    TransitionHelper.startActivityWithTransition(this@SelfDevelopmentRepoActivity, detailIntent)
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(bottomPadding.plus(72.dp)))
                            }
                        }
                    }
                },
                floatingActionButton = {
                    Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                        FloatingActionButton(
                            onClick = { finishActivityWithTransition() },
                            containerColor = Color(config.primaryColor),
                            contentColor = Color.White,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(8.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cross),
                                contentDescription = "Finish Activity"
                            )
                        }
                    }
                },
                floatingActionButtonPosition = FabPosition.Center,
            )
        }
    }

    /**
     * Flattens the file tree into a list of pairs containing the node and its level in the tree.
     * The root node is excluded from the result.
     */
    fun flattenTree(node: FileNode, level: Int = 0): List<Pair<FileNode, Int>> {
        val list = mutableListOf<Pair<FileNode, Int>>()
        if (node.name != "root") list.add(node to level)
        node.children.sortedBy { it.name }.forEach {
            list.addAll(flattenTree(it, level + 1))
        }
        return list
    }

    /**
     * Builds a file tree structure from a list of paths.
     * Each path is expected to be in the format "dir1/dir2/file.txt".
     */
    fun buildFileTree(items: List<Diary>): FileNode {
        val root = FileNode("root", sequence = 0)
        for (diary in items) {
            var current = root
            val parts = diary.title!!.split("/")
            for ((i, part) in parts.withIndex()) {
                val isFile = i == parts.lastIndex
                val existing = current.children.find { it.name == part }
                if (existing != null) {
                    current = existing
                } else {
                    val newNode = FileNode(name = part, isFile = isFile, sequence = diary.sequence)
                    current.children.add(newNode)
                    current = newNode
                }
            }
        }
        return root
    }

    /**
     * Represents a node in the file tree.
     */
    data class FileNode(
        val name: String,
        val children: MutableList<FileNode> = mutableListOf(),
        val isFile: Boolean = false,
        val sequence: Int
    )
}


