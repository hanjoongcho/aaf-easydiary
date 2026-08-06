package me.blog.korn123.easydiary.data.local.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.datasource.AlarmDataSource
import me.blog.korn123.easydiary.data.local.dao.AlarmDao
import me.blog.korn123.easydiary.data.local.entity.AlarmEntity
import javax.inject.Inject

class AlarmLocalDataSourceImpl
    @Inject
    constructor(
        private val alarmDao: AlarmDao,
    ) : AlarmDataSource {
        override fun getAllAlarms(): Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()

        override suspend fun getAlarmById(id: Int): AlarmEntity? = alarmDao.getAlarmById(id)

        override suspend fun insertAlarm(alarm: AlarmEntity) {
            alarmDao.insertAlarm(alarm)
        }

        override suspend fun updateAlarm(alarm: AlarmEntity) {
            alarmDao.updateAlarm(alarm)
        }

        override suspend fun deleteAlarm(alarm: AlarmEntity) {
            alarmDao.deleteAlarm(alarm)
        }

        override suspend fun deleteAlarmById(id: Int) {
            alarmDao.deleteAlarmById(id)
        }

        override suspend fun deleteAllAlarms() {
            alarmDao.deleteAllAlarms()
        }
    }