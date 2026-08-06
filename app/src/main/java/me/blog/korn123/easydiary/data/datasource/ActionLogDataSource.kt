package me.blog.korn123.easydiary.data.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.ActionLogEntity

interface ActionLogDataSource {
    fun getAllActionLogs(): Flow<List<ActionLogEntity>>
    suspend fun insertActionLog(actionLog: ActionLogEntity)
    suspend fun deleteAllActionLogs()
}