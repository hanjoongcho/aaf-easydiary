package me.blog.korn123.easydiary.enums

enum class DiaryMode(
    val description: String,
) {
    READ("This is the basic mode of the main screen of the diary."),
    DELETE("If you touch and hold a grid item on the main screen of the diary, it will change to Delete Mode."),
}
