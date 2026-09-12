package me.blog.korn123.easydiary.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.realm.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.blog.korn123.easydiary.data.datasource.ActionLogDataSource
import me.blog.korn123.easydiary.data.datasource.DDayDataSource
import me.blog.korn123.easydiary.data.datasource.LocalDataSource
import me.blog.korn123.easydiary.data.datasource.RemoteDataSource
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.domain.repository.DDayRepository
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DDayRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @LocalDataSource private val localDataSource: DDayDataSource,
        @RemoteDataSource private val remoteDataSource: DDayDataSource,
    ) : DDayRepository {
        private val dataSource: DDayDataSource
            //            get() = if (context.config.enableJetpackRoomDatabase) localDataSource else remoteDataSource
            // FIXME: Remove temporary code when migrate to Jetpack Room
            get() = localDataSource

        override fun getAllDDaysFlow(): Flow<List<DDay>> =
            dataSource.getAllDDays().map { entities ->
                entities.map { it.toDomain() }
            }

        // FIXME: Remove legacy realm functions
        override suspend fun getAllDDays(isReverse: Boolean): List<DDay> =
            if (context.config.enableJetpackRoomDatabase) {
                val items = this.getAllDDaysFlow().first()
                if (isReverse) items.reversed() else items
            } else {
                EasyDiaryDbHelper.findDDayAll(if (isReverse) Sort.DESCENDING else Sort.ASCENDING)
            }

        override suspend fun insertDDay(dDay: DDay) {
            val entity = dDay.toEntity()
            dataSource.insertDDay(entity)
        }

        override suspend fun updateDDay(dDay: DDay) {
            val nextId = if (dDay.id > 0) (this.getAllDDays().maxOfOrNull { it.id } ?: 0) + 1 else 1
            val entity = dDay.toEntity()
            dataSource.updateDDay(entity.copy(id = nextId))

            // FIXME: Remove legacy realm functions
            EasyDiaryDbHelper.updateDDay(dDay)
        }

        override suspend fun deleteDDay(dDay: DDay) {
            val entity = dDay.toEntity()
            dataSource.deleteDDay(entity)

            // FIXME: Remove legacy realm functions
            EasyDiaryDbHelper.deleteDDayById(dDay.id)
        }

        override suspend fun deleteDDayById(id: Int) {
            dataSource.deleteDDayById(id)
        }

        override suspend fun deleteAllDDays() {
            localDataSource.deleteAllDDays()
        }
    }
