package me.blog.korn123.easydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "d_days")
data class DDayEntity(
    @PrimaryKey(autoGenerate = true)
    var sequence: Int = 0,
    var targetTimeStamp: Long = 0,
    var title: String? = null
)
