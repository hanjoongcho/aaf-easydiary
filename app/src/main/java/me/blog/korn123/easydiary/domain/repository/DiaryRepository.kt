package me.blog.korn123.easydiary.domain.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.domain.model.Diary

interface DiaryRepository {
    fun getAllDiaries(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
        checkFutureDiaryOption: Boolean = false,
    ): Flow<List<Diary>>

    fun getDiariesWithPhotos(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
    ): Flow<List<Diary>>

    fun getDiaryWithPhotosById(id: Int): Flow<Diary?>

    fun getDiaryWithPhotosByPhotoUri(photoUriString: String): Flow<Diary?>

    fun getDiariesWithPhotosByDateString(
        dateString: String,
        isAsc: Boolean = false,
    ): Flow<List<Diary>>

    suspend fun getDiaryById(seq: Int): Diary?

    suspend fun insertDiary(diary: Diary)

    suspend fun insertTemporaryDiary(diary: Diary)

    suspend fun deleteTemporaryDiaryBy(originDiaryId: Int)

    suspend fun addAllDiaries(diaries: List<Diary>)

    suspend fun updateDiaryWithPhotos(diary: Diary)

    suspend fun deleteDiary(diary: Diary)

    suspend fun deleteDiaryById(seq: Int)

    suspend fun deleteAllDiaries()

    fun getPhotoUris(): Flow<List<PhotoUriEntity>>

    fun findParentDiariesOf(sequence: Int): Flow<List<Diary>>
}
