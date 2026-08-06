package me.blog.korn123.easydiary.data.remote.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.blog.korn123.easydiary.data.datasource.ActionLogDataSource
import me.blog.korn123.easydiary.data.local.entity.ActionLogEntity
import javax.inject.Inject

class ActionLogRemoteDataSourceImpl
    @Inject
    constructor() : ActionLogDataSource {
        override fun getAllActionLogs(): Flow<List<ActionLogEntity>> = flowOf(emptyList())

        override suspend fun insertActionLog(actionLog: ActionLogEntity) {
            // Mock implementation
        }

        override suspend fun deleteAllActionLogs() {
            // Mock implementation
        }
    }