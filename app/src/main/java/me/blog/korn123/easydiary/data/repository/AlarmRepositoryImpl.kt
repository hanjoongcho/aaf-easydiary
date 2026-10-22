package me.blog.korn123.easydiary.data.repository

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.blog.korn123.easydiary.data.datasource.AlarmDataSource
import me.blog.korn123.easydiary.data.datasource.LocalDataSource
import me.blog.korn123.easydiary.data.datasource.RemoteDataSource
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.domain.model.ActionLog
import me.blog.korn123.easydiary.domain.model.Alarm
import me.blog.korn123.easydiary.domain.repository.AlarmRepository
import me.blog.korn123.easydiary.extensions.config
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @LocalDataSource private val localDataSource: AlarmDataSource,
        @RemoteDataSource private val remoteDataSource: AlarmDataSource,
    ) : AlarmRepository {
        private val dataSource: AlarmDataSource
            //            get() = if (context.config.enableJetpackRoomDatabase) localDataSource else remoteDataSource
            // FIXME: Remove temporary code when migrate to Jetpack Room
            get() = localDataSource

        override fun getAllAlarmsFlow(): Flow<List<Alarm>> =
            dataSource.getAllAlarms().map { entities ->
                entities.map { it.toDomain() }
            }

        // FIXME: Remove legacy realm functions
        override suspend fun getAllAlarms(): List<Alarm> = this.getAllAlarmsFlow().first()

        // FIXME: Remove legacy realm functions
        override suspend fun getAlarmById(id: Int): Alarm? =
            dataSource
                .getAlarmById(id)
                ?.toDomain()

        override suspend fun insertAlarm(alarm: Alarm) {
            val entity = alarm.toEntity()
            dataSource.insertAlarm(entity)
        }

        override suspend fun insertAllAlarms(alarms: List<Alarm>) {
            dataSource.insertAllAlarms(alarms.map { it.toEntity() })
        }

        override suspend fun updateAlarm(alarm: Alarm) {
            val entity = alarm.toEntity()
            dataSource.updateAlarm(entity)
        }

        override suspend fun deleteAlarm(alarm: Alarm) {
            val entity = alarm.toEntity()
            dataSource.deleteAlarm(entity)
        }

        override suspend fun deleteAlarmById(id: Int) {
            Log.i("aaf-t", "deleteAlarmById id: $id")
            dataSource.deleteAlarmById(id)
        }

        override suspend fun deleteAllAlarms() {
            dataSource.deleteAllAlarms()
        }
    }
