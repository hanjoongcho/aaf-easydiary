package me.blog.korn123.easydiary.presentation.main.components

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DashboardActivity
import me.blog.korn123.easydiary.activities.GalleryActivity
import me.blog.korn123.easydiary.activities.PostcardViewerActivity
import me.blog.korn123.easydiary.activities.SettingsActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.components.EasyDiaryAlertDialog
import me.blog.korn123.easydiary.presentation.components.EasyDiaryDialogButton
import me.blog.korn123.easydiary.presentation.components.SimpleText

data class MenuOptionItem(
    val iconRes: Int,
    val titleRes: Int,
    val onClick: () -> Unit,
)

@Composable
fun MainOptionDialog(
    showDialog: Boolean,
    fontFamily: FontFamily?,
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        val context = LocalContext.current
        val activity = LocalActivity.current
        val menuOptionItems =
            remember(activity) {
                listOf(
                    MenuOptionItem(
                        iconRes = R.drawable.ic_grid_three_up,
                        titleRes = R.string.postcard_grid_option_title,
                        onClick = {},
                    ),
                    MenuOptionItem(
                        iconRes = R.drawable.ic_picture_w,
                        titleRes = R.string.post_card,
                        onClick = {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, PostcardViewerActivity::class.java),
                                )
                            }
                        },
                    ),
                    MenuOptionItem(
                        iconRes = R.drawable.ic_stack_of_photos,
                        titleRes = R.string.attached_photo_gallery,
                        onClick = {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, GalleryActivity::class.java),
                                )
                            }
                        },
                    ),
                    MenuOptionItem(
                        iconRes = R.drawable.ic_combo,
                        titleRes = R.string.menu_chart,
                        onClick = {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, DashboardActivity::class.java),
                                )
                            }
                        },
                    ),
                    MenuOptionItem(
                        iconRes = R.drawable.ic_settings_7,
                        titleRes = R.string.settings,
                        onClick = {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, SettingsActivity::class.java),
                                )
                            }
                        },
                    ),
                )
            }

        EasyDiaryAlertDialog(
            onDismissRequest = onDismiss,
            title = null,
            buttons = {
                EasyDiaryDialogButton(
                    text = stringResource(id = R.string.cancel),
                    onClick = onDismiss,
                )
                EasyDiaryDialogButton(
                    text = stringResource(id = R.string.ok),
                    onClick = onDismiss,
                )
            },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                menuOptionItems.forEach { item ->
                    val itemTitle = stringResource(id = item.titleRes)
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.makeToast(itemTitle)
                                    item.onClick.invoke()
                                    onDismiss()
                                }.padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = itemTitle,
                            modifier = Modifier.size(24.dp),
                            tint = Color(context.config.primaryColor),
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        SimpleText(
                            text = itemTitle,
                            fontFamily = fontFamily,
                            fontColor = Color(context.config.textColor),
                        )
                    }
                }
            }
        }
    }
}
