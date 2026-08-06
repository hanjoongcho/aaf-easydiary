package me.blog.korn123.easydiary.data.local.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.datasource.ActionLogDataSource
import me.blog.korn123.easydiary.data.local.dao.ActionLogDao
import me.blog.korn123.easydiary.data.local.entity.ActionLogEntity
import javax.inject.Inject

class ActionLogLocalDataSourceImpl
    @Inject
    constructor(
        private val actionLogDao: ActionLogDao,
    ) : ActionLogDataSource {
        override fun getAllActionLogs(): Flow<List<ActionLogEntity>> = actionLogDao.getAllActionLogs()

        override suspend fun insertActionLog(actionLog: ActionLogEntity) {
            actionLogDao.insertActionLog(actionLog)
        }

        override suspend fun deleteAllActionLogs() {
            actionLogDao.deleteAllActionLogs()
        }
    }