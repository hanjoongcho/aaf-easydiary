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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.tag.TagPickerUiState

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
                placeholder = { Text(text = stringResource(R.string.tag_picker_search_hint), color = textColor) },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null, tint = primaryColor) },
                trailingIcon = {
                    if (uiState.selectedNames.isNotEmpty()) {
                        Text(
                            text = uiState.selectedNames.size.toString(),
                            color = primaryColor,
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
                        Text(
                            text = stringResource(R.string.tag_picker_empty),
                            color = textColor,
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
        Text(
            text = "#$name",
            color = textColor,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
        Text(
            text = stringResource(R.string.tag_picker_create, name),
            color = textColor,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val CREATE_ROW_KEY = "tag_picker_create_row"
private const val EMPTY_ROW_KEY = "tag_picker_empty_row"

@Preview(showBackground = true)
@Composable
private fun TagPickerScreenPreview() {
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
