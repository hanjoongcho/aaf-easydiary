package me.blog.korn123.easydiary.api.models

data class CommitRequest(
    val message: String,
    val content: String, // base64 인코딩된 파일 내용
    val branch: String? = null,
    val sha: String? = null // 파일이 이미 존재하면 필요
)

data class CommitResponse(
    val content: Contents?,
    val commit: CommitInfo?
)

data class CommitInfo(
    val sha: String,
    val message: String
)