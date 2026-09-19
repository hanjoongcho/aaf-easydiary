package me.blog.korn123.easydiary.domain.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.domain.model.Diary

/*
 * =====================================================================================
 *  Data Layer Method Naming Convention
 * =====================================================================================
 *
 *  CRUD         | Dao                  | LocalDataSource  | RemoteDataSource | Repository       | ViewModel
 *  -------------+----------------------+------------------+------------------+------------------+----------
 *  Create       | insert               | insertX          | createX          | addX             | addX
 *  Read (one)   | getById/observeById  | getX/observeX    | fetchX           | getX/observeX    | loadX
 *  Read (list)  | getAll/observeAll    | getXs/observeXs  | fetchXs          | getXs/observeXs  | loadXs
 *  Update       | update               | updateX          | updateX          | updateX          | updateX
 *  Delete       | delete/deleteById    | deleteX          | deleteX          | deleteX          | deleteX
 *  Upsert       | upsert               | upsertX          | (putX)           | saveX            | saveX
 *  Sync         | -                    | -                | -                | refreshX         | refresh
 *
 *  Rules
 *  -----
 *  1. get     : suspend one-shot read from local/cache.
 *  2. observe : any function returning Flow. Always use the observe prefix.
 *  3. fetch   : network read. RemoteDataSource only. Repository never exposes fetch.
 *  4. refreshX: Remote.fetchX -> Local.upsertX. Repository only.
 *  5. saveX   : caller doesn't care insert vs update; the branch lives in Repository.
 *  6. ViewModel: no get*. Use load/refresh for state updates, onXClicked for UI events.
 *
 *  Prefix -> layer hint
 *  --------------------
 *  insert / getById / observeAll -> Dao or LocalDataSource
 *  create / fetch                -> RemoteDataSource
 *  add / save / refresh          -> Repository
 *  load / onXClicked             -> ViewModel
 * =====================================================================================
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

    fun getDiaryWithPhotosById(id: Long): Flow<Diary?>

    fun getDiaryWithPhotosByPhotoUri(photoUriString: String): Flow<Diary?>

    suspend fun getDiariesByDateString(
        dateString: String,
        isAsc: Boolean = false,
    ): List<Diary>

    suspend fun getDiariesByDateRange(
        startDate: String,
        endDate: String,
    ): List<Diary>

    suspend fun getDiaryById(seq: Long): Diary?

    suspend fun insertDiary(diary: Diary)

    suspend fun insertTemporaryDiary(diary: Diary)

    suspend fun duplicateDiary(diary: Diary)

    suspend fun deleteTemporaryDiaryByOriginId(originDiaryId: Long)

    suspend fun insertAllDiaries(diaries: List<Diary>)

    suspend fun updateDiaryWithPhotos(diary: Diary)

    suspend fun updateDiaries(diaries: List<Diary>)

    suspend fun deleteDiary(diary: Diary)

    suspend fun deleteDiaryById(seq: Long)

    suspend fun deleteAllDiaries()

    suspend fun clearSelectedStatus()

    fun getPhotoUris(): Flow<List<PhotoUriEntity>>

    fun findParentDiariesOf(sequence: Long): Flow<List<Diary>>

    suspend fun findOldestDiary(): Diary?
}
