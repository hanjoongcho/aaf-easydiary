package me.blog.korn123.easydiary.extensions
import java.util.Base64

// ByteArray -> Base64 String
fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)
