package me.blog.korn123.easydiary.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.blog.korn123.easydiary.data.datasource.AlarmDataSource
import me.blog.korn123.easydiary.data.datasource.LocalDataSource
import me.blog.korn123.easydiary.data.datasource.RemoteDataSource
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.domain.model.Alarm
import me.blog.korn123.easydiary.domain.repository.AlarmRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    @LocalDataSource private val localDataSource: AlarmDataSource,
    @RemoteDataSource private val remoteDataSource: AlarmDataSource
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> =
        localDataSource.getAllAlarms().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getAlarmById(id: Int): Alarm? =
        localDataSource.getAlarmById(id)?.toDomain()

    override suspend fun insertAlarm(alarm: Alarm) {
        val entity = alarm.toEntity()
        localDataSource.insertAlarm(entity)
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        val entity = alarm.toEntity()
        localDataSource.updateAlarm(entity)
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        val entity = alarm.toEntity()
        localDataSource.deleteAlarm(entity)
    }

    override suspend fun deleteAlarmById(id: Int) {
        localDataSource.deleteAlarmById(id)
    }

    override suspend fun deleteAllAlarms() {
        localDataSource.deleteAllAlarms()
    }
}
