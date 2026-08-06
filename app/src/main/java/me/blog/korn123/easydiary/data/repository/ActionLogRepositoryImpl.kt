package me.blog.korn123.easydiary.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.blog.korn123.easydiary.data.datasource.ActionLogDataSource
import me.blog.korn123.easydiary.data.datasource.LocalDataSource
import me.blog.korn123.easydiary.data.datasource.RemoteDataSource
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.domain.model.ActionLog
import me.blog.korn123.easydiary.domain.repository.ActionLogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionLogRepositoryImpl
    @Inject
    constructor(
        @LocalDataSource private val localDataSource: ActionLogDataSource,
        @RemoteDataSource private val remoteDataSource: ActionLogDataSource,
    ) : ActionLogRepository {
        override fun getAllActionLogs(): Flow<List<ActionLog>> =
            localDataSource.getAllActionLogs().map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun insertActionLog(actionLog: ActionLog) {
            val entity = actionLog.toEntity()
            localDataSource.insertActionLog(entity)
        }

        override suspend fun deleteAllActionLogs() {
            localDataSource.deleteAllActionLogs()
        }
    }
