package me.blog.korn123.easydiary.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BaseDiaryEditingActivity.Companion.DIARY_SEQUENCE_INIT
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isColorLight
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.isVanillaIceCreamPlus
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.updateSystemStatusBarColor
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.SELECTED_SYMBOL_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.ui.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.ui.components.SimpleCard
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
            NestedScrollConnection()
        }
    }


    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NestedScrollConnection() {
        mSettingsViewModel = initSettingsViewModel()
        val bottomPadding = if (isVanillaIceCreamPlus()) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp

        AppTheme {
            Scaffold(
                contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                topBar = {
                    EasyDiaryActionBar(
                        title = "QuickSettings",
                    ) {
                        finishActivityWithTransition()
                    }
                },
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
                            0,
                            0,
                            DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC).sortedBy { diary -> diary.title }
                    }
                    fun findDiaryByTitle(): List<Diary> {
                        return findDiary().filter { diary ->  diary.title!!.contains(currentQuery, ignoreCase= true) } .sortedBy { diary -> diary.title }
                    }
                    val fileNode = buildFileTree(findDiary())
                    var treeData by remember { mutableStateOf(flattenTree(fileNode)) }

                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        TreeToolbar(
                            title = "Toolbar Area",
                            modifier = settingCardModifier.padding(
                                0.dp,
                                0.dp,
                                0.dp,
                                0.dp
                            ),
                            level = 0,
                            enableCardViewPolicy = enableCardViewPolicy,
                        ) { query ->
                            currentQuery = query.trim()
                            val fileNode = buildFileTree(findDiary())
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
                                    detailIntent.putExtra(SELECTED_SYMBOL_SEQUENCE, DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC)
                                    TransitionHelper.startActivityWithTransition(this@SelfDevelopmentRepoActivity, detailIntent)
                                }
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


