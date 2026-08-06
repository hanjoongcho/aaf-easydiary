package me.blog.korn123.easydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_logs")
data class ActionLogEntity(
    @PrimaryKey(autoGenerate = true)
    var sequence: Int = 0,
    var className: String? = null,
    var signature: String? = null,
    var key: String? = null,
    var value: String? = null
)
