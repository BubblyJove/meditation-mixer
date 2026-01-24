package com.meditationmixer.core.audio.di

import com.meditationmixer.core.audio.engine.AudioEngine
import com.meditationmixer.core.audio.engine.AudioEngineImpl
import com.meditationmixer.core.audio.repository.AudioRepositoryImpl
import com.meditationmixer.core.audio.repository.SessionRepositoryImpl
import com.meditationmixer.core.domain.repository.AudioRepository
import com.meditationmixer.core.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {
    
    @Binds
    @Singleton
    abstract fun bindAudioEngine(impl: AudioEngineImpl): AudioEngine
    
    @Binds
    @Singleton
    abstract fun bindAudioRepository(impl: AudioRepositoryImpl): AudioRepository
    
    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
}
