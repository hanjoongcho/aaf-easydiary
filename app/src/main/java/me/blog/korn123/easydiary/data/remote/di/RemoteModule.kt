package me.blog.korn123.easydiary.data.remote.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.blog.korn123.easydiary.data.remote.DiaryRemoteDataSourceImpl
import me.blog.korn123.easydiary.data.repository.DiaryDataSource
import me.blog.korn123.easydiary.data.repository.RemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteModule {

    @Binds
    @Singleton
    @RemoteDataSource
    abstract fun bindDiaryRemoteDataSource(
        diaryRemoteDataSourceImpl: DiaryRemoteDataSourceImpl
    ): DiaryDataSource
}
