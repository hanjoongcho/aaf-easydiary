package me.blog.korn123.easydiary.presentation.tag

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.helper.TagPickerConstants
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.tag.components.TagPickerScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import java.util.ArrayList

/**
 * The tag picker the diary editor opens from its `+ tag` button (issue #270).
 *
 * Launch with [TagPickerConstants.TAG_NAMES] (the stored tags) and [TagPickerConstants.SELECTED_TAG_NAMES]
 * (the tags the editor already holds); the picker answers with
 * [TagPickerConstants.RESULT_SELECTED_TAG_NAMES]. It touches no database, so a tag created here only becomes
 * a row when the entry is saved.
 */
@AndroidEntryPoint
class TagPickerActivity : EasyDiaryComposeBaseActivity() {
    private val viewModel: TagPickerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initialize(
            allNames = intent.getStringArrayListExtra(TagPickerConstants.TAG_NAMES).orEmpty(),
            selectedNames = intent.getStringArrayListExtra(TagPickerConstants.SELECTED_TAG_NAMES).orEmpty(),
        )

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleClose()
                }
            },
        )

        setContent {
            AppTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                TagPickerScreen(
                    uiState = uiState,
                    onQueryChanged = { query -> viewModel.updateQuery(query) },
                    onToggleTag = { name -> viewModel.toggle(name) },
                    onCreateTag = { viewModel.addQueryAsTag() },
                    onConfirmClick = { handleConfirm() },
                    onCloseClick = { handleClose() },
                )
            }
        }
    }

    private fun handleConfirm() {
        val selectedNames = ArrayList(viewModel.uiState.value.selectedNames)
        setResult(
            RESULT_OK,
            Intent().putStringArrayListExtra(TagPickerConstants.RESULT_SELECTED_TAG_NAMES, selectedNames),
        )
        finishActivityWithPauseLock()
    }

    private fun handleClose() {
        setResult(RESULT_CANCELED)
        finishActivityWithPauseLock()
    }
}
