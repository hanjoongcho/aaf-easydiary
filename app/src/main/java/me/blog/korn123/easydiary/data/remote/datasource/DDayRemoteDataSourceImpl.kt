package me.blog.korn123.easydiary.data.remote.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.blog.korn123.easydiary.data.datasource.DDayDataSource
import me.blog.korn123.easydiary.data.local.entity.DDayEntity
import javax.inject.Inject

class DDayRemoteDataSourceImpl
    @Inject
    constructor() : DDayDataSource {
        override fun getAllDDays(): Flow<List<DDayEntity>> = flowOf(emptyList())

        override suspend fun insertDDay(dDay: DDayEntity) {
            // Mock implementation
        }

        override suspend fun updateDDay(dDay: DDayEntity) {
            // Mock implementation
        }

        override suspend fun deleteDDay(dDay: DDayEntity) {
            // Mock implementation
        }

        override suspend fun deleteDDayById(id: Int) {
            // Mock implementation
        }

        override suspend fun deleteAllDDays() {
            // Mock implementation
        }
    }