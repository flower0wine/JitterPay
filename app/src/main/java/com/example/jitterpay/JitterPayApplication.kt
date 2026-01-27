package com.example.jitterpay

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main application class for JitterPay
 *
 * Initializes Hilt dependency injection and sets up background services.
 * Provides WorkManager configuration to enable dependency injection in workers.
 */
@HiltAndroidApp
class JitterPayApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var wmConfiguration: dagger.Lazy<Configuration>

    @Inject
    lateinit var recurringTransactionScheduler: dagger.Lazy<com.example.jitterpay.scheduler.RecurringTransactionScheduler>

    /**
     * Provide WorkManager configuration
     *
     * This enables Hilt to inject dependencies into Workers.
     */
    override val workManagerConfiguration: Configuration
        get() = wmConfiguration.get()

    override fun onCreate() {
        super.onCreate()

        // Initialize WorkManager with our configuration
        WorkManager.initialize(this, workManagerConfiguration)

        // Initialize recurring transaction scheduler
        // This ensures that periodic checks for recurring transactions
        // are scheduled and persist across app restarts
        try {
            recurringTransactionScheduler.get().scheduleRecurringTransactionChecks()
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e(
                "JitterPayApp",
                "Failed to initialize recurring transaction scheduler",
                e
            )
        }
    }
}

