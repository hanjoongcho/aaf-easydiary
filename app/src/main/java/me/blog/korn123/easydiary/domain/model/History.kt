package me.blog.korn123.easydiary.domain.model

data class History(
    val historyTag: String,
    val date: String,
    val attachedPhotoPath: String,
    val sequence: Int
)