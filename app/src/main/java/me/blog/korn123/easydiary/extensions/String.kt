package me.blog.korn123.easydiary.extensions
import java.util.Base64

// String -> Base64 String
fun String.toBase64(): String = Base64.getEncoder().encodeToString(this.toByteArray(Charsets.UTF_8))
