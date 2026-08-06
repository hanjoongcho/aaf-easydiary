package me.blog.korn123.easydiary.data.remote.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.blog.korn123.easydiary.data.datasource.AlarmDataSource
import me.blog.korn123.easydiary.data.local.entity.AlarmEntity
import javax.inject.Inject

class AlarmRemoteDataSourceImpl
    @Inject
    constructor() : AlarmDataSource {
        override fun getAllAlarms(): Flow<List<AlarmEntity>> = flowOf(emptyList())

        override suspend fun getAlarmById(id: Int): AlarmEntity? = null

        override suspend fun insertAlarm(alarm: AlarmEntity) {
            // Mock implementation
        }

        override suspend fun updateAlarm(alarm: AlarmEntity) {
            // Mock implementation
        }

        override suspend fun deleteAlarm(alarm: AlarmEntity) {
            // Mock implementation
        }

        override suspend fun deleteAlarmById(id: Int) {
            // Mock implementation
        }

        override suspend fun deleteAllAlarms() {
            // Mock implementation
        }
    }