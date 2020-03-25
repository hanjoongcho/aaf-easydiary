package me.blog.korn123.commons.utils

import org.jasypt.util.text.BasicTextEncryptor

class JasyptUtils {
    companion object {
        fun encrypt(plainText: String, keyString: String): String {
            val basicTextEncryptor = BasicTextEncryptor()
            basicTextEncryptor.setPasswordCharArray(keyString.toCharArray())
            return basicTextEncryptor.encrypt(plainText)
        }

        fun decrypt(cipherText: String, keyString: String): String {
            val basicTextEncryptor = BasicTextEncryptor()
            basicTextEncryptor.setPasswordCharArray(keyString.toCharArray())
            return basicTextEncryptor.decrypt(cipherText)
        }
    }
}