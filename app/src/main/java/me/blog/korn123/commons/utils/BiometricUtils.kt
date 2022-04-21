package me.blog.korn123.commons.utils

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.makeToast
import java.util.concurrent.Executor

class BiometricUtils {
    companion object {

        /*
         * Condition I: Check if the android version in device is greater than
         * Marshmallow, since fingerprint authentication is only supported
         * from Android 6.0.
         * Note: If your project's minSdkversion is 23 or higher,
         * then you won't need to perform this check.
         *
         * */
        private fun isSdkVersionSupported(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }

        private fun isBiometricPromptEnabled(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        }

        private fun isBiometricManagerEnabled(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }

        fun startListening(activity: AppCompatActivity) {
            when {
                isSdkVersionSupported() && isBiometricPromptEnabled() -> startListeningBiometric(activity)
                isSdkVersionSupported() -> startListeningFingerprint(activity)
            }
        }

        fun startListeningBiometric(activity: AppCompatActivity) {
            if (isBiometricManagerEnabled() && canAuthenticateWithBiometrics(activity)) {
                showBiometricPrompt(activity)
            }
        }

        @Suppress("DEPRECATION")
        fun startListeningFingerprint(activity: AppCompatActivity) {
            val cancellationSignal = CancellationSignal()
            FingerprintManagerCompat.from(activity)
                .authenticate(null, 0 /* flags */, cancellationSignal, object : FingerprintManagerCompat.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        activity.makeToast("onAuthenticationSucceeded")
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                        activity.makeToast("onAuthenticationError")
                    }

                    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                        super.onAuthenticationHelp(helpCode, helpString)
                        activity.makeToast("onAuthenticationHelp")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        activity.makeToast("onAuthenticationFailed")
                    }
                }, null)
        }

        /*
         * Condition II: Check if the device has fingerprint sensors.
         * Note: If you marked android.hardware.fingerprint as something that
         * your app requires (android:required="true"), then you don't need
         * to perform this check.
         *
         * */
        @RequiresApi(Build.VERSION_CODES.M)
        @Suppress("DEPRECATION")
        fun isHardwareSupported(context: Context): Boolean {
            val fingerprintManager = FingerprintManagerCompat.from(context)
            return fingerprintManager.isHardwareDetected
        }

        /**
         * screen lock 설정여부 확인
         */
        @RequiresApi(Build.VERSION_CODES.M)
        fun isKeyguardSecure(context: Context): Boolean {
            return context.getSystemService(KeyguardManager::class.java).isKeyguardSecure
        }

        /*
         * Condition III: Fingerprint authentication can be matched with a
         * registered fingerprint of the user. So we need to perform this check
         * in order to enable fingerprint authentication
         *
         * */
        @RequiresApi(Build.VERSION_CODES.M)
        @Suppress("DEPRECATION")
        fun isFingerprintAvailable(context: Context): Boolean {
            val fingerprintManager = FingerprintManagerCompat.from(context)
            return fingerprintManager.hasEnrolledFingerprints()
        }


        /*
         * Condition IV: Check if the permission has been added to
         * the app. This permission will be granted as soon as the user
         * installs the app on their device.
         *
         * */
        @RequiresApi(Build.VERSION_CODES.M)
        @Suppress("DEPRECATION")
        fun isPermissionGranted(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED
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
            } else false
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
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setDescription(getString(R.string.place_finger_description))
                    .setTitle(getString(R.string.app_name))
//                .setSubtitle("Subtitle")
                    .setNegativeButtonText(getString(R.string.cancel))
                    .build()

                mBiometricPrompt.authenticate(promptInfo)
            }
        }

        @RequiresApi(Build.VERSION_CODES.P)
        private fun getAuthenticationCallback(activity: Activity): BiometricPrompt.AuthenticationCallback {
            return object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
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
}