package me.blog.korn123.easydiary.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.blog.korn123.easydiary.data.datasource.DDayDataSource
import me.blog.korn123.easydiary.data.datasource.LocalDataSource
import me.blog.korn123.easydiary.data.datasource.RemoteDataSource
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.domain.repository.DDayRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DDayRepositoryImpl
    @Inject
    constructor(
        @LocalDataSource private val localDataSource: DDayDataSource,
        @RemoteDataSource private val remoteDataSource: DDayDataSource,
    ) : DDayRepository {
        override fun getAllDDays(): Flow<List<DDay>> =
            localDataSource.getAllDDays().map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun insertDDay(dDay: DDay) {
            val entity = dDay.toEntity()
            localDataSource.insertDDay(entity)
        }

        override suspend fun updateDDay(dDay: DDay) {
            val entity = dDay.toEntity()
            localDataSource.updateDDay(entity)
        }

        override suspend fun deleteDDay(dDay: DDay) {
            val entity = dDay.toEntity()
            localDataSource.deleteDDay(entity)
        }

        override suspend fun deleteDDayById(id: Int) {
            localDataSource.deleteDDayById(id)
        }

        override suspend fun deleteAllDDays() {
            localDataSource.deleteAllDDays()
        }
    }
