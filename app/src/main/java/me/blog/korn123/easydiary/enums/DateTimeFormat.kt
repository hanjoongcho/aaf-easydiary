package me.blog.korn123.easydiary.enums

enum class DateTimeFormat(val indexChar: String) {
    DATE_LONG_AND_TIME_LONG("0"),
    DATE_MEDIUM_AND_TIME_MEDIUM("1"),
    DATE_MEDIUM_AND_TIME_SHORT("2"),
    DATE_SHORT_AND_TIME_SHORT("3"),
}