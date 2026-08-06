package me.blog.korn123.easydiary.domain.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.domain.model.Alarm

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>

    suspend fun getAlarmById(id: Int): Alarm?

    suspend fun insertAlarm(alarm: Alarm)

    suspend fun updateAlarm(alarm: Alarm)

    suspend fun deleteAlarm(alarm: Alarm)

    suspend fun deleteAlarmById(id: Int)

    suspend fun deleteAllAlarms()
}
