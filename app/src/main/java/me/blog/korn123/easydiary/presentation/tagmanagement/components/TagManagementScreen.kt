package me.blog.korn123.easydiary.presentation.tagmanagement.components

import android.content.DialogInterface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.Tag
import me.blog.korn123.easydiary.domain.model.TagWithCount
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.components.TagChip
import me.blog.korn123.easydiary.presentation.settings.components.SettingsBottomSpacer
import me.blog.korn123.easydiary.presentation.tagmanagement.TagManagementAlert
import me.blog.korn123.easydiary.presentation.tagmanagement.TagManagementUiState
import me.blog.korn123.easydiary.presentation.theme.AppTheme

/**
 * The tag manager the settings open (issue #270).
 *
 * The screen lists every stored tag with the number of entries that carry it, so the writer can see which tags
 * are in use and which are left over. A tag is added from the field at the top, its name is renamed in place, and
 * deleting it asks first: the entries themselves are never touched, only the tag they carry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagementScreen(
    uiState: TagManagementUiState,
    onNewTagNameChanged: (String) -> Unit,
    onAddTag: () -> Unit,
    onStartEditing: (Tag) -> Unit,
    onEditingNameChanged: (String) -> Unit,
    onConfirmRename: () -> Unit,
    onCancelEditing: () -> Unit,
    onDeleteClick: (TagWithCount) -> Unit,
    onDismissAlert: () -> Unit,
    onConfirmDelete: () -> Unit,
    onConfirmMerge: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val primaryColor = Color(context.config.primaryColor)
    val textColor = Color(context.config.textColor)

    Scaffold(
        topBar = {
            EasyDiaryActionBar(
                title = stringResource(R.string.tag_management_title),
                close = onBackClick,
            )
        },
        containerColor = Color(context.config.screenBackgroundColor),
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            AddTagRow(
                name = uiState.newTagName,
                primaryColor = primaryColor,
                onNameChanged = onNewTagNameChanged,
                onAdd = onAddTag,
            )

            when {
                uiState.tags.isEmpty() -> {
                    if (!uiState.isLoading) {
                        SimpleText(
                            text = stringResource(R.string.tag_management_empty),
                            alpha = 0.7f,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                        )
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.tags, key = { it.tag.tagId }) { tagWithCount ->
                            val editing = uiState.editing
                            if (editing != null && editing.tagId == tagWithCount.tag.tagId) {
                                EditTagRow(
                                    name = editing.name,
                                    primaryColor = primaryColor,
                                    textColor = textColor,
                                    onNameChanged = onEditingNameChanged,
                                    onConfirm = onConfirmRename,
                                    onCancel = onCancelEditing,
                                )
                            } else {
                                TagRow(
                                    tagWithCount = tagWithCount,
                                    textColor = textColor,
                                    onRenameClick = { onStartEditing(tagWithCount.tag) },
                                    onDeleteClick = { onDeleteClick(tagWithCount) },
                                )
                            }
                        }
                        item { SettingsBottomSpacer() }
                    }
                }
            }
        }
    }

    /*
     * The question the screen waits on is asked with the app common dialog, so the tag manager looks like the
     * rest of the app. uiState.alert stays set while the dialog is up: the buttons clear it themselves, and the
     * dialog's cancel clears it for the exits that pick no button (a tap outside it or the back key), so a closed
     * dialog never leaves the screen with a question that would not be asked again.
     */
    val alert = uiState.alert
    val emptyNameMessage = stringResource(R.string.tag_management_name_required)
    val duplicateNameMessage =
        (alert as? TagManagementAlert.DuplicateName)?.let { stringResource(R.string.tag_management_duplicate, it.name) }
    val deleteMessage =
        (alert as? TagManagementAlert.DeleteConfirm)?.let {
            when (it.diaryCount) {
                0 -> stringResource(R.string.tag_management_delete_unused_message, it.tag.name)
                else -> stringResource(R.string.tag_management_delete_message, it.tag.name, it.diaryCount)
            }
        }
    val mergeMessage =
        (alert as? TagManagementAlert.MergeConfirm)?.let {
            stringResource(R.string.tag_management_merge_message, it.target.name, it.source.name)
        }
    val deleteTitle = stringResource(R.string.tag_management_delete_title)
    val mergeTitle = stringResource(R.string.tag_management_merge_title)
    val dialogTitle = stringResource(R.string.tag_management_title)

    LaunchedEffect(alert) {
        val dismissAlertListener = DialogInterface.OnCancelListener { onDismissAlert() }
        when (alert) {
            TagManagementAlert.EmptyName -> {
                context.showAlertDialog(
                    message = emptyNameMessage,
                    positiveListener = { _, _ -> onDismissAlert() },
                    negativeListener = null,
                    dialogMode = DialogMode.INFO,
                    paramTitle = dialogTitle,
                    onCancelListener = dismissAlertListener,
                )
            }

            is TagManagementAlert.DuplicateName -> {
                context.showAlertDialog(
                    message = duplicateNameMessage.orEmpty(),
                    positiveListener = { _, _ -> onDismissAlert() },
                    negativeListener = null,
                    dialogMode = DialogMode.INFO,
                    paramTitle = dialogTitle,
                    onCancelListener = dismissAlertListener,
                )
            }

            is TagManagementAlert.DeleteConfirm -> {
                context.showAlertDialog(
                    message = deleteMessage.orEmpty(),
                    positiveListener = { _, _ -> onConfirmDelete() },
                    negativeListener = { _, _ -> onDismissAlert() },
                    dialogMode = DialogMode.WARNING,
                    paramTitle = deleteTitle,
                    onCancelListener = dismissAlertListener,
                )
            }

            is TagManagementAlert.MergeConfirm -> {
                context.showAlertDialog(
                    message = mergeMessage.orEmpty(),
                    positiveListener = { _, _ -> onConfirmMerge() },
                    negativeListener = { _, _ -> onDismissAlert() },
                    dialogMode = DialogMode.WARNING,
                    paramTitle = mergeTitle,
                    onCancelListener = dismissAlertListener,
                )
            }

            null -> {}
        }
    }
}

/** The field that adds a tag of its own, the way the picker lets a name be typed before an entry carries it. */
@Composable
private fun AddTagRow(
    name: String,
    primaryColor: Color,
    onNameChanged: (String) -> Unit,
    onAdd: () -> Unit,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChanged,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        placeholder = { SimpleText(text = stringResource(R.string.tag_management_add_hint), alpha = 0.6f) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onAdd() }),
        trailingIcon = {
            IconButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.tag_management_add),
                    tint = primaryColor,
                )
            }
        },
    )
}

/** One stored tag: its chip, how many entries carry it, and the two actions the manager offers. */
@Composable
private fun TagRow(
    tagWithCount: TagWithCount,
    textColor: Color,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            TagChip(name = tagWithCount.tag.name)
        }
        SimpleText(
            text = stringResource(R.string.tag_management_diary_count, tagWithCount.diaryCount),
            alpha = 0.7f,
            maxLines = 1,
        )
        IconButton(onClick = onRenameClick) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.tag_management_rename),
                tint = textColor,
            )
        }
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = textColor,
            )
        }
    }
}

/** The row of the tag whose name is being typed, with the two buttons that end the edit. */
@Composable
private fun EditTagRow(
    name: String,
    primaryColor: Color,
    textColor: Color,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            modifier =
                Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
            singleLine = true,
            placeholder = { SimpleText(text = stringResource(R.string.tag_management_rename_hint), alpha = 0.6f) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onConfirm() }),
            trailingIcon = {
                Row {
                    IconButton(onClick = onConfirm) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.tag_management_rename),
                            tint = primaryColor,
                        )
                    }
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = textColor,
                        )
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TagManagementScreenPreview() {
    AppTheme {
        TagManagementScreen(
            uiState =
                TagManagementUiState(
                    tags =
                        listOf(
                            TagWithCount(tag = Tag(tagId = 1L, name = "travel"), diaryCount = 12),
                            TagWithCount(tag = Tag(tagId = 2L, name = "work"), diaryCount = 3),
                            TagWithCount(tag = Tag(tagId = 3L, name = "diet"), diaryCount = 0),
                        ),
                    isLoading = false,
                    newTagName = "",
                ),
            onNewTagNameChanged = {},
            onAddTag = {},
            onStartEditing = {},
            onEditingNameChanged = {},
            onConfirmRename = {},
            onCancelEditing = {},
            onDeleteClick = {},
            onDismissAlert = {},
            onConfirmDelete = {},
            onConfirmMerge = {},
            onBackClick = {},
        )
    }
}
