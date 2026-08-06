package me.blog.korn123.easydiary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.DDayEntity

@Dao
interface DDayDao {
    @Query("SELECT * FROM d_days ORDER BY sequence DESC")
    fun getAllDDays(): Flow<List<DDayEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertDDay(dDay: DDayEntity): Long

    @Update
    suspend fun updateDDay(dDay: DDayEntity)

    @Delete
    suspend fun deleteDDay(dDay: DDayEntity)

    @Query("DELETE FROM d_days WHERE sequence = :id")
    suspend fun deleteDDayById(id: Int)

    @Query("DELETE FROM d_days")
    suspend fun deleteAllDDays()
}