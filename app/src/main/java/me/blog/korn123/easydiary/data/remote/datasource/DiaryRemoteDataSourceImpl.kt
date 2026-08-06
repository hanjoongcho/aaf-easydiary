package me.blog.korn123.easydiary.data.remote.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.blog.korn123.easydiary.data.datasource.DiaryDataSource
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos
import javax.inject.Inject

class DiaryRemoteDataSourceImpl
    @Inject
    constructor() : DiaryDataSource {
        override fun getAllDiaries(): Flow<List<DiaryEntity>> = flowOf(emptyList())

        override suspend fun getDiariesWithPhotos(): List<DiaryWithPhotos> = emptyList()

        override suspend fun getDiaryById(seq: Int): DiaryEntity? = null

        override suspend fun insertDiary(diary: DiaryEntity) {
            // Mock implementation
        }

        override suspend fun insertDiaryWithPhotos(
            diary: DiaryEntity,
            photoUris: List<PhotoUriEntity>,
        ) {
            // Mock implementation
        }

        override suspend fun insertDiariesWithPhotos(
            diariesWithPhotos: List<Pair<DiaryEntity, List<PhotoUriEntity>>>,
        ) {
            // Mock implementation
        }

        override suspend fun updateDiary(diary: DiaryEntity) {
            // Mock implementation
        }

        override suspend fun deleteDiary(diary: DiaryEntity) {
            // Mock implementation
        }

        override suspend fun deleteDiaryById(seq: Int) {
            // Mock implementation
        }

        override suspend fun deleteAllDiaries() {
            // Mock implementation
        }

        override fun getPhotoUris(): Flow<List<PhotoUriEntity>> = flowOf(emptyList())
    }