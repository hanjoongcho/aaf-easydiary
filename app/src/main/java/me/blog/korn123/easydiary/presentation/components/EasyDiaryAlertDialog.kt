package me.blog.korn123.easydiary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config

/**
 * The dialog every screen of the app shows, drawn with Compose so that a Compose screen can open a dialog
 * that looks like the XML ones instead of a Material 3 `AlertDialog`.
 *
 * It is the Compose counterpart of `Context.updateAlertDialog`, and takes its look from the same places that
 * function paints: the window is the background color of the theme rounded by 3dp, the title bar above it the
 * primary color with the title in bold white 18dp (the size the XML title is set to, in dp and not in sp, so
 * the text size setting leaves it alone), the optional icon in white at the start of the title, the content on
 * the background color with the 15dp padding of `dialog_message.xml`, and the buttons at the bottom end of the
 * dialog in the text color.
 *
 * ```
 * EasyDiaryAlertDialog(
 *     onDismissRequest = onDismiss,
 *     buttons = {
 *         EasyDiaryDialogButton(text = stringResource(R.string.cancel), onClick = onDismiss)
 *     },
 * ) {
 *     SimpleText(text = message)
 * }
 * ```
 *
 * The content is a slot on purpose: the app's dialogs carry a plain message, a list, or a small form, and the
 * slot lets each caller write what it needs with [SimpleText], which already follows the theme font, the text
 * color and the text size setting. A caller whose content can grow past the screen scrolls it itself (see
 * `WritingStreakCard`), because one scroll on top of another in the same direction would not move.
 *
 * @param onDismissRequest Invoked on a tap outside the dialog and on the back key, the places the XML dialog
 * reports through its cancel listener. A button is expected to call it as well once it is done.
 * @param title The text of the title bar, the app name by default as in `DialogMode.DEFAULT`.
 * @param titleIconResId The drawable at the start of the title bar, shown white, or null for no icon.
 * @param dismissible Whether a tap outside the dialog or the back key closes it, the `cancelable` of
 * `Context.showAlertDialog`.
 * @param buttons The row at the bottom end of the dialog. The buttons are given in the order they read from
 * the left, so a caller with all three writes the neutral one first and the positive one last; null leaves the
 * row out for a dialog that only tells something.
 * @param content The body of the dialog, laid out in a column with the padding of the XML message view.
 */
@Composable
fun EasyDiaryAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    titleIconResId: Int? = null,
    dismissible: Boolean = true,
    buttons: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val backgroundColor = Color(context.config.backgroundColor)
    val primaryColor = Color(context.config.primaryColor)
    val titleFontSize = with(density) { TITLE_FONT_SIZE_DP.dp.toPx() }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
                dismissOnBackPress = dismissible,
                dismissOnClickOutside = dismissible,
            ),
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(DIALOG_CORNER_RADIUS_DP.dp),
            color = backgroundColor,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(primaryColor)
                            .padding(TITLE_BAR_PADDING_DP.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (titleIconResId != null) {
                        Icon(
                            painter = painterResource(id = titleIconResId),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .padding(horizontal = TITLE_ICON_MARGIN_DP.dp)
                                    .size(TITLE_ICON_SIZE_DP.dp),
                            tint = Color.White,
                        )
                    }
                    SimpleText(
                        modifier = Modifier.padding(start = TITLE_TEXT_MARGIN_DP.dp).weight(1F),
                        text = title,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Bold,
                        fontColor = Color.White,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(CONTENT_PADDING_DP.dp),
                    content = content,
                )

                if (buttons != null) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = BUTTON_PANEL_HORIZONTAL_PADDING_DP.dp, vertical = BUTTON_PANEL_VERTICAL_PADDING_DP.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        content = buttons,
                    )
                }
            }
        }
    }
}

/**
 * A button of an [EasyDiaryAlertDialog], written in the text color of the theme as the buttons of the XML
 * dialogs are, with the app font and the ripple of a tappable element.
 *
 * It is meant for the `buttons` row of [EasyDiaryAlertDialog], where [RowScope] places it after the button
 * written before it.
 */
@Composable
fun RowScope.EasyDiaryDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val fontSize = with(density) { BUTTON_FONT_SIZE_SP.sp.toPx() }

    Box(
        modifier =
            modifier
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = BUTTON_HORIZONTAL_PADDING_DP.dp, vertical = BUTTON_VERTICAL_PADDING_DP.dp),
        contentAlignment = Alignment.Center,
    ) {
        SimpleText(
            text = text,
            fontSize = fontSize,
            fontColor = Color(context.config.textColor),
            maxLines = 1,
        )
    }
}

/** The window of the XML dialog is a rectangle of the background color rounded by 3dp. */
private const val DIALOG_CORNER_RADIUS_DP = 3F

/** The title bar of `partial_dialog_title.xml` pads its title by 15dp and keeps the icon 5dp apart from it. */
private const val TITLE_BAR_PADDING_DP = 15F
private const val TITLE_ICON_MARGIN_DP = 5F
private const val TITLE_TEXT_MARGIN_DP = 5F

/** The title icons are 24dp drawables, the height the title text takes at 18dp. */
private const val TITLE_ICON_SIZE_DP = 24F

/** The XML title is set to 18dp, a dp size that the text size setting does not scale. */
private const val TITLE_FONT_SIZE_DP = 18F

/** `dialog_message.xml` carries the message with the 15dp padding of the app cards. */
private const val CONTENT_PADDING_DP = 15F

/** The dialog buttons keep the text size of the dialog theme, a size the text size setting does not scale. */
private const val BUTTON_FONT_SIZE_SP = 14F

/** The buttons of the XML dialog sit in a panel that keeps them 48dp tall and inside the window edge. */
private const val BUTTON_PANEL_HORIZONTAL_PADDING_DP = 8F
private const val BUTTON_PANEL_VERTICAL_PADDING_DP = 4F
private const val BUTTON_HORIZONTAL_PADDING_DP = 16F
private const val BUTTON_VERTICAL_PADDING_DP = 12F
