package me.blog.korn123.easydiary.data.local.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.blog.korn123.easydiary.data.local.dao.ActionLogDao
import me.blog.korn123.easydiary.data.local.datasource.ActionLogLocalDataSourceImpl
import me.blog.korn123.easydiary.data.local.dao.AlarmDao
import me.blog.korn123.easydiary.data.local.datasource.AlarmLocalDataSourceImpl
import me.blog.korn123.easydiary.data.local.AppDatabase
import me.blog.korn123.easydiary.data.local.dao.DDayDao
import me.blog.korn123.easydiary.data.local.datasource.DDayLocalDataSourceImpl
import me.blog.korn123.easydiary.data.local.dao.DiaryDao
import me.blog.korn123.easydiary.data.local.datasource.DiaryLocalDataSourceImpl
import me.blog.korn123.easydiary.data.local.MIGRATION_1_2
import me.blog.korn123.easydiary.data.local.MIGRATION_2_3
import me.blog.korn123.easydiary.data.local.MIGRATION_3_4
import me.blog.korn123.easydiary.data.local.MIGRATION_4_5
import me.blog.korn123.easydiary.data.datasource.ActionLogDataSource
import me.blog.korn123.easydiary.data.datasource.AlarmDataSource
import me.blog.korn123.easydiary.data.datasource.DDayDataSource
import me.blog.korn123.easydiary.data.datasource.DiaryDataSource
import me.blog.korn123.easydiary.data.datasource.LocalDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {
    @Binds
    @Singleton
    @LocalDataSource
    abstract fun bindDiaryLocalDataSource(
        diaryLocalDataSourceImpl: DiaryLocalDataSourceImpl,
    ): DiaryDataSource

    @Binds
    @Singleton
    @LocalDataSource
    abstract fun bindAlarmLocalDataSource(
        alarmLocalDataSourceImpl: AlarmLocalDataSourceImpl,
    ): AlarmDataSource

    @Binds
    @Singleton
    @LocalDataSource
    abstract fun bindActionLogLocalDataSource(
        actionLogLocalDataSourceImpl: ActionLogLocalDataSourceImpl,
    ): ActionLogDataSource

    @Binds
    @Singleton
    @LocalDataSource
    abstract fun bindDDayLocalDataSource(
        dDayLocalDataSourceImpl: DDayLocalDataSourceImpl,
    ): DDayDataSource

    companion object {
        @Provides
        @Singleton
        fun provideAppDatabase(
            @ApplicationContext context: Context,
        ): AppDatabase =
            Room
                .databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "easy_diary.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()

        @Provides
        fun provideDiaryDao(database: AppDatabase): DiaryDao = database.diaryDao()

        @Provides
        fun provideAlarmDao(database: AppDatabase): AlarmDao = database.alarmDao()

        @Provides
        fun provideActionLogDao(database: AppDatabase): ActionLogDao = database.actionLogDao()

        @Provides
        fun provideDDayDao(database: AppDatabase): DDayDao = database.dDayDao()
    }
}
