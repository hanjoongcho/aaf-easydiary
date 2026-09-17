package me.blog.korn123.easydiary.data.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos
import me.blog.korn123.easydiary.domain.model.Diary
import javax.inject.Qualifier

interface DiaryDataSource {
    fun getDiariesWithPhotosFlow(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
    ): Flow<List<DiaryWithPhotos>>

    suspend fun getDiariesWithPhotos(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
    ): List<DiaryWithPhotos>

    fun getDiaryWithPhotosById(id: Int): Flow<DiaryWithPhotos?>

    fun getDiaryWithPhotosByPhotoUri(photoUriString: String): Flow<DiaryWithPhotos?>

    suspend fun getDiariesByDateString(
        dateString: String,
        isAsc: Boolean,
    ): List<DiaryEntity>

    suspend fun getDiariesByDateRange(
        startDate: String,
        endDate: String,
    ): List<DiaryEntity>

    suspend fun getDiaryById(seq: Int): DiaryEntity?

    suspend fun insertDiary(diary: DiaryEntity)

    suspend fun insertDiaryWithPhotos(
        diary: DiaryEntity,
        photoUris: List<PhotoUriEntity>,
    ): Int

    suspend fun insertDiariesWithPhotos(
        diariesWithPhotos: List<Pair<DiaryEntity, List<PhotoUriEntity>>>,
    )

    suspend fun updateDiary(diary: DiaryEntity)

    suspend fun updateDiaries(diaries: List<DiaryEntity>)

    suspend fun updateDiaryWithPhotos(
        diary: DiaryEntity,
        photoUris: List<PhotoUriEntity>,
    )

    suspend fun deleteDiary(diary: DiaryEntity)

    suspend fun deleteDiaryById(seq: Int)

    suspend fun deleteTemporaryDiaryBy(originDiaryId: Int)

    suspend fun deleteAllDiaries()

    suspend fun clearSelectedStatus()

    fun getPhotoUris(): Flow<List<PhotoUriEntity>>

    fun findParentDiariesOf(sequence: Int): Flow<List<DiaryEntity>>

    suspend fun findOldestDiary(): Diary?
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteDataSource
