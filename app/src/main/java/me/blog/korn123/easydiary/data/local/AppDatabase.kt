package me.blog.korn123.easydiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.blog.korn123.easydiary.data.local.dao.ActionLogDao
import me.blog.korn123.easydiary.data.local.dao.AlarmDao
import me.blog.korn123.easydiary.data.local.dao.DDayDao
import me.blog.korn123.easydiary.data.local.dao.DiaryDao
import me.blog.korn123.easydiary.data.local.entity.ActionLogEntity
import me.blog.korn123.easydiary.data.local.entity.AlarmEntity
import me.blog.korn123.easydiary.data.local.entity.DDayEntity
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity

@Database(
    entities = [
        DiaryEntity::class,
        PhotoUriEntity::class,
        AlarmEntity::class,
        ActionLogEntity::class,
        DDayEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun alarmDao(): AlarmDao
    abstract fun actionLogDao(): ActionLogDao
    abstract fun dDayDao(): DDayDao
}
