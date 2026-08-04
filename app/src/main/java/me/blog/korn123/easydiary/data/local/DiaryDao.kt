package me.blog.korn123.easydiary.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.models.DiaryEntity
import me.blog.korn123.easydiary.data.local.models.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diaries ORDER BY currentTimeMillis DESC")
    fun getAllDiaries(): Flow<List<DiaryEntity>>

    @Transaction
    @Query("SELECT * FROM diaries ORDER BY currentTimeMillis DESC")
    suspend fun getDiariesWithPhotos(): List<DiaryWithPhotos>

    @Query("SELECT * FROM diaries WHERE diaryId = :id")
    suspend fun getDiaryById(id: Int): DiaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoUris(photoUris: List<PhotoUriEntity>)

    @Transaction
    suspend fun insertDiaryWithPhotos(diary: DiaryEntity, photoUris: List<PhotoUriEntity>) {
        val diaryId = insertDiary(diary).toInt()
        val photoEntities = photoUris.map { it.copy(diaryId = diaryId) }
        insertPhotoUris(photoEntities)
    }

    @Update
    suspend fun updateDiary(diary: DiaryEntity)

    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)

    @Query("DELETE FROM diaries WHERE diaryId = :id")
    suspend fun deleteDiaryById(id: Int)

    @Query("DELETE FROM diaries")
    suspend fun deleteAllDiaries()
}
