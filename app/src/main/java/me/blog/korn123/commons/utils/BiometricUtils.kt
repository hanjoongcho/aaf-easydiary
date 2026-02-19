package me.blog.korn123.commons.utils

import android.app.Activity
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
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

        /**
         * Indicate whether this device can authenticate the user with biometrics
         * @return true if there are any available biometric sensors and biometrics are enrolled on the device, if not, return false
         */
        @RequiresApi(Build.VERSION_CODES.Q)
        @Suppress("DEPRECATION")
        private fun canAuthenticateWithBiometrics(context: Context): Boolean {
            val biometricManager = context.getSystemService(BiometricManager::class.java)
            return if (biometricManager != null) {
                biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
            } else {
                false
            }
        }

        private fun getMainThreadExecutor(): Executor = MainThreadExecutor()

        private class MainThreadExecutor : Executor {
            private val handler = Handler(Looper.getMainLooper())

            override fun execute(r: Runnable) {
                handler.post(r)
            }
        }

        @RequiresApi(Build.VERSION_CODES.P)
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

        @RequiresApi(Build.VERSION_CODES.P)
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
