package me.blog.korn123.easydiary.data.local.datasource

import kotlinx.coroutines.flow.Flow
import me.blog.korn123.easydiary.data.datasource.DiaryDataSource
import me.blog.korn123.easydiary.data.local.dao.DiaryDao
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos
import javax.inject.Inject

class DiaryLocalDataSourceImpl
    @Inject
    constructor(
        private val diaryDao: DiaryDao,
    ) : DiaryDataSource {
        override fun getAllDiaries(
            query: String?,
            isSensitive: Boolean,
            startTimeMillis: Long,
            endTimeMillis: Long,
            symbolSequence: Int,
        ): Flow<List<DiaryEntity>> = diaryDao.getAllDiaries(query, isSensitive, startTimeMillis, endTimeMillis, symbolSequence)

        override fun getDiariesWithPhotos(
            query: String?,
            isSensitive: Boolean,
            startTimeMillis: Long,
            endTimeMillis: Long,
            symbolSequence: Int,
        ): Flow<List<DiaryWithPhotos>> = diaryDao.getDiariesWithPhotos(query, isSensitive, startTimeMillis, endTimeMillis, symbolSequence)

        override fun getDiaryWithPhotosById(id: Int): Flow<DiaryWithPhotos?> = diaryDao.getDiaryWithPhotosById(id)

        override fun getDiaryWithPhotosByPhotoUri(photoUriString: String): Flow<DiaryWithPhotos?> = diaryDao.getDiaryWithPhotosByPhotoUri(photoUriString)

        override fun getDiariesWithPhotosByDateString(
            dateString: String,
            isAsc: Boolean,
        ): Flow<List<DiaryWithPhotos>> = diaryDao.getDiariesWithPhotosByDateString(dateString, isAsc)

        override suspend fun getDiaryById(seq: Int): DiaryEntity? = diaryDao.getDiaryById(seq)

        override suspend fun insertDiary(diary: DiaryEntity) = diaryDao.insertDiary(diary).let { }

        override suspend fun insertDiaryWithPhotos(
            diary: DiaryEntity,
            photoUris: List<PhotoUriEntity>,
        ) = diaryDao.insertDiaryWithPhotos(diary, photoUris)

        override suspend fun insertDiariesWithPhotos(
            diariesWithPhotos: List<Pair<DiaryEntity, List<PhotoUriEntity>>>,
        ) = diaryDao.insertDiariesWithPhotos(diariesWithPhotos)

        override suspend fun updateDiary(diary: DiaryEntity) = diaryDao.updateDiary(diary)

        override suspend fun updateDiaryWithPhotos(
            diary: DiaryEntity,
            photoUris: List<PhotoUriEntity>,
        ) = diaryDao.updateDiaryWithPhotos(diary, photoUris)

        override suspend fun deleteDiary(diary: DiaryEntity) = diaryDao.deleteDiary(diary)

        override suspend fun deleteDiaryById(seq: Int) = diaryDao.deleteDiaryById(seq)

        override suspend fun deleteTemporaryDiaryBy(originDiaryId: Int) = diaryDao.deleteTemporaryDiaryBy(originDiaryId)

        override suspend fun deleteAllDiaries() = diaryDao.deleteAllDiaries()

        override fun getPhotoUris(): Flow<List<PhotoUriEntity>> = diaryDao.getPhotoUris()

        override fun findParentDiariesOf(sequence: Int): Flow<List<DiaryEntity>> = diaryDao.findParentDiariesOf(sequence)
    }
