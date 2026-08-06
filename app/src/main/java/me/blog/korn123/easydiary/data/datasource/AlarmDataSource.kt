package me.blog.korn123.easydiary.data.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.AlarmEntity

interface AlarmDataSource {
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    suspend fun getAlarmById(id: Int): AlarmEntity?

    suspend fun insertAlarm(alarm: AlarmEntity)

    suspend fun updateAlarm(alarm: AlarmEntity)

    suspend fun deleteAlarm(alarm: AlarmEntity)

    suspend fun deleteAlarmById(id: Int)

    suspend fun deleteAllAlarms()
}