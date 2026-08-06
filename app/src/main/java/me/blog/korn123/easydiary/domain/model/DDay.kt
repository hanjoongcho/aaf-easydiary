package me.blog.korn123.easydiary.domain.model

data class DDay(
    val sequence: Int = 0,
    val targetTimeStamp: Long = 0,
    val title: String? = null
)
