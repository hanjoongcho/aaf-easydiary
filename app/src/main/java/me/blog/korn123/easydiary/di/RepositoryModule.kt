package me.blog.korn123.easydiary.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.blog.korn123.easydiary.data.repository.ActionLogRepositoryImpl
import me.blog.korn123.easydiary.data.repository.AlarmRepositoryImpl
import me.blog.korn123.easydiary.data.repository.DDayRepositoryImpl
import me.blog.korn123.easydiary.data.repository.DiaryRepositoryImpl
import me.blog.korn123.easydiary.domain.repository.ActionLogRepository
import me.blog.korn123.easydiary.domain.repository.AlarmRepository
import me.blog.korn123.easydiary.domain.repository.DDayRepository
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(
        diaryRepositoryImpl: DiaryRepositoryImpl
    ): DiaryRepository

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        alarmRepositoryImpl: AlarmRepositoryImpl
    ): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindDDayRepository(
        dDayRepositoryImpl: DDayRepositoryImpl
    ): DDayRepository

    @Binds
    @Singleton
    abstract fun bindActionLogRepository(
        actionLogRepositoryImpl: ActionLogRepositoryImpl
    ): ActionLogRepository
}
