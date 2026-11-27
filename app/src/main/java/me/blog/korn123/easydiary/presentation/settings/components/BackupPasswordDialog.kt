package me.blog.korn123.easydiary.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.utils.FontUtils

/**
 * Asks for the optional zip password of `[BETA] Backup full data` before the destination is chosen.
 *
 * An empty field is handed over as null, which keeps the plain archive the app has always produced. A password
 * has to be typed twice the same, because an archive whose password nobody knows can never be opened again.
 */
@Composable
fun BackupPasswordDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (password: String?) -> Unit,
) {
    if (visible) {
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        val primaryColor = Color(context.config.primaryColor)
        val textColor = Color(context.config.textColor)
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var showMismatch by remember { mutableStateOf(false) }

        val confirm =
            {
                when {
                    password.isEmpty() -> onConfirm(null)
                    password == confirmPassword -> onConfirm(password)
                    else -> showMismatch = true
                }
            }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                SimpleText(
                    text = stringResource(R.string.backup_full_password_title),
                    fontWeight = FontWeight.Bold,
                    fontColor = textColor,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SimpleText(
                        text = stringResource(R.string.backup_full_password_description),
                        fontColor = textColor,
                    )
                    PasswordField(
                        value = password,
                        hint = stringResource(R.string.backup_full_password_hint),
                        imeAction = ImeAction.Next,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        onValueChange = {
                            password = it
                            showMismatch = false
                        },
                        onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    )
                    PasswordField(
                        value = confirmPassword,
                        hint = stringResource(R.string.backup_full_password_confirm_hint),
                        imeAction = ImeAction.Done,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        onValueChange = {
                            confirmPassword = it
                            showMismatch = false
                        },
                        onImeAction = { confirm() },
                    )
                    if (showMismatch) {
                        SimpleText(
                            text = stringResource(R.string.backup_full_password_mismatch),
                            fontColor = primaryColor,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { confirm() }) {
                    SimpleText(
                        text = stringResource(R.string.backup_full_password_continue),
                        fontColor = primaryColor,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    SimpleText(
                        text = stringResource(R.string.cancel),
                        fontColor = primaryColor,
                    )
                }
            },
            containerColor = Color(context.config.backgroundColor),
            titleContentColor = textColor,
            textContentColor = textColor,
        )
    }
}

@Composable
private fun PasswordField(
    value: String,
    hint: String,
    imeAction: ImeAction,
    primaryColor: Color,
    textColor: Color,
    onValueChange: (String) -> Unit,
    onImeAction: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val fontSize = with(density) { context.config.settingFontSize.toDp().toSp() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = {
            SimpleText(
                text = hint,
                alpha = 0.6f,
                fontColor = textColor,
            )
        },
        textStyle = TextStyle(fontFamily = FontUtils.getComposeFontFamily(context), fontSize = fontSize, color = textColor),
        visualTransformation = PasswordVisualTransformation(),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = primaryColor,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = textColor.copy(alpha = 0.4f),
            ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onImeAction() }, onNext = { onImeAction() }),
    )
}
