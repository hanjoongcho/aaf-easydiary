package me.blog.korn123.easydiary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import me.blog.korn123.easydiary.domain.model.Tag
import me.blog.korn123.easydiary.domain.model.TagRules
import me.blog.korn123.easydiary.extensions.config

/**
 * One tag of an entry (issue #270).
 *
 * The chip is drawn with the colors of the current theme, filled when it is the active filter and outlined
 * otherwise, so it sits next to the card badges the app already draws.
 */
@Composable
fun TagChip(
    name: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    showMarker: Boolean = true,
    onClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val primaryColor = Color(context.config.primaryColor)
    val shape = RoundedCornerShape(50)
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(if (selected) primaryColor else Color(context.config.backgroundColor))
                .border(1.dp, primaryColor, shape)
                .clickable { onClick() }
                .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = if (showMarker) TagRules.label(name) else name,
            color = if (selected) Color.White else Color(context.config.textColor),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The tag filter of the main screen (issue #270): every tag at least one entry carries, with an 'All' chip
 * that clears the filter. Hidden while no entry is tagged, so the screen keeps its usual height.
 */
@Composable
fun TagFilterBar(
    tags: List<Tag>,
    selectedTagId: Long?,
    modifier: Modifier = Modifier,
    onTagSelected: (Tag?) -> Unit = {},
) {
    if (tags.isEmpty()) return

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        item(key = FILTER_ALL_KEY) {
            TagChip(
                name = stringResource(R.string.tag_filter_all),
                selected = selectedTagId == null,
                showMarker = false,
                onClick = { onTagSelected(null) },
            )
        }
        items(tags, key = { it.tagId }) { tag ->
            TagChip(
                name = tag.name,
                selected = selectedTagId == tag.tagId,
                onClick = { onTagSelected(tag) },
            )
        }
    }
}

private const val FILTER_ALL_KEY = "tag_filter_all"

@Preview(showBackground = true)
@Composable
private fun TagChipPreview() {
    TagChip(name = "travel", selected = true)
}
