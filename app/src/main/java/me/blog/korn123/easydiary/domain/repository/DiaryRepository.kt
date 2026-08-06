package me.blog.korn123.easydiary.domain.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.domain.model.Diary

interface DiaryRepository {
    fun getAllDiaries(): Flow<List<Diary>>
    suspend fun getDiariesWithPhotos(): List<Diary>
    suspend fun getDiaryById(seq: Int): Diary?
    suspend fun insertDiary(diary: Diary)
    suspend fun addAllDiaries(diaries: List<Diary>)
    suspend fun updateDiary(diary: Diary)
    suspend fun deleteDiary(diary: Diary)
    suspend fun deleteDiaryById(seq: Int)
    suspend fun deleteAllDiaries()
    fun getPhotoUris(): Flow<List<PhotoUriEntity>>
}
