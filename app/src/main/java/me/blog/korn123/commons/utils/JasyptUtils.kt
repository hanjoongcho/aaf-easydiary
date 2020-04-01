package me.blog.korn123.commons.utils

import org.jasypt.util.text.BasicTextEncryptor
import java.security.MessageDigest

class JasyptUtils {
    companion object {
        fun encrypt(plainText: String, keyString: String): String {
            if (plainText.isEmpty()) return ""
            val basicTextEncryptor = BasicTextEncryptor()
            basicTextEncryptor.setPasswordCharArray(keyString.toCharArray())
            return basicTextEncryptor.encrypt(plainText)
        }

        fun decrypt(cipherText: String, keyString: String): String {
            if (cipherText.isEmpty()) return ""
            val basicTextEncryptor = BasicTextEncryptor()
            basicTextEncryptor.setPasswordCharArray(keyString.toCharArray())
            return basicTextEncryptor.decrypt(cipherText)
        }

        fun md5(targetString: String): String {
            return hashString(targetString, "MD5")
        }

        fun sha256(targetString: String): String {
            return hashString(targetString, "SHA-256")
        }

        private fun hashString(input: String, algorithm: String): String {
            return MessageDigest
                    .getInstance(algorithm)
                    .digest(input.toByteArray())
                    .fold("", { str, it -> str + "%02x".format(it) })
        }
    }
}