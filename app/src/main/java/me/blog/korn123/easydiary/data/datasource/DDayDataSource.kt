package me.blog.korn123.easydiary.data.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.DDayEntity

interface DDayDataSource {
    fun getAllDDays(): Flow<List<DDayEntity>>
    suspend fun insertDDay(dDay: DDayEntity)
    suspend fun updateDDay(dDay: DDayEntity)
    suspend fun deleteDDay(dDay: DDayEntity)
    suspend fun deleteDDayById(id: Int)
    suspend fun deleteAllDDays()
}