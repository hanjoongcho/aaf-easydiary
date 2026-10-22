package me.blog.korn123.easydiary.extensions
import java.util.Base64

// String -> Base64 String
fun String.toBase64(): String = Base64.getEncoder().encodeToString(this.toByteArray(Charsets.UTF_8))

fun String.getBaseName(): String =
    this
        .substringAfterLast('/')
        .substringAfterLast('\\')
        .substringBeforeLast('.')

fun String.getExtension(): String {
    val fileName = this.substringAfterLast('/').substringAfterLast('\\')
    return if (fileName.contains('.')) fileName.substringAfterLast('.') else ""
}
