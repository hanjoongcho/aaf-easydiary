package me.blog.korn123.commons.utils

import android.content.Context
import com.tozny.crypto.android.AesCbcWithIntegrity
import com.tozny.crypto.android.AesCbcWithIntegrity.keyString
import me.blog.korn123.easydiary.R

class AesUtils {
    companion object {
        fun encryptPassword(context: Context, plainText: String, password: String): String {
            var cipherText:String = ""
            val key: AesCbcWithIntegrity.SecretKeys = AesCbcWithIntegrity.generateKeyFromPassword(password, context.getString(R.string.oauth_requerst_id_token))

            // The encryption / storage & display:
            val civ = AesCbcWithIntegrity.encrypt(plainText, AesCbcWithIntegrity.keys(keyString(key)))
            cipherText = civ.toString()
            return cipherText
        }

        fun decryptPassword(context: Context, cipherText: String, password: String): String {
            val key: AesCbcWithIntegrity.SecretKeys = AesCbcWithIntegrity.generateKeyFromPassword(password, context.getString(R.string.oauth_requerst_id_token))
            var plainText:String = ""
            try {
                val cipherTextIvMac = AesCbcWithIntegrity.CipherTextIvMac(cipherText)
                plainText = AesCbcWithIntegrity.decryptString(cipherTextIvMac, AesCbcWithIntegrity.keys(keyString(key)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return plainText
        }
    }
}