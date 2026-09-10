package me.blog.korn123.easydiary.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.blog.korn123.easydiary.data.datasource.ActionLogDataSource
import me.blog.korn123.easydiary.data.datasource.LocalDataSource
import me.blog.korn123.easydiary.data.datasource.RemoteDataSource
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.domain.model.ActionLog
import me.blog.korn123.easydiary.domain.repository.ActionLogRepository
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionLogRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @LocalDataSource private val localDataSource: ActionLogDataSource,
        @RemoteDataSource private val remoteDataSource: ActionLogDataSource,
    ) : ActionLogRepository {
        private val dataSource: ActionLogDataSource
            //            get() = if (context.config.enableJetpackRoomDatabase) localDataSource else remoteDataSource
            // FIXME: Remove temporary code when migrate to Jetpack Room
            get() = localDataSource

        override fun getAllActionLogsFlow(): Flow<List<ActionLog>> =
            dataSource.getAllActionLogs().map { entities ->
                entities.map { it.toDomain() }
            }

        // FIXME: Remove legacy realm functions
        override suspend fun getAllActionLogs(): List<ActionLog> =
            if (context.config.enableJetpackRoomDatabase) {
                this.getAllActionLogsFlow().first()
            } else {
                EasyDiaryDbHelper.findAllActionLogs()
            }

        override suspend fun insertActionLog(actionLog: ActionLog) {
            val entity = actionLog.toEntity()
            dataSource.insertActionLog(entity)

            // FIXME: Remove legacy realm functions
            EasyDiaryDbHelper.insertActionLog(actionLog, context)
        }

        override suspend fun deleteAllActionLogs(excludeRealm: Boolean) {
            dataSource.deleteAllActionLogs()

            // FIXME: Remove legacy realm functions
            if (excludeRealm.not()) {
                EasyDiaryDbHelper.deleteAllActionLogs()
            }
        }
    }
