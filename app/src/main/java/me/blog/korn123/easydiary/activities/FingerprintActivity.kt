package me.blog.korn123.easydiary.activities

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.github.aafactory.commons.activities.BaseSimpleActivity
import kotlinx.android.synthetic.main.activity_fingerprint.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

class FingerprintActivity : BaseSimpleActivity() {
    private lateinit var mKeyStore: KeyStore
    private lateinit var mKeyGenerator: KeyGenerator
    private lateinit var mFingerprintManager: FingerprintManager
    private lateinit var mCryptoObject: FingerprintManager.CryptoObject
    private lateinit var mCancellationSignal: CancellationSignal
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)

        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to get an instance of KeyStore", e)
        }

        try {
            mKeyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get an instance of KeyGenerator", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get an instance of KeyGenerator", e)
        }

        val defaultCipher: Cipher
        val cipherNotInvalidated: Cipher
        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get an instance of Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get an instance of Cipher", e)
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            mFingerprintManager = getSystemService(FingerprintManager::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                purchaseButtonNotInvalidated.setEnabled(true)
//                purchaseButtonNotInvalidated.setOnClickListener(
//                        PurchaseButtonClickListener(cipherNotInvalidated,
//                                KEY_NAME_NOT_INVALIDATED))
            } else {
                // Hide the purchase button which uses a non-invalidated key
                // if the app doesn't work on Android N preview
//                purchaseButtonNotInvalidated.setVisibility(View.GONE)
//                findViewById<View>(R.id.purchase_button_not_invalidated_description).visibility = View.GONE
            }

            if (!keyguardManager.isKeyguardSecure) {
                // Show a message that the user hasn't set up a fingerprint or lock screen.
                Toast.makeText(this,
                        "Secure lock screen hasn't set up.\n" + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                        Toast.LENGTH_LONG).show()
//                purchaseButton.setEnabled(false)
//                purchaseButtonNotInvalidated.setEnabled(false)
                return
            }

            // Now the protection level of USE_FINGERPRINT permission is normal instead of dangerous.
            // See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
            // The line below prevents the false positive inspection from Android Studio
            // noinspection ResourceType
            if (!mFingerprintManager.hasEnrolledFingerprints()) {
//                purchaseButton.setEnabled(false)
                // This happens when no fingerprints are registered.
                Toast.makeText(this,
                        "Go to 'Settings -> Security -> Fingerprint' and register at least one" + " fingerprint",
                        Toast.LENGTH_LONG).show()
                return
            }
            createKey(DEFAULT_KEY_NAME, true)
//            createKey(KEY_NAME_NOT_INVALIDATED, false)
//            purchaseButton.setEnabled(true)
//            purchaseButton.setOnClickListener(
//                    PurchaseButtonClickListener(defaultCipher, DEFAULT_KEY_NAME))

            // fingerprint 인증시작
            // Set up the crypto object for later. The object will be authenticated by use
            // of the fingerprint.
            if (initCipher(defaultCipher, DEFAULT_KEY_NAME)) {
                mCryptoObject = FingerprintManager.CryptoObject(defaultCipher)
            } else {

            }
        }
    }

    override fun onResume() {
        isBackgroundColorFromPrimaryColor = true
        super.onResume()
        FontUtils.setFontsTypeface(applicationContext, assets, null, container)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            startListening(mCryptoObject)
        }
    }

    override fun onPause() {
        super.onPause()
        mCancellationSignal.cancel()
    }
    
    override fun getMainViewGroup(): ViewGroup? = container

    @RequiresApi(Build.VERSION_CODES.M)
    fun startListening(cryptoObject: FingerprintManager.CryptoObject) {
        if (!isFingerprintAuthAvailable()) {
            return
        }
        mCancellationSignal = CancellationSignal()
//        mSelfCancelled = false
        // The line below prevents the false positive inspection from Android Studio

        mFingerprintManager
                .authenticate(cryptoObject, mCancellationSignal, 0 /* flags */, object : FingerprintManager.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        guideMessage.text = "onAuthenticationSucceeded"
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                        guideMessage.text = "onAuthenticationError"
                    }

                    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                        super.onAuthenticationHelp(helpCode, helpString)
                        guideMessage.text = "onAuthenticationHelp"
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        guideMessage.text = "onAuthenticationFailed"
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
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }

    }
    
    companion object {
        const val DEFAULT_KEY_NAME = "default_key"
    }
}