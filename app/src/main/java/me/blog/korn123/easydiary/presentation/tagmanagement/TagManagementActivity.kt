package me.blog.korn123.easydiary.presentation.tagmanagement

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.tagmanagement.components.TagManagementScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme

/**
 * The tag manager the settings open (issue #270).
 *
 * The screen reads and writes the database on its own through [TagManagementViewModel], so a change is stored as
 * soon as the writer confirms it and every screen that shows tags (the main filter, the reading screen, the
 * editor's picker) follows from the database.
 */
@AndroidEntryPoint
class TagManagementActivity : EasyDiaryComposeBaseActivity() {
    private val viewModel: TagManagementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishActivityWithPauseLock()
                }
            },
        )

        setContent {
            AppTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                TagManagementScreen(
                    uiState = uiState,
                    onNewTagNameChanged = { name -> viewModel.updateNewTagName(name) },
                    onAddTag = { viewModel.addTag() },
                    onStartEditing = { tag -> viewModel.startEditing(tag) },
                    onEditingNameChanged = { name -> viewModel.updateEditingName(name) },
                    onConfirmRename = { viewModel.confirmRename() },
                    onCancelEditing = { viewModel.cancelEditing() },
                    onDeleteClick = { tagWithCount -> viewModel.requestDelete(tagWithCount) },
                    onDismissAlert = { viewModel.dismissAlert() },
                    onConfirmDelete = { viewModel.confirmDelete() },
                    onConfirmMerge = { viewModel.confirmMerge() },
                    onBackClick = { finishActivityWithPauseLock() },
                )
            }
        }
    }
}
