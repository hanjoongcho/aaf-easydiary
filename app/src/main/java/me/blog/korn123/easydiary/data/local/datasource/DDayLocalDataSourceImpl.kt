package me.blog.korn123.easydiary.data.local.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.datasource.DDayDataSource
import me.blog.korn123.easydiary.data.local.dao.DDayDao
import me.blog.korn123.easydiary.data.local.entity.DDayEntity
import javax.inject.Inject

class DDayLocalDataSourceImpl
    @Inject
    constructor(
        private val dDayDao: DDayDao,
    ) : DDayDataSource {
        override fun getAllDDays(): Flow<List<DDayEntity>> = dDayDao.getAllDDays()

        override suspend fun insertDDay(dDay: DDayEntity) {
            dDayDao.insertDDay(dDay)
        }

        override suspend fun updateDDay(dDay: DDayEntity) {
            dDayDao.updateDDay(dDay)
        }

        override suspend fun deleteDDay(dDay: DDayEntity) {
            dDayDao.deleteDDay(dDay)
        }

        override suspend fun deleteDDayById(id: Int) {
            dDayDao.deleteDDayById(id)
        }

        override suspend fun deleteAllDDays() {
            dDayDao.deleteAllDDays()
        }
    }