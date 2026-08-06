package me.blog.korn123.easydiary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
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

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertDiary(diary: DiaryEntity): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertDiaries(diaries: List<DiaryEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPhotoUris(photoUris: List<PhotoUriEntity>)

    @Transaction
    suspend fun insertDiaryWithPhotos(
        diary: DiaryEntity,
        photoUris: List<PhotoUriEntity>,
    ) {
        val diaryId = insertDiary(diary).toInt()
        val photoEntities = photoUris.map { it.copy(diaryId = diaryId) }
        insertPhotoUris(photoEntities)
    }

    @Transaction
    suspend fun insertDiariesWithPhotos(
        diariesWithPhotos: List<Pair<DiaryEntity, List<PhotoUriEntity>>>,
    ) {
        diariesWithPhotos.forEach { (diary, photoUris) ->
            insertDiaryWithPhotos(diary, photoUris)
        }
    }

    @Update
    suspend fun updateDiary(diary: DiaryEntity)

    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)

    @Query("DELETE FROM diaries WHERE diaryId = :id")
    suspend fun deleteDiaryById(id: Int)

    @Query("DELETE FROM diaries")
    suspend fun deleteAllDiaries()

    @Query("SELECT * FROM photo_uris")
    fun getPhotoUris(): Flow<List<PhotoUriEntity>>
}
