package me.blog.korn123.easydiary.presentation.pinlock

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BaseSimpleActivity
import me.blog.korn123.easydiary.activities.FingerprintLockActivity
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.hideSystemBars
import me.blog.korn123.easydiary.extensions.holdCurrentOrientation
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.FingerprintLockConstants
import me.blog.korn123.easydiary.helper.PinLockConstants
import me.blog.korn123.easydiary.presentation.pinlock.components.PinLockScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme

@AndroidEntryPoint
class PinLockActivity : BaseSimpleActivity() {
    private val pinLockViewModel: PinLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityMode = intent.getStringExtra(PinLockConstants.LAUNCHING_MODE)
        pinLockViewModel.initActivityMode(activityMode)

//        if (isLandScape()) hideSystemBars()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    ActivityCompat.finishAffinity(this@PinLockActivity)
                }
            },
        )

        setContent {
            AppTheme {
                val uiState by pinLockViewModel.uiState.collectAsStateWithLifecycle()

                PinLockScreen(
                    pin = uiState.pin,
                    activityMode = uiState.activityMode,
                    isFingerprintEnabled = uiState.isFingerprintEnabled,
                    onKeyClick = { key ->
                        pinLockViewModel.handleKeyInput(
                            key = key,
                            onUnlockSuccess = {
                                pauseLock()
                                finish()
                            },
                        )
                    },
                    onFingerprintClick = {
                        openFingerprintLock()
                    },
                )

                val dialogState = uiState.dialogState
                LaunchedEffect(dialogState) {
                    dialogState?.let { state ->
                        when (state) {
                            is PinLockDialogState.SettingComplete -> {
                                holdCurrentOrientation()
                                showAlertDialog(
                                    getString(R.string.pin_setting_complete, state.fullPassword),
                                    { _, _ ->
                                        config.aafPinLockEnable = true
                                        config.aafPinLockSavedPassword = state.fullPassword
                                        pauseLock()
                                        finish()
                                    },
                                    { _, _ ->
                                        pinLockViewModel.dismissDialog()
                                        finish()
                                    },
                                    DialogMode.INFO,
                                    false,
                                )
                            }

                            is PinLockDialogState.UnlockFail -> {
                                holdCurrentOrientation()
                                showAlertDialog(
                                    message = getString(R.string.pin_verification_fail),
                                    positiveListener = { _, _ ->
                                        pinLockViewModel.dismissDialog()
                                        ActivityCompat.finishAffinity(this@PinLockActivity)
                                    },
                                    negativeListener = null,
                                    dialogMode = DialogMode.DEFAULT,
                                    cancelable = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openFingerprintLock() {
        startActivity(
            Intent(this@PinLockActivity, FingerprintLockActivity::class.java).apply {
                putExtra(FingerprintLockConstants.LAUNCHING_MODE, FingerprintLockConstants.ACTIVITY_UNLOCK)
            },
        )
        finish()
    }
}
