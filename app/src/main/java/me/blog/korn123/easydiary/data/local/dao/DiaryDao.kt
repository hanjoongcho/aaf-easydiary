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
    @Query(
        """
        SELECT * FROM diaries 
        WHERE (:query IS NULL OR :query = '' OR 
            CASE WHEN :isSensitive = 1 
                 THEN (LOWER(title) LIKE '%' || LOWER(:query) || '%' OR LOWER(contents) LIKE '%' || LOWER(:query) || '%')
                 ELSE (title LIKE '%' || :query || '%' OR contents LIKE '%' || :query || '%')
            END
        )
        AND (:startTimeMillis = 0 OR currentTimeMillis >= :startTimeMillis)
        AND (:endTimeMillis = 0 OR currentTimeMillis <= :endTimeMillis)
        AND (:symbolSequence = 0 OR :symbolSequence = 9999 OR symbolSequence = :symbolSequence)
        ORDER BY currentTimeMillis DESC
    """,
    )
    fun getAllDiaries(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
    ): Flow<List<DiaryEntity>>

    @Transaction
    @Query(
        """
        SELECT * FROM diaries 
        WHERE (:query IS NULL OR :query = '' OR 
            CASE WHEN :isSensitive = 1 
                 THEN (LOWER(title) LIKE '%' || LOWER(:query) || '%' OR LOWER(contents) LIKE '%' || LOWER(:query) || '%')
                 ELSE (title LIKE '%' || :query || '%' OR contents LIKE '%' || :query || '%')
            END
        )
        AND (:startTimeMillis = 0 OR currentTimeMillis >= :startTimeMillis)
        AND (:endTimeMillis = 0 OR currentTimeMillis <= :endTimeMillis)
        AND (:symbolSequence = 0 OR :symbolSequence = 9999 OR symbolSequence = :symbolSequence)
        ORDER BY currentTimeMillis DESC
    """,
    )
    fun getDiariesWithPhotos(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
    ): Flow<List<DiaryWithPhotos>>

    @Transaction
    @Query("SELECT * FROM diaries WHERE diaryId = :id")
    fun getDiaryWithPhotosById(id: Int): Flow<DiaryWithPhotos?>

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

    @Transaction
    suspend fun updateDiaryWithPhotos(
        diary: DiaryEntity,
        photoUris: List<PhotoUriEntity>,
    ) {
        updateDiary(diary)
        deletePhotoUrisByDiaryId(diary.diaryId)
        val photoEntities = photoUris.map { it.copy(diaryId = diary.diaryId) }
        insertPhotoUris(photoEntities)
    }

    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)

    @Query("DELETE FROM diaries WHERE diaryId = :id")
    suspend fun deleteDiaryById(id: Int)

    @Query("DELETE FROM diaries")
    suspend fun deleteAllDiaries()

    @Query("DELETE FROM photo_uris WHERE diaryId = :diaryId")
    suspend fun deletePhotoUrisByDiaryId(diaryId: Int)

    @Query("SELECT * FROM photo_uris")
    fun getPhotoUris(): Flow<List<PhotoUriEntity>>
}
