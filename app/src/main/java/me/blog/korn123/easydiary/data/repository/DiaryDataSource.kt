package me.blog.korn123.easydiary.data.repository

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.local.models.DiaryEntity
import me.blog.korn123.easydiary.data.local.models.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos
import javax.inject.Qualifier

interface DiaryDataSource {
    fun getAllDiaries(): Flow<List<DiaryEntity>>
    suspend fun getDiariesWithPhotos(): List<DiaryWithPhotos>
    suspend fun getDiaryById(seq: Int): DiaryEntity?
    suspend fun insertDiary(diary: DiaryEntity)
    suspend fun insertDiaryWithPhotos(diary: DiaryEntity, photoUris: List<PhotoUriEntity>)
    suspend fun updateDiary(diary: DiaryEntity)
    suspend fun deleteDiary(diary: DiaryEntity)
    suspend fun deleteDiaryById(seq: Int)
    suspend fun deleteAllDiaries()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteDataSource
