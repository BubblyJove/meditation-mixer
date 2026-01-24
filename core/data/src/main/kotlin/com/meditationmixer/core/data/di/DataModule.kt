package com.meditationmixer.core.data.di

import android.content.Context
import androidx.room.Room
import com.meditationmixer.core.data.database.DatabaseCallback
import com.meditationmixer.core.data.database.MeditationDatabase
import com.meditationmixer.core.data.database.dao.PresetDao
import com.meditationmixer.core.data.database.dao.SessionHistoryDao
import com.meditationmixer.core.data.database.dao.StreakDao
import com.meditationmixer.core.data.repository.PresetRepositoryImpl
import com.meditationmixer.core.data.repository.SettingsRepositoryImpl
import com.meditationmixer.core.data.repository.StreakRepositoryImpl
import com.meditationmixer.core.domain.repository.PresetRepository
import com.meditationmixer.core.domain.repository.SettingsRepository
import com.meditationmixer.core.domain.repository.StreakRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MeditationDatabase {
        return Room.databaseBuilder(
            context,
            MeditationDatabase::class.java,
            MeditationDatabase.DATABASE_NAME
        )
            .addCallback(DatabaseCallback())
            .build()
    }
    
    @Provides
    fun providePresetDao(database: MeditationDatabase): PresetDao {
        return database.presetDao()
    }
    
    @Provides
    fun provideSessionHistoryDao(database: MeditationDatabase): SessionHistoryDao {
        return database.sessionHistoryDao()
    }
    
    @Provides
    fun provideStreakDao(database: MeditationDatabase): StreakDao {
        return database.streakDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindPresetRepository(impl: PresetRepositoryImpl): PresetRepository
    
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
    
    @Binds
    @Singleton
    abstract fun bindStreakRepository(impl: StreakRepositoryImpl): StreakRepository
}
