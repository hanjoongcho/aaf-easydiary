package me.blog.korn123.easydiary.models

data class DiarySymbol(val info: String) {
    private val infoArr = info.split("|")
    val sequence = infoArr[0].toInt()
    val description = infoArr[1]
}