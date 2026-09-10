package me.blog.korn123.easydiary.domain.model

import me.blog.korn123.easydiary.enums.ActionLogKey

data class ActionLog(
    val id: Int = 0,
    val className: String? = null,
    val signature: String? = null,
    val key: ActionLogKey = ActionLogKey.UNDEFINED,
    val value: String? = null,
)
