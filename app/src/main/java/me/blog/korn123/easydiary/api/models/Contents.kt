package me.blog.korn123.easydiary.api.models

data class Contents(
    val name: String,
    val path: String,
    val download_url: String?,
    val sha: String
)