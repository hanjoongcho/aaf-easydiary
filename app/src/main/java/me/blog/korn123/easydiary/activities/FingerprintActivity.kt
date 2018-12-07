package me.blog.korn123.easydiary.activities

import android.app.KeyguardManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyProperties
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.github.aafactory.commons.activities.BaseSimpleActivity
import kotlinx.android.synthetic.main.activity_lock_setting.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException

class FingerprintActivity : BaseSimpleActivity() {
    private var mKeyStore: KeyStore? = null
    private var mKeyGenerator: KeyGenerator? = null
    
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
            val fingerprintManager = getSystemService(FingerprintManager::class.java)

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
            if (!fingerprintManager.hasEnrolledFingerprints()) {
//                purchaseButton.setEnabled(false)
                // This happens when no fingerprints are registered.
                Toast.makeText(this,
                        "Go to 'Settings -> Security -> Fingerprint' and register at least one" + " fingerprint",
                        Toast.LENGTH_LONG).show()
                return
            }
//            createKey(DEFAULT_KEY_NAME, true)
//            createKey(KEY_NAME_NOT_INVALIDATED, false)
//            purchaseButton.setEnabled(true)
//            purchaseButton.setOnClickListener(
//                    PurchaseButtonClickListener(defaultCipher, DEFAULT_KEY_NAME))
        }
    }

    override fun onResume() {
        isBackgroundColorFromPrimaryColor = true
        super.onResume()
        FontUtils.setFontsTypeface(applicationContext, assets, null, container)
    }

    override fun getMainViewGroup(): ViewGroup? = container
}