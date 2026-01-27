package com.example.jitterpay.di

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * WorkManager dependency injection module
 *
 * Provides WorkManager configuration to enable Hilt dependency injection
 * in custom workers like RecurringTransactionWorker.
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    /**
     * Provide WorkManager configuration with Hilt worker factory
     *
     * This allows Hilt to inject dependencies into Workers.
     * The configuration is used by JitterPayApplication to
     * provide WorkManager initialization.
     */
    @Provides
    @Singleton
    fun provideWorkManagerConfiguration(
        workerFactory: HiltWorkerFactory
    ): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
