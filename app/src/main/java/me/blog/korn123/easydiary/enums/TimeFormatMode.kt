package me.blog.korn123.easydiary.enums

/**
 * 일기 화면에 표시되는 시간의 표시형식
 *
 * SYSTEM_DEFAULT: 기기 언어(로케일)의 기본 표시형식을 그대로 사용합니다.
 * HOUR_12: 오전/오후가 포함된 12시간제로 표시합니다.
 * HOUR_24: 24시간제로 표시합니다.
 */
enum class TimeFormatMode {
    SYSTEM_DEFAULT,
    HOUR_12,
    HOUR_24,
    ;

    companion object {
        fun fromName(name: String?): TimeFormatMode = entries.firstOrNull { it.name == name } ?: SYSTEM_DEFAULT
    }
}
