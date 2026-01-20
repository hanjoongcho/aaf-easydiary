package me.blog.korn123.easydiary.enums

enum class DateTimeFormat(
    val keys: String,
) {
    DATE_FULL_AND_TIME_FULL("0|0"),
    DATE_FULL_AND_TIME_SHORT("0|3"),
    DATE_LONG_AND_TIME_LONG("1|1"),
    DATE_MEDIUM_AND_TIME_MEDIUM("2|2"),
    DATE_MEDIUM_AND_TIME_SHORT("2|3"),
    DATE_SHORT_AND_TIME_SHORT("3|3"),
    ;

    fun getDateKey() = keys.split("|")[0].toInt()

    fun getTimeKey() = keys.split("|")[1].toInt()
}
