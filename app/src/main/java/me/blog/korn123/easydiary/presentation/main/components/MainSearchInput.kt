package me.blog.korn123.easydiary.presentation.main.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainSearchInput(
    title: String,
    currentQuery: String,
    symbolSequence: Int,
    fontSize: Float,
    fontColor: Color,
    fontWeight: FontWeight,
    fontFamily: FontFamily?,
    onFocusChanged: (Boolean) -> Unit,
    callbackQuery: (String) -> Unit,
    clearFilter: () -> Unit,
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var text by remember(currentQuery) { mutableStateOf(currentQuery) }
        val density = LocalDensity.current
        val textUnit =
            with(density) {
                val temp = fontSize.toDp()
                temp.toSp()
            }

        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val isKeyboardVisible = WindowInsets.isImeVisible

        LaunchedEffect(isKeyboardVisible) {
            if (!isKeyboardVisible) {
                focusManager.clearFocus()
            }
        }

        TextField(
            value = text,
            onValueChange = {
                text = it
                callbackQuery.invoke(text)
            },
            label = {
                Text(
                    text = title,
                    style =
                        TextStyle(
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            color = fontColor,
                            fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                        ),
                )
            },
            colors =
                TextFieldDefaults.colors(
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            textStyle =
                TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    color = fontColor,
                    fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                ),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    onSearch = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                ),
            trailingIcon = {
                if (text.isNotEmpty() || symbolSequence != SYMBOL_SELECT_ALL) {
                    IconButton(onClick = {
                        text = ""
                        clearFilter.invoke()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear filter",
                            tint = fontColor,
                        )
                    }
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onFocusChanged(focusState.isFocused)
                    },
        )
    }
}
