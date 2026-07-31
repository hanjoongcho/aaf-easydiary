package me.blog.korn123.easydiary.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.models.DiaryEntity

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diaries ORDER BY currentTimeMillis DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diaries WHERE diaryId = :id")
    suspend fun getDiaryById(id: Int): DiaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity)

    @Update
    suspend fun updateDiary(diary: DiaryEntity)

    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)

    @Query("DELETE FROM diaries WHERE diaryId = :id")
    suspend fun deleteDiaryById(id: Int)

    @Query("DELETE FROM diaries")
    suspend fun deleteAllDiaries()
}
