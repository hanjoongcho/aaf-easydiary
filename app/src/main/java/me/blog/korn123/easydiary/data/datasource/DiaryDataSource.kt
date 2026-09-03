package me.blog.korn123.easydiary.data.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos
import javax.inject.Qualifier

interface DiaryDataSource {
    fun getAllDiaries(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
    ): Flow<List<DiaryEntity>>

    fun getDiariesWithPhotos(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
    ): Flow<List<DiaryWithPhotos>>

    fun getDiaryWithPhotosById(id: Int): Flow<DiaryWithPhotos?>

    fun getDiaryWithPhotosByPhotoUri(photoUriString: String): Flow<DiaryWithPhotos?>

    fun getDiariesWithPhotosByDateString(
        dateString: String,
        isAsc: Boolean,
    ): Flow<List<DiaryWithPhotos>>

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

    suspend fun updateDiaryWithPhotos(
        diary: DiaryEntity,
        photoUris: List<PhotoUriEntity>,
    )

    suspend fun deleteDiary(diary: DiaryEntity)

    suspend fun deleteDiaryById(seq: Int)

    suspend fun deleteTemporaryDiaryBy(originDiaryId: Int)

    suspend fun deleteAllDiaries()

    fun getPhotoUris(): Flow<List<PhotoUriEntity>>

    fun findParentDiariesOf(sequence: Int): Flow<List<DiaryEntity>>
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteDataSource
