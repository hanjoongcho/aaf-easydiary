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
        override fun getAllDiaries(): Flow<List<DiaryEntity>> = diaryDao.getAllDiaries()

        override suspend fun getDiariesWithPhotos(): List<DiaryWithPhotos> = diaryDao.getDiariesWithPhotos()

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

        override suspend fun deleteDiary(diary: DiaryEntity) = diaryDao.deleteDiary(diary)

        override suspend fun deleteDiaryById(seq: Int) = diaryDao.deleteDiaryById(seq)

        override suspend fun deleteAllDiaries() = diaryDao.deleteAllDiaries()

        override fun getPhotoUris(): Flow<List<PhotoUriEntity>> = diaryDao.getPhotoUris()
    }