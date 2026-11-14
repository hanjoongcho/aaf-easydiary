package me.blog.korn123.easydiary.presentation.symbol

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.presentation.symbol.components.SymbolFilterPickerScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme

@AndroidEntryPoint
class SymbolFilterPickerActivity : EasyDiaryComposeBaseActivity() {
    private val viewModel: SymbolFilterPickerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                SymbolFilterPickerScreen(
                    uiState = uiState,
                    onRemoveSymbol = { sequence -> viewModel.removeSymbol(sequence) },
                    onAddSymbol = { sequence -> viewModel.addSymbol(sequence) },
                    onDismissAlertDialog = { viewModel.dismissAlertDialog() },
                    onConfirmClick = { handleConfirm() },
                    onCloseClick = { handleClose() },
                    onScrollToBottomHandled = { viewModel.onScrollToBottomHandled() },
                )
            }
        }
    }

    private fun handleConfirm() {
        viewModel.saveSelectedSymbols()
        setResult(RESULT_OK)
        finishActivityWithPauseLock()
    }

    private fun handleClose() {
        setResult(RESULT_CANCELED)
        finishActivityWithPauseLock()
    }
}
