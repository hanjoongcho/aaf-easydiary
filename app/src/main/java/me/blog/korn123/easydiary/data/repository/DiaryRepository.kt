package me.blog.korn123.easydiary.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.data.local.mapper.toEntity
import me.blog.korn123.easydiary.domain.model.Diary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    @LocalDataSource private val localDataSource: DiaryDataSource,
    @RemoteDataSource private val remoteDataSource: DiaryDataSource
) {
    fun getAllDiaries(): Flow<List<Diary>> =
        localDataSource.getAllDiaries().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getDiaryById(seq: Int): Diary? = localDataSource.getDiaryById(seq)?.toDomain()

    suspend fun insertDiary(diary: Diary) {
        val entity = diary.toEntity()
        localDataSource.insertDiary(entity)
        remoteDataSource.insertDiary(entity)
    }

    suspend fun updateDiary(diary: Diary) {
        val entity = diary.toEntity()
        localDataSource.updateDiary(entity)
        remoteDataSource.updateDiary(entity)
    }

    suspend fun deleteDiary(diary: Diary) {
        val entity = diary.toEntity()
        localDataSource.deleteDiary(entity)
        remoteDataSource.deleteDiary(entity)
    }

    suspend fun deleteDiaryById(seq: Int) {
        localDataSource.deleteDiaryById(seq)
        remoteDataSource.deleteDiaryById(seq)
    }

    suspend fun deleteAllDiaries() {
        localDataSource.deleteAllDiaries()
        remoteDataSource.deleteAllDiaries()
    }
}
