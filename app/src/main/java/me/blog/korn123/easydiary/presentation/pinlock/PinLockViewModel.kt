package me.blog.korn123.easydiary.presentation.pinlock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.PinLockConstants
import javax.inject.Inject

sealed interface PinLockDialogState {
    data class SettingComplete(val fullPassword: String) : PinLockDialogState
    data object UnlockFail : PinLockDialogState
}

data class PinLockUiState(
    val pin: String = "",
    val activityMode: String = PinLockConstants.ACTIVITY_UNLOCK,
    val isFingerprintEnabled: Boolean = false,
    val dialogState: PinLockDialogState? = null,
)

@HiltViewModel
class PinLockViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val _uiState =
            MutableStateFlow(
                PinLockUiState(
                    isFingerprintEnabled = application.config.fingerprintLockEnable,
                ),
            )
        val uiState: StateFlow<PinLockUiState> = _uiState.asStateFlow()

        fun initActivityMode(mode: String?) {
            val launchingMode = mode ?: PinLockConstants.ACTIVITY_UNLOCK
            _uiState.update {
                it.copy(
                    activityMode = launchingMode,
                    isFingerprintEnabled = getApplication<Application>().config.fingerprintLockEnable,
                )
            }
        }

        fun handleKeyInput(
            key: String,
            onUnlockSuccess: () -> Unit,
        ) {
            val currentState = _uiState.value
            if (currentState.dialogState != null) return

            if (key == "delete") {
                if (currentState.pin.isNotEmpty()) {
                    _uiState.update { it.copy(pin = it.pin.dropLast(1)) }
                }
            } else if (key.isNotEmpty() && currentState.pin.length < 4) {
                val newPin = currentState.pin + key
                _uiState.update { it.copy(pin = newPin) }

                if (newPin.length == 4) {
                    processCompletedPassword(newPin, onUnlockSuccess)
                }
            }
        }

        private fun processCompletedPassword(
            newPin: String,
            onUnlockSuccess: () -> Unit,
        ) {
            val mode = _uiState.value.activityMode
            if (mode == PinLockConstants.ACTIVITY_SETTING) {
                _uiState.update { it.copy(dialogState = PinLockDialogState.SettingComplete(newPin)) }
            } else {
                val savedPassword = getApplication<Application>().config.aafPinLockSavedPassword
                if (savedPassword == newPin) {
                    onUnlockSuccess()
                } else {
                    _uiState.update { it.copy(dialogState = PinLockDialogState.UnlockFail) }
                }
            }
        }

        fun dismissDialog() {
            _uiState.update { it.copy(dialogState = null, pin = "") }
        }
    }
