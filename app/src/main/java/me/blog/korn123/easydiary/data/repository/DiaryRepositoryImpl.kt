package me.blog.korn123.easydiary.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.blog.korn123.easydiary.data.datasource.DiaryDataSource
import me.blog.korn123.easydiary.data.datasource.LocalDataSource
import me.blog.korn123.easydiary.data.datasource.RemoteDataSource
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.extensions.config
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @LocalDataSource private val localDataSource: DiaryDataSource,
    @RemoteDataSource private val remoteDataSource: DiaryDataSource
) : DiaryRepository {

    private val dataSource: DiaryDataSource
        get() = if (context.config.enableJetpackRoomDatabase) localDataSource else remoteDataSource

    override fun getAllDiaries(): Flow<List<Diary>> =
        dataSource.getAllDiaries().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getDiariesWithPhotos(): List<Diary> =
        dataSource.getDiariesWithPhotos().map { it.toDomain() }

    override suspend fun getDiaryById(seq: Int): Diary? = 
        dataSource.getDiaryById(seq)?.toDomain()

    override suspend fun insertDiary(diary: Diary) {
        val diaryEntity = diary.toEntity()
        val photoEntities = diary.photoUris.map { it.toEntity(diaryEntity.diaryId) }
        dataSource.insertDiaryWithPhotos(diaryEntity, photoEntities)
    }

    override suspend fun addAllDiaries(diaries: List<Diary>) {
        val diariesWithPhotos = diaries.map { diary ->
            val diaryEntity = diary.toEntity()
            val photoEntities = diary.photoUris.map { it.toEntity(diaryEntity.diaryId) }
            Pair(diaryEntity, photoEntities)
        }
        dataSource.insertDiariesWithPhotos(diariesWithPhotos)
    }

    override suspend fun updateDiary(diary: Diary) {
        val entity = diary.toEntity()
        dataSource.updateDiary(entity)
    }

    override suspend fun deleteDiary(diary: Diary) {
        val entity = diary.toEntity()
        dataSource.deleteDiary(entity)
    }

    override suspend fun deleteDiaryById(seq: Int) {
        dataSource.deleteDiaryById(seq)
    }

    override suspend fun deleteAllDiaries() {
        dataSource.deleteAllDiaries()
    }

    override fun getPhotoUris(): Flow<List<PhotoUriEntity>> =
        dataSource.getPhotoUris()
}
