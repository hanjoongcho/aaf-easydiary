package me.blog.korn123.easydiary.presentation.fingerprintlock

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BaseSimpleActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.holdCurrentOrientation
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.FingerprintLockConstants
import me.blog.korn123.easydiary.helper.PinLockConstants
import me.blog.korn123.easydiary.presentation.fingerprintlock.components.FingerprintLockScreen
import me.blog.korn123.easydiary.presentation.pinlock.PinLockActivity
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

@AndroidEntryPoint
class FingerprintLockActivity : BaseSimpleActivity() {
    private val viewModel: FingerprintLockViewModel by viewModels()

    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyGenerator

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var settingComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activityMode = intent.getStringExtra(FingerprintLockConstants.LAUNCHING_MODE)
        viewModel.initActivityMode(activityMode)

        setupBiometricComponents()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    ActivityCompat.finishAffinity(this@FingerprintLockActivity)
                }
            },
        )

        setContent {
            AppTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                FingerprintLockScreen(
                    guideMessage = uiState.guideMessage,
                    isChangePinLockVisible = uiState.isChangePinLockVisible,
                    onChangePinLock = { openPinLock() },
                )

                val dialogState = uiState.dialogState
                LaunchedEffect(dialogState) {
                    dialogState?.let { state ->
                        when (state) {
                            is FingerprintLockDialogState.SettingComplete -> {
                                holdCurrentOrientation()
                                showAlertDialog(
                                    getString(R.string.fingerprint_setting_complete),
                                    { _, _ ->
                                        config.fingerprintLockEnable = true
                                        pauseLock()
                                        finish()
                                    },
                                    false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupBiometricComponents() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        config.fingerprintAuthenticationFailCount = 0
                        val cipher = result.cryptoObject?.cipher
                        val activityMode = viewModel.uiState.value.activityMode

                        when (activityMode) {
                            FingerprintLockConstants.ACTIVITY_SETTING -> {
                                tryEncrypt(cipher)
                                settingComplete = true
                                viewModel.showSettingCompleteDialog()
                            }

                            FingerprintLockConstants.ACTIVITY_UNLOCK -> {
                                if (tryDecrypt(cipher)) {
                                    pauseLock()
                                    finish()
                                }
                            }
                        }
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence,
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            viewModel.updateGuideMessage(errString.toString())
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        viewModel.updateGuideMessage(getString(R.string.fingerprint_authentication_fail_try_again))
                    }
                },
            )

        promptInfo =
            BiometricPrompt.PromptInfo
                .Builder()
                .setTitle(getString(R.string.app_name))
                .setSubtitle(getString(R.string.place_finger_description))
                .setNegativeButtonText(getString(R.string.cancel))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()
    }

    override fun onResume() {
        super.onResume()
        if (!settingComplete) {
            viewModel.updateGuideMessage(getString(R.string.place_finger_description))
            startBiometricAuth()
        }
    }

    private fun openPinLock() {
        startActivity(
            Intent(this@FingerprintLockActivity, PinLockActivity::class.java).apply {
                putExtra(PinLockConstants.LAUNCHING_MODE, PinLockConstants.ACTIVITY_UNLOCK)
            },
        )
        finish()
    }

    private fun startBiometricAuth() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            val defaultCipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
            val keyguardManager = getSystemService(KeyguardManager::class.java)

            if (!keyguardManager.isKeyguardSecure) {
                viewModel.updateGuideMessage("Secure lock screen hasn't set up.")
                return
            }

            val biometricManager = BiometricManager.from(this)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                val activityMode = viewModel.uiState.value.activityMode
                if (activityMode == FingerprintLockConstants.ACTIVITY_SETTING) {
                    createKey(FingerprintLockConstants.KEY_NAME, true)
                }

                if (initCipher(defaultCipher, FingerprintLockConstants.KEY_NAME)) {
                    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(defaultCipher))
                }
            } else {
                viewModel.updateGuideMessage("Fingerprint not registered or hardware not available.")
            }
        } catch (e: Exception) {
            viewModel.updateGuideMessage("Initialization error: ${e.message}")
        }
    }

    private fun createKey(
        keyName: String,
        invalidatedByBiometricEnrollment: Boolean,
    ) {
        try {
            keyStore.load(null)
            val builder =
                KeyGenParameterSpec
                    .Builder(keyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
            }
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun initCipher(
        cipher: Cipher,
        keyName: String,
    ): Boolean {
        try {
            keyStore.load(null)
            val key = keyStore.getKey(keyName, null) as SecretKey
            val activityMode = viewModel.uiState.value.activityMode
            if (activityMode == FingerprintLockConstants.ACTIVITY_SETTING) {
                cipher.init(Cipher.ENCRYPT_MODE, key)
            } else {
                val iv = Base64.decode(config.fingerprintEncryptDataIV, Base64.DEFAULT)
                cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            }
            return true
        } catch (e: Exception) {
            viewModel.updateGuideMessage(getString(R.string.init_cipher_error_guide_message))
            return false
        }
    }

    private fun tryEncrypt(cipher: Cipher?) {
        try {
            cipher?.let {
                val encrypted = it.doFinal(FingerprintLockConstants.DUMMY_ENCRYPT_DATA.toByteArray(Charsets.UTF_8))
                val ivParams = it.parameters.getParameterSpec(IvParameterSpec::class.java)
                config.fingerprintEncryptData = Base64.encodeToString(encrypted, Base64.DEFAULT)
                config.fingerprintEncryptDataIV = Base64.encodeToString(ivParams.iv, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun tryDecrypt(cipher: Cipher?): Boolean =
        try {
            cipher?.let {
                val encodedData = Base64.decode(config.fingerprintEncryptData, Base64.DEFAULT)
                it.doFinal(encodedData)
                true
            } ?: false
        } catch (_: Exception) {
            viewModel.updateGuideMessage(getString(R.string.fingerprint_authentication_info_changed))
            false
        }
}
