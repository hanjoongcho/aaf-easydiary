package me.blog.korn123.commons.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeSnackBar
import java.util.concurrent.Executor

class BiometricUtils {
    companion object {
        private fun isBiometricManagerEnabled(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        fun startListeningBiometric(activity: AppCompatActivity) {
            if (isBiometricManagerEnabled() && canAuthenticateWithBiometrics(activity)) {
                showBiometricPrompt(activity)
            }
        }

        private fun canAuthenticateWithBiometrics(context: Context): Boolean {
            val biometricManager = androidx.biometric.BiometricManager.from(context)
            return biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG,
            ) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
        }

        private fun getMainThreadExecutor(): Executor = MainThreadExecutor()

        private class MainThreadExecutor : Executor {
            private val handler = Handler(Looper.getMainLooper())

            override fun execute(r: Runnable) {
                handler.post(r)
            }
        }

        private fun showBiometricPrompt(activity: AppCompatActivity) {
            activity.run {
                val authenticationCallback = getAuthenticationCallback(activity)
                val mBiometricPrompt = BiometricPrompt(this, getMainThreadExecutor(), authenticationCallback)

                // Set prompt info
                val promptInfo =
                    BiometricPrompt.PromptInfo
                        .Builder()
                        .setDescription(getString(R.string.place_finger_description))
                        .setTitle(getString(R.string.app_name))
//                .setSubtitle("Subtitle")
                        .setNegativeButtonText(getString(R.string.cancel))
                        .build()

                mBiometricPrompt.authenticate(promptInfo)
            }
        }

        private fun getAuthenticationCallback(activity: Activity): BiometricPrompt.AuthenticationCallback =
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    activity.makeSnackBar(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    activity.makeSnackBar("onAuthenticationSucceeded")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    activity.makeSnackBar("onAuthenticationFailed")
                }
            }
    }
}
