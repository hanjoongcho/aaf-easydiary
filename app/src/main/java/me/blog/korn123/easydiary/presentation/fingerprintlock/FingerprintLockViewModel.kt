package me.blog.korn123.easydiary.presentation.fingerprintlock

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.blog.korn123.easydiary.helper.FingerprintLockConstants
import javax.inject.Inject

data class FingerprintLockUiState(
    val activityMode: String = FingerprintLockConstants.ACTIVITY_SETTING,
    val guideMessage: String = "",
    val isChangePinLockVisible: Boolean = false,
    val dialogState: FingerprintLockDialogState? = null,
)

sealed interface FingerprintLockDialogState {
    data object SettingComplete : FingerprintLockDialogState
}

@HiltViewModel
class FingerprintLockViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(FingerprintLockUiState())
    val uiState: StateFlow<FingerprintLockUiState> = _uiState.asStateFlow()

    fun initActivityMode(mode: String?) {
        val activityMode = mode ?: FingerprintLockConstants.ACTIVITY_SETTING
        _uiState.update {
            it.copy(
                activityMode = activityMode,
                isChangePinLockVisible = activityMode != FingerprintLockConstants.ACTIVITY_SETTING,
            )
        }
    }

    fun updateGuideMessage(message: String) {
        _uiState.update { it.copy(guideMessage = message) }
    }

    fun showSettingCompleteDialog() {
        _uiState.update { it.copy(dialogState = FingerprintLockDialogState.SettingComplete) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = null) }
    }
}
