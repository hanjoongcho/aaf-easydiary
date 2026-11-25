package me.blog.korn123.easydiary.presentation.main.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.presentation.components.CustomElevatedButton

/**
 * The bottom sheet in its collapsed state: the sheet is put away and only this floating button stays at the
 * bottom end of the screen, above the navigation bar. Tapping it brings the sheet back.
 *
 * The button sits over the diary list rather than beside it, so it reports its own height through
 * [onHeightChanged] and the list keeps its last row clear of it the same way it keeps clear of the sheet.
 */
@Composable
fun CollapsedBottomSheetButton(
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
    onHeightChanged: (Dp) -> Unit = {},
) {
    val density = LocalDensity.current
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(0.dp, 10.dp, 16.dp, 10.dp)
                .onGloballyPositioned { coordinates ->
                    onHeightChanged(with(density) { coordinates.size.height.toDp() })
                },
        contentAlignment = Alignment.BottomEnd,
    ) {
        CustomElevatedButton(
            text = stringResource(R.string.button_expand_bottom_sheet),
            iconResourceId = R.drawable.ic_keyboard_arrow_up,
            iconSize = 18.dp,
        ) {
            onExpandClick()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollapsedBottomSheetButtonPreview() {
    CollapsedBottomSheetButton(onExpandClick = {})
}
