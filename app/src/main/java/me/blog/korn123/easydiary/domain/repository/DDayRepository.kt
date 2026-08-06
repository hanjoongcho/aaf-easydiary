package me.blog.korn123.easydiary.domain.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.domain.model.DDay

interface DDayRepository {
    fun getAllDDays(): Flow<List<DDay>>
    suspend fun insertDDay(dDay: DDay)
    suspend fun updateDDay(dDay: DDay)
    suspend fun deleteDDay(dDay: DDay)
    suspend fun deleteDDayById(id: Int)
    suspend fun deleteAllDDays()
}
