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
import me.blog.korn123.easydiary.data.local.entity.DiaryTagCrossRef
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos

@Dao
interface DiaryDao {
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
        AND (:tagId IS NULL OR :tagId = 0 OR EXISTS (
            SELECT 1 FROM diary_tag_cross_ref crossRef
            WHERE crossRef.diaryId = diaries.diaryId AND crossRef.tagId = :tagId
        ))
        ORDER BY currentTimeMillis DESC
    """,
    )
    suspend fun getDiariesWithPhotos(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
        tagId: Long? = null,
    ): List<DiaryWithPhotos>

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
        AND (:tagId IS NULL OR :tagId = 0 OR EXISTS (
            SELECT 1 FROM diary_tag_cross_ref crossRef
            WHERE crossRef.diaryId = diaries.diaryId AND crossRef.tagId = :tagId
        ))
        ORDER BY currentTimeMillis DESC
    """,
    )
    fun getDiariesWithPhotosFlow(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
        tagId: Long? = null,
    ): Flow<List<DiaryWithPhotos>>

    @Transaction
    @Query("SELECT * FROM diaries WHERE diaryId = :id")
    fun getDiaryWithPhotosById(id: Long): Flow<DiaryWithPhotos?>

    @Transaction
    @Query(
        """
        SELECT DISTINCT d.* FROM diaries d
        INNER JOIN photo_uris p ON d.diaryId = p.diaryId
        WHERE p.photoUri LIKE '%' || :photoUriString || '%'
        LIMIT 1
    """,
    )
    fun getDiaryWithPhotosByPhotoUri(photoUriString: String): Flow<DiaryWithPhotos?>

    @Query("SELECT * FROM diaries WHERE diaryId = :id")
    suspend fun getDiaryById(id: Long): DiaryEntity?

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
        tagIds: List<Long>,
    ): Long {
        val diaryId = insertDiary(diary)
        val photoEntities = photoUris.map { it.copy(diaryId = diaryId) }
        insertPhotoUris(photoEntities)
        replaceDiaryTagCrossRefs(diaryId, tagIds)
        return diaryId
    }

    /**
     * Writes the attachments of [diaryId] as [tagIds], replacing what the entry carried before (issue #270).
     * An empty list is a deliberate "no tags", so callers that do not edit tags never reach this method
     * (see `DiaryDao.updateDiaryWithPhotos`).
     */
    @Transaction
    suspend fun replaceDiaryTagCrossRefs(
        diaryId: Long,
        tagIds: List<Long>,
    ) {
        deleteTagCrossRefsByDiaryId(diaryId)
        insertTagCrossRefs(tagIds.distinct().map { DiaryTagCrossRef(diaryId = diaryId, tagId = it) })
    }

    @Query("DELETE FROM diary_tag_cross_ref WHERE diaryId = :diaryId")
    suspend fun deleteTagCrossRefsByDiaryId(diaryId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRefs(crossRefs: List<DiaryTagCrossRef>)

    @Query("SELECT diaryId FROM diaries")
    suspend fun getDiaryIds(): List<Long>

    @Transaction
    suspend fun insertDiariesWithPhotos(
        diariesWithPhotos: List<Pair<DiaryEntity, List<PhotoUriEntity>>>,
    ) {
        // A restore carries its tag attachments in their own collection of the backup file, so the entries are
        // written without tags here and the attachments are added after.
        diariesWithPhotos.forEach { (diary, photoUris) ->
            insertDiaryWithPhotos(diary, photoUris, emptyList())
        }
    }

    @Update
    suspend fun updateDiary(diary: DiaryEntity)

    @Update
    suspend fun updateDiaries(diaries: List<DiaryEntity>)

    @Transaction
    suspend fun updateDiaryWithPhotos(
        diary: DiaryEntity,
        photoUris: List<PhotoUriEntity>,
        tagIds: List<Long>?,
    ) {
        updateDiary(diary)
        deletePhotoUrisByDiaryId(diary.diaryId)
        val photoEntities = photoUris.map { it.copy(diaryId = diary.diaryId) }
        insertPhotoUris(photoEntities)
        // A null list means the caller edits something else than the tags (encryption, linking, restoring),
        // so the attachments the entry already has are left alone.
        tagIds?.let { replaceDiaryTagCrossRefs(diary.diaryId, it) }
    }

    @Delete
    suspend fun deleteDiary(diary: DiaryEntity)

    @Query("DELETE FROM diaries WHERE diaryId = :id")
    suspend fun deleteDiaryById(id: Long)

    @Query("DELETE FROM diaries WHERE originDiaryId = :originDiaryId")
    suspend fun deleteTemporaryDiaryBy(originDiaryId: Long)

    @Query("DELETE FROM diaries")
    suspend fun deleteAllDiaries()

    @Query("DELETE FROM photo_uris WHERE diaryId = :diaryId")
    suspend fun deletePhotoUrisByDiaryId(diaryId: Long)

    @Query("SELECT * FROM photo_uris")
    fun getPhotoUris(): Flow<List<PhotoUriEntity>>

    @Query(
        """
    SELECT * FROM diaries 
    WHERE ',' || REPLACE(REPLACE(REPLACE(linkedDiaries, ' ', ''), '[', ''), ']', '') || ',' 
          LIKE '%,' || :sequence || ',%'
    ORDER BY currentTimeMillis ASC
""",
    )
    fun findParentDiariesOf(sequence: Long): Flow<List<DiaryEntity>>

    @Query(
        """
        SELECT * FROM diaries 
        WHERE originDiaryId = 0 AND dateString = :dateString 
        ORDER BY 
            CASE WHEN :isAsc = 1 THEN currentTimeMillis END ASC,
            CASE WHEN :isAsc = 0 THEN currentTimeMillis END DESC
    """,
    )
    fun observeDiariesByDateString(
        dateString: String,
        isAsc: Boolean,
    ): Flow<List<DiaryEntity>>

    @Query(
        """
        SELECT * FROM diaries 
        WHERE originDiaryId = 0 AND dateString = :dateString 
        ORDER BY 
            CASE WHEN :isAsc = 1 THEN currentTimeMillis END ASC,
            CASE WHEN :isAsc = 0 THEN currentTimeMillis END DESC
    """,
    )
    suspend fun getDiariesByDateString(
        dateString: String,
        isAsc: Boolean,
    ): List<DiaryEntity>

    @Query(
        """
        SELECT * FROM diaries 
        WHERE originDiaryId = 0 AND dateString BETWEEN :startDate AND :endDate
        ORDER BY currentTimeMillis DESC
    """,
    )
    suspend fun getDiariesByDateRange(
        startDate: String,
        endDate: String,
    ): List<DiaryEntity>

    @Query("UPDATE diaries SET isSelected = 0")
    suspend fun clearSelectedStatus()

    @Transaction
    @Query("SELECT * FROM diaries ORDER BY currentTimeMillis ASC LIMIT 1")
    suspend fun findOldestDiary(): DiaryEntity?
}
