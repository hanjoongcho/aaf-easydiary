package me.blog.korn123.easydiary.domain.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.domain.model.ActionLog

interface ActionLogRepository {
    fun getAllActionLogsFlow(): Flow<List<ActionLog>>

    suspend fun getAllActionLogs(): List<ActionLog>

    suspend fun insertActionLog(actionLog: ActionLog)

    suspend fun deleteAllActionLogs(excludeRealm: Boolean = true)
}
