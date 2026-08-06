package me.blog.korn123.easydiary.domain.model

data class ActionLog(
    val sequence: Int = 0,
    val className: String? = null,
    val signature: String? = null,
    val key: String? = null,
    val value: String? = null
)
