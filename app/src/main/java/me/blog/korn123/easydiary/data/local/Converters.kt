package me.blog.korn123.easydiary.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromLongList(value: List<Long>): String = Gson().toJson(value)

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val listType = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
