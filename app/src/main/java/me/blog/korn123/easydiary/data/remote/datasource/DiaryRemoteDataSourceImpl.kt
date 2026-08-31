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
        override fun getAllDiaries(
            query: String?,
            isSensitive: Boolean,
            startTimeMillis: Long,
            endTimeMillis: Long,
            symbolSequence: Int,
        ): Flow<List<DiaryEntity>> = flowOf(emptyList())

        override fun getDiariesWithPhotos(
            query: String?,
            isSensitive: Boolean,
            startTimeMillis: Long,
            endTimeMillis: Long,
            symbolSequence: Int,
        ): Flow<List<DiaryWithPhotos>> = flowOf(emptyList())

        override fun getDiaryWithPhotosById(id: Int): Flow<DiaryWithPhotos?> = flowOf(null)

        override fun getDiaryWithPhotosByPhotoUri(photoUriString: String): Flow<DiaryWithPhotos?> = flowOf(null)

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

        override suspend fun updateDiaryWithPhotos(
            diary: DiaryEntity,
            photoUris: List<PhotoUriEntity>,
        ) {
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

        override fun findParentDiariesOf(sequence: Int): Flow<List<DiaryEntity>> = flowOf(emptyList())
    }
