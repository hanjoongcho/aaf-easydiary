package me.blog.korn123.easydiary.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.blog.korn123.easydiary.data.local.models.PhotoUriEntity

class Converters {
    @TypeConverter
    fun fromPhotoUriList(value: List<PhotoUriEntity>): String = Gson().toJson(value)

    @TypeConverter
    fun toPhotoUriList(value: String): List<PhotoUriEntity> {
        val listType = object : TypeToken<List<PhotoUriEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String = Gson().toJson(value)

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
