package me.blog.korn123.easydiary.domain.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.domain.model.Diary

/**
 * [Data Layer / Repository 메서드 Naming Conventions]
 *
 * 1. get...
 *   - [의도] 데이터의 존재가 명확하거나, 비즈니스 로직상 반드시 존재해야 하는 대상을 조회합니다.
 *   - [단일 객체] 반환 타입: Non-nullable (T)
 *     - 대상 데이터가 없으면 Exception을 던집니다 (e.g., NoSuchElementException).
 *   - [컬렉션/배열] 반환 타입: List<T>
 *     - 대상 목록이 비어있는 상황 자체가 비정상(에러)일 때 사용하며, 비어있을 경우 Exception을 던집니다.
 *
 * 2. find...
 *   - [의도] 데이터 존재 여부가 불확실하여 조건에 맞는 대상을 검색/조회합니다.
 *   - [단일 객체] 반환 타입: Nullable (T?)
 *     - 대상 데이터가 없으면 null을 반환합니다.
 *   - [컬렉션/배열] 반환 타입: List<T>
 *     - 결과가 없는 상황도 정상 흐름으로 간주하며, 비어있을 경우 emptyList()를 반환합니다.
 */
interface DiaryRepository {
    fun getDiariesWithPhotosFlow(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
    ): Flow<List<Diary>>

    suspend fun getDiariesWithPhotos(
        query: String? = null,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
    ): List<Diary>

    fun getDiaryWithPhotosById(id: Int): Flow<Diary?>

    fun getDiaryWithPhotosByPhotoUri(photoUriString: String): Flow<Diary?>

    suspend fun getDiariesByDateString(
        dateString: String,
        isAsc: Boolean = false,
    ): List<Diary>

    suspend fun getDiariesByDateRange(
        startDate: String,
        endDate: String,
    ): List<Diary>

    suspend fun getDiaryById(seq: Int): Diary?

    suspend fun insertDiary(diary: Diary)

    suspend fun insertTemporaryDiary(diary: Diary)

    suspend fun duplicateDiary(diary: Diary)

    suspend fun deleteTemporaryDiaryByOriginId(originDiaryId: Int)

    suspend fun insertAllDiaries(diaries: List<Diary>)

    suspend fun updateDiaryWithPhotos(diary: Diary)

    suspend fun updateDiaries(diaries: List<Diary>)

    suspend fun deleteDiary(diary: Diary)

    suspend fun deleteDiaryById(seq: Int)

    suspend fun deleteAllDiaries()

    suspend fun clearSelectedStatus()

    fun getPhotoUris(): Flow<List<PhotoUriEntity>>

    fun findParentDiariesOf(sequence: Int): Flow<List<Diary>>

    suspend fun findOldestDiary(): Diary?
}
