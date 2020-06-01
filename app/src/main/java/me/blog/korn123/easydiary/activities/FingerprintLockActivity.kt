package me.blog.korn123.easydiary.activities

import android.app.KeyguardManager
import android.content.DialogInterface
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import io.github.aafactory.commons.activities.BaseSimpleActivity
import kotlinx.android.synthetic.main.activity_fingerprint.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.util.concurrent.Executor
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec

class FingerprintLockActivity : BaseSimpleActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mKeyStore: KeyStore
    private lateinit var mKeyGenerator: KeyGenerator
    private lateinit var mFingerprintManager: FingerprintManagerCompat
    private lateinit var mCryptoObject: FingerprintManagerCompat.CryptoObject
    private var mCancellationSignal: CancellationSignal? = null
    private var mActivityMode: String? = null
    private var mSettingComplete = false


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)
        mActivityMode = intent.getStringExtra(LAUNCHING_MODE)

        changePinLock.setOnClickListener {
            startActivity(Intent(this, PinLockActivity::class.java).apply {
                putExtra(PinLockActivity.LAUNCHING_MODE, PinLockActivity.ACTIVITY_UNLOCK)
            })
            finish()
        }

        changePinLock.setOnLongClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && canAuthenticateWithBiometrics()) showBiometricPrompt()
            true
        }
    }

    override fun onResume() {
        super.onResume()

        if (!mSettingComplete) {
            guideMessage.text = getString(R.string.place_finger)
            FontUtils.setFontsTypeface(applicationContext, assets, null, container)
            changePinLock.visibility = if (mActivityMode == ACTIVITY_SETTING) View.GONE else View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // 01. KeyStore 인스턴스 생성
                try {
                    mKeyStore = KeyStore.getInstance("AndroidKeyStore")
                } catch (e: KeyStoreException) {
                    makeSnackBar("Failed to get an instance of KeyStore")
                }

                // 02. KeyGenerator 인스턴스 초기화
                try {
                    mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                } catch (e: Exception) {
                    makeSnackBar("Failed to get an instance of KeyGenerator")
                }

                // 03. Cipher 인스턴스 초기화
                var defaultCipher: Cipher? = null
                try {
                    defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
                } catch (e: NoSuchAlgorithmException) {
                    makeSnackBar("Failed to get an instance of Cipher")
                } catch (e: NoSuchPaddingException) {
                    makeSnackBar("Failed to get an instance of Cipher")
                }

                // 04. KeyguardManager service 초기화
                val keyguardManager = getSystemService(KeyguardManager::class.java)

                // 05. FingerprintManager service 초기화
                mFingerprintManager = FingerprintManagerCompat.from(this);

                // 06. screen lock 설정여부 확인
                if (!keyguardManager.isKeyguardSecure) {
                    // Show a message that the user hasn't set up a fingerprint or lock screen.
                    guideMessage.text = "Secure lock screen hasn't set up.\nGo to 'Settings -> Security -> Fingerprint' to set up a fingerprint"
                    return
                }

                // 07. fingerprint 등록여부 확인
                // Now the protection level of USE_FINGERPRINT permission is normal instead of dangerous.
                // See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
                // The line below prevents the false positive inspection from Android Studio
                // noinspection ResourceType
                if (!mFingerprintManager.hasEnrolledFingerprints()) {
                    // This happens when no fingerprints are registered.
                    guideMessage.text = "Go to 'Settings -> Security -> Fingerprint' and register at least one" + " fingerprint"
                    return
                }

                // 08. KeyGenerator를 이용하여 key 생성
                if (mActivityMode == ACTIVITY_SETTING) createKey(KEY_NAME, true)

                // 09. Cipher & CryptoObject 초기화
                // Set up the crypto object for later. The object will be authenticated by use
                // of the fingerprint.
                defaultCipher?.let {
                    if (initCipher(it, KEY_NAME)) {
                        mCryptoObject = FingerprintManagerCompat.CryptoObject(it)

                        // 10. 지문인식 시작
                        startListening(mCryptoObject)
                    } else {

                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mCancellationSignal?.cancel()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ActivityCompat.finishAffinity(this)
    }


    /***************************************************************************************************
     *   FingerprintManager
     *   
     ***************************************************************************************************/
    @RequiresApi(Build.VERSION_CODES.M)
    fun startListening(cryptoObject: FingerprintManagerCompat.CryptoObject) {
        // 11. fingerprint 센서 상태 및 권한 확인
        if (!isFingerprintAuthAvailable()) {
            return
        }
        mCancellationSignal = CancellationSignal()
//        mSelfCancelled = false
        // The line below prevents the false positive inspection from Android Studio

        // 12. fingerprint authentication callback 등록
        mFingerprintManager
                .authenticate(cryptoObject, 0 /* flags */, mCancellationSignal, object : FingerprintManagerCompat.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        config.fingerprintAuthenticationFailCount = 0

                        when (mActivityMode) {
                            ACTIVITY_SETTING -> {
                                tryEncrypt(mCryptoObject.cipher)
                                holdCurrentOrientation()
                                mSettingComplete = true
                                showAlertDialog(getString(R.string.fingerprint_setting_complete), DialogInterface.OnClickListener { _, _ ->
                                    config.fingerprintLockEnable = true
                                    pauseLock()
                                    finish()
                                }, false)
                            }
                            ACTIVITY_UNLOCK-> {
                                if (tryDecrypt(mCryptoObject.cipher)) {
                                    pauseLock()
                                    finish()
                                }
                            }
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                        config.fingerprintAuthenticationFailCount = ++config.fingerprintAuthenticationFailCount
                        updateErrorMessage(errString.toString())
                    }

                    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                        super.onAuthenticationHelp(helpCode, helpString)
                        updateErrorMessage(helpString.toString())
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        updateErrorMessage(getString(R.string.fingerprint_authentication_fail_try_again))
                    }
                }, null)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isFingerprintAuthAvailable(): Boolean {
        // The line below prevents the false positive inspection from Android Studio
        return mFingerprintManager.isHardwareDetected() && mFingerprintManager.hasEnrolledFingerprints()
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName the name of the key to be created
     * @param invalidatedByBiometricEnrollment if `false` is passed, the created key will not
     * be invalidated even if a new fingerprint is enrolled.
     * The default value is `true`, so passing
     * `true` doesn't change the behavior
     * (the key will be invalidated if a new fingerprint is
     * enrolled.). Note that this parameter is only valid if
     * the app works on Android N developer preview.
     */
//    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.M)
    fun createKey(keyName: String, invalidatedByBiometricEnrollment: Boolean) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null)
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            val builder = KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
            }
            mKeyGenerator.init(builder.build())
            mKeyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * Initialize the [Cipher] instance with the created key in the
     * [.createKey] method.
     *
     * @param keyName the key name to init the cipher
     * @return `true` if initialization is successful, `false` if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initCipher(cipher: Cipher, keyName: String): Boolean {
        try {
            mKeyStore.load(null)
            val key = mKeyStore.getKey(keyName, null) as SecretKey
            when (mActivityMode) {
                ACTIVITY_SETTING -> cipher.init(Cipher.ENCRYPT_MODE, key)
                ACTIVITY_UNLOCK -> {
                    val iv = Base64.decode(config.fingerprintEncryptDataIV, Base64.DEFAULT)
                    val ivParams = IvParameterSpec(iv)
                    cipher.init(Cipher.DECRYPT_MODE, key, ivParams)
                }
            }
            return true
//        } catch (e: KeyPermanentlyInvalidatedException) {
//            updateErrorMessage(e.message ?: "KeyPermanentlyInvalidatedException")
//        } catch (e: KeyStoreException) {
//            updateErrorMessage(e.message ?: "KeyStoreException")
//        } catch (e: CertificateException) {
//            updateErrorMessage(e.message ?: "CertificateException")
//        } catch (e: UnrecoverableKeyException) {
//            updateErrorMessage(e.message ?: "UnrecoverableKeyException")
//        } catch (e: IOException) {
//            updateErrorMessage(e.message ?: "IOException")
//        } catch (e: NoSuchAlgorithmException) {
//            updateErrorMessage(e.message ?: "NoSuchAlgorithmException")
//        } catch (e: InvalidKeyException) {
//            updateErrorMessage(e.message ?: "InvalidKeyException")
        } catch (e: Exception) {
            updateErrorMessage(getString(R.string.init_cipher_error_guide_message))
        }
        return false
    }

    /**
     * Tries to encrypt some data with the generated key in [.createKey] which is
     * only works if the user has just authenticated via fingerprint.
     */
    private fun tryEncrypt(cipher: Cipher?) {
        try {
            cipher?.let {
                val encrypted = it.doFinal(DUMMY_ENCRYPT_DATA.toByteArray())
                val ivParams = it.parameters.getParameterSpec(IvParameterSpec::class.java)
                val iv = Base64.encodeToString(ivParams.iv, Base64.DEFAULT)
                config.fingerprintEncryptData = Base64.encodeToString(encrypted, Base64.DEFAULT)
                config.fingerprintEncryptDataIV = iv
            }
        } catch (e: BadPaddingException) {
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun tryDecrypt(cipher: Cipher?): Boolean {
        var result = true
        try {
            cipher?.let {
                val encodedData = Base64.decode(config.fingerprintEncryptData, Base64.DEFAULT)
                val decodedData = cipher.doFinal(encodedData)
                Log.i(TAG, "decode dummy data: ${String(decodedData)}, origin dummy data: $DUMMY_ENCRYPT_DATA")
            }
        } catch (e: Exception) {
            updateErrorMessage(getString(R.string.fingerprint_authentication_info_changed))
            result = false
        }
        return result
    }

    private fun updateErrorMessage(errorMessage: String) {
        guideMessage.text = errorMessage
    }
    

    /***************************************************************************************************
     *   Biometric Prompt
     *   https://github.com/Kieun/android-biometricprompt
     ***************************************************************************************************/
    private fun getMainThreadExecutor(): Executor = MainThreadExecutor()

    private class MainThreadExecutor : Executor {
        private val handler = Handler(Looper.getMainLooper())

        override fun execute(r: Runnable) {
            handler.post(r)
        }
    }

    /**
     * Indicate whether this device can authenticate the user with biometrics
     * @return true if there are any available biometric sensors and biometrics are enrolled on the device, if not, return false
     */
    private fun canAuthenticateWithBiometrics(): Boolean {
        // Check whether the fingerprint can be used for authentication (Android M to P)
        if (Build.VERSION.SDK_INT < 29) {
            val fingerprintManagerCompat = FingerprintManagerCompat.from(this)
            return fingerprintManagerCompat.hasEnrolledFingerprints() && fingerprintManagerCompat.isHardwareDetected
        } else {    // Check biometric manager (from Android Q)
            val biometricManager = this.getSystemService<BiometricManager>(BiometricManager::class.java)
            return if (biometricManager != null) {
                biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
            } else false
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showBiometricPrompt() {
        val authenticationCallback = getAuthenticationCallback()
        val mBiometricPrompt = BiometricPrompt(this, getMainThreadExecutor(), authenticationCallback)

        // Set prompt info
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setDescription(getString(R.string.place_finger))
                .setTitle(getString(R.string.app_name))
//                .setSubtitle("Subtitle")
                .setNegativeButtonText(getString(R.string.cancel))
                .build()

        mBiometricPrompt.authenticate(promptInfo)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getAuthenticationCallback(): BiometricPrompt.AuthenticationCallback {
        return object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                makeSnackBar(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                pauseLock()
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                makeSnackBar("onAuthenticationFailed")
            }
        }
    }
    
    
    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    companion object {
        private val TAG = FingerprintLockActivity::class.java.simpleName
        const val KEY_NAME = "me.blog.korn123"
        const val DUMMY_ENCRYPT_DATA = "aaf-easydiary"
        const val LAUNCHING_MODE   = "launching_mode"
        const val ACTIVITY_SETTING = "activity_setting"
        const val ACTIVITY_UNLOCK  = "activity_unlock"
    }
}