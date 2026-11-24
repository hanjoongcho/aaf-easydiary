package me.blog.korn123.easydiary.presentation.tag.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.tag.TagPickerUiState
import me.blog.korn123.easydiary.presentation.theme.AppTheme

/**
 * Picks the tags of an entry (issue #270).
 *
 * The search field filters the stored tags as the writer types; a tag is added or removed with one tap, and
 * a name no tag has yet is offered as its own row, which creates the tag and selects it at once.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagPickerScreen(
    uiState: TagPickerUiState,
    onQueryChanged: (String) -> Unit,
    onToggleTag: (String) -> Unit,
    onCreateTag: () -> Unit,
    onConfirmClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val context = LocalContext.current
    val textColor = Color(context.config.textColor)
    val primaryColor = Color(context.config.primaryColor)

    Scaffold(
        topBar = {
            EasyDiaryActionBar(
                title = stringResource(R.string.tag_picker_title),
                useCloseIcon = true,
                actions = {
                    IconButton(onClick = onConfirmClick) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.tag_picker_done),
                            tint = Color.White,
                        )
                    }
                },
                close = onCloseClick,
            )
        },
        containerColor = Color(context.config.backgroundColor),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChanged,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                placeholder = {
                    SimpleText(
                        text = stringResource(R.string.tag_picker_search_hint),
                        alpha = 0.6f,
                        fontColor = textColor,
                    )
                },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = primaryColor) },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = textColor.copy(alpha = 0.4f),
                    ),
                trailingIcon = {
                    if (uiState.selectedNames.isNotEmpty()) {
                        SimpleText(
                            text = uiState.selectedNames.size.toString(),
                            fontColor = primaryColor,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                },
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                uiState.createCandidate?.let { candidate ->
                    item(key = CREATE_ROW_KEY) {
                        CreateTagRow(
                            name = candidate,
                            primaryColor = primaryColor,
                            textColor = primaryColor,
                            onClick = onCreateTag,
                        )
                    }
                }

                items(uiState.filteredNames, key = { it }) { name ->
                    TagRow(
                        name = name,
                        selected = uiState.isSelected(name),
                        primaryColor = primaryColor,
                        textColor = textColor,
                        onClick = { onToggleTag(name) },
                    )
                }

                if (uiState.filteredNames.isEmpty() && uiState.createCandidate == null) {
                    item(key = EMPTY_ROW_KEY) {
                        SimpleText(
                            text = stringResource(R.string.tag_picker_empty),
                            alpha = 0.7f,
                            fontColor = textColor,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagRow(
    name: String,
    selected: Boolean,
    primaryColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .clip(CircleShape)
                    .background(if (selected) primaryColor else Color.Transparent)
                    .padding(3.dp),
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(1.dp),
                )
            }
        }
        SimpleText(
            text = "#$name",
            fontColor = textColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun CreateTagRow(
    name: String,
    primaryColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = Icons.Outlined.Add, contentDescription = null, tint = primaryColor)
        SimpleText(
            text = stringResource(R.string.tag_picker_create, name),
            fontColor = textColor,
            maxLines = 1,
        )
    }
}

private const val CREATE_ROW_KEY = "tag_picker_create_row"
private const val EMPTY_ROW_KEY = "tag_picker_empty_row"

@Preview(showBackground = true)
@Composable
private fun TagPickerScreenPreview() {
    AppTheme {
        TagPickerScreen(
            uiState =
                TagPickerUiState(
                    query = "tr",
                    allNames = listOf("travel", "trip", "work"),
                    selectedNames = listOf("trip"),
                ),
            onQueryChanged = {},
            onToggleTag = {},
            onCreateTag = {},
            onConfirmClick = {},
            onCloseClick = {},
        )
    }
}
