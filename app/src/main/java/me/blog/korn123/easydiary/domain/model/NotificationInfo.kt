package me.blog.korn123.easydiary.domain.model

data class NotificationInfo(
    var largeIconResourceId: Int,
    var useActionButton: Boolean = false,
    var id: Int = 0,
)
