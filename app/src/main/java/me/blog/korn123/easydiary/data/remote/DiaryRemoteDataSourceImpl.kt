package me.blog.korn123.easydiary.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.blog.korn123.easydiary.data.repository.DiaryDataSource
import me.blog.korn123.easydiary.data.local.models.DiaryEntity
import javax.inject.Inject

class DiaryRemoteDataSourceImpl @Inject constructor() : DiaryDataSource {
    override fun getAllDiaries(): Flow<List<DiaryEntity>> = flowOf(emptyList())

    override suspend fun getDiaryById(seq: Int): DiaryEntity? = null

    override suspend fun insertDiary(diary: DiaryEntity) {

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
}
