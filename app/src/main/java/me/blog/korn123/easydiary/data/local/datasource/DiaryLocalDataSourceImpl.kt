package me.blog.korn123.easydiary.data.local.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.datasource.DiaryDataSource
import me.blog.korn123.easydiary.data.local.dao.DiaryDao
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos
import me.blog.korn123.easydiary.domain.model.Diary
import javax.inject.Inject

class DiaryLocalDataSourceImpl
    @Inject
    constructor(
        private val diaryDao: DiaryDao,
    ) : DiaryDataSource {
        override fun getDiariesWithPhotosFlow(
            query: String?,
            isSensitive: Boolean,
            startTimeMillis: Long,
            endTimeMillis: Long,
            symbolSequence: Int,
        ): Flow<List<DiaryWithPhotos>> =
            diaryDao.getDiariesWithPhotosFlow(
                query,
                isSensitive,
                startTimeMillis,
                endTimeMillis,
                symbolSequence,
            )

        override suspend fun getDiariesWithPhotos(
            query: String?,
            isSensitive: Boolean,
            startTimeMillis: Long,
            endTimeMillis: Long,
            symbolSequence: Int,
        ): List<DiaryWithPhotos> =
            diaryDao.getDiariesWithPhotos(
                query,
                isSensitive,
                startTimeMillis,
                endTimeMillis,
                symbolSequence,
            )

        override fun getDiaryWithPhotosById(id: Long): Flow<DiaryWithPhotos?> = diaryDao.getDiaryWithPhotosById(id)

        override fun getDiaryWithPhotosByPhotoUri(photoUriString: String): Flow<DiaryWithPhotos?> = diaryDao.getDiaryWithPhotosByPhotoUri(photoUriString)

        override suspend fun getDiariesByDateString(
            dateString: String,
            isAsc: Boolean,
        ): List<DiaryEntity> = diaryDao.getDiariesByDateString(dateString, isAsc)

        override suspend fun getDiariesByDateRange(
            startDate: String,
            endDate: String,
        ): List<DiaryEntity> = diaryDao.getDiariesByDateRange(startDate, endDate)

        override suspend fun getDiaryById(seq: Long): DiaryEntity? = diaryDao.getDiaryById(seq)

        override suspend fun insertDiary(diary: DiaryEntity) {
            diaryDao.insertDiary(diary)
        }

        override suspend fun insertDiaryWithPhotos(
            diary: DiaryEntity,
            photoUris: List<PhotoUriEntity>,
        ): Long = diaryDao.insertDiaryWithPhotos(diary, photoUris)

        override suspend fun insertDiariesWithPhotos(diariesWithPhotos: List<Pair<DiaryEntity, List<PhotoUriEntity>>>) {
            diaryDao.insertDiariesWithPhotos(diariesWithPhotos)
        }

        override suspend fun updateDiary(diary: DiaryEntity) {
            diaryDao.updateDiary(diary)
        }

        override suspend fun updateDiaries(diaries: List<DiaryEntity>) {
            diaryDao.updateDiaries(diaries)
        }

        override suspend fun updateDiaryWithPhotos(
            diary: DiaryEntity,
            photoUris: List<PhotoUriEntity>,
        ) {
            diaryDao.updateDiaryWithPhotos(diary, photoUris)
        }

        override suspend fun deleteDiary(diary: DiaryEntity) {
            diaryDao.deleteDiary(diary)
        }

        override suspend fun deleteDiaryById(seq: Long) {
            diaryDao.deleteDiaryById(seq)
        }

        override suspend fun deleteTemporaryDiaryBy(originDiaryId: Long) {
            diaryDao.deleteTemporaryDiaryBy(originDiaryId)
        }

        override suspend fun deleteAllDiaries() {
            diaryDao.deleteAllDiaries()
        }

        override suspend fun clearSelectedStatus() {
            diaryDao.clearSelectedStatus()
        }

        override fun getPhotoUris(): Flow<List<PhotoUriEntity>> = diaryDao.getPhotoUris()

        override fun findParentDiariesOf(sequence: Long): Flow<List<DiaryEntity>> = diaryDao.findParentDiariesOf(sequence)

        override suspend fun findOldestDiary(): Diary? = diaryDao.findOldestDiary()?.toDomain()
    }
