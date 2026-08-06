package me.blog.korn123.easydiary.data.remote.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.blog.korn123.easydiary.data.remote.datasource.ActionLogRemoteDataSourceImpl
import me.blog.korn123.easydiary.data.remote.datasource.AlarmRemoteDataSourceImpl
import me.blog.korn123.easydiary.data.remote.datasource.DDayRemoteDataSourceImpl
import me.blog.korn123.easydiary.data.remote.datasource.DiaryRemoteDataSourceImpl
import me.blog.korn123.easydiary.data.datasource.ActionLogDataSource
import me.blog.korn123.easydiary.data.datasource.AlarmDataSource
import me.blog.korn123.easydiary.data.datasource.DDayDataSource
import me.blog.korn123.easydiary.data.datasource.DiaryDataSource
import me.blog.korn123.easydiary.data.datasource.RemoteDataSource
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

    @Binds
    @Singleton
    @RemoteDataSource
    abstract fun bindAlarmRemoteDataSource(
        alarmRemoteDataSourceImpl: AlarmRemoteDataSourceImpl
    ): AlarmDataSource

    @Binds
    @Singleton
    @RemoteDataSource
    abstract fun bindActionLogRemoteDataSource(
        actionLogRemoteDataSourceImpl: ActionLogRemoteDataSourceImpl
    ): ActionLogDataSource

    @Binds
    @Singleton
    @RemoteDataSource
    abstract fun bindDDayRemoteDataSource(
        dDayRemoteDataSourceImpl: DDayRemoteDataSourceImpl
    ): DDayDataSource
}
