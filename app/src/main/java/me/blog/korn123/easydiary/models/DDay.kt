package me.blog.korn123.easydiary.models

open class DDay {
    var targetTimeStamp: Long = 0
    var title: String? = null

    constructor()

    constructor(title: String) {
        this.title = title
    }
}