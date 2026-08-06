package me.blog.korn123.easydiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.ActionLogEntity

@Dao
interface ActionLogDao {
    @Query("SELECT * FROM action_logs ORDER BY sequence DESC")
    fun getAllActionLogs(): Flow<List<ActionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertActionLog(actionLog: ActionLogEntity): Long

    @Query("DELETE FROM action_logs")
    suspend fun deleteAllActionLogs()
}