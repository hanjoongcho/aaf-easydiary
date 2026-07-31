package me.blog.korn123.easydiary.data.local.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.blog.korn123.easydiary.data.local.AppDatabase
import me.blog.korn123.easydiary.data.local.DiaryDao
import me.blog.korn123.easydiary.data.local.DiaryLocalDataSourceImpl
import me.blog.korn123.easydiary.data.local.MIGRATION_1_2
import me.blog.korn123.easydiary.data.repository.DiaryDataSource
import me.blog.korn123.easydiary.data.repository.LocalDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    @LocalDataSource
    abstract fun bindDiaryLocalDataSource(
        diaryLocalDataSourceImpl: DiaryLocalDataSourceImpl
    ): DiaryDataSource

    companion object {
        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "easy_diary.db"
            ).addMigrations(MIGRATION_1_2)
                .build()
        }

        @Provides
        fun provideDiaryDao(database: AppDatabase): DiaryDao {
            return database.diaryDao()
        }
    }
}
