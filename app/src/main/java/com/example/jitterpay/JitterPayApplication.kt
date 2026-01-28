package com.example.jitterpay

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.jitterpay.scheduler.RecurringReminderScheduler
import com.example.jitterpay.scheduler.RecurringTransactionScheduler
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
    lateinit var recurringTransactionScheduler: dagger.Lazy<RecurringTransactionScheduler>

    @Inject
    lateinit var recurringReminderScheduler: dagger.Lazy<RecurringReminderScheduler>

    /**
     * Provide WorkManager configuration
     *
     * This enables Hilt to inject dependencies into Workers.
     */
    override val workManagerConfiguration: Configuration
        get() = wmConfiguration.get()

    override fun onCreate() {
        super.onCreate()

        // WorkManager is automatically initialized by Configuration.Provider interface
        // No need to call WorkManager.initialize() manually

        // Initialize recurring transaction scheduler
        // This ensures that periodic checks for recurring transactions
        // are scheduled and persist across app restarts
        try {
            recurringTransactionScheduler.get().scheduleRecurringTransactionChecks()
        } catch (e: Exception) {
            // Log error but don't crash app
            Log.e(
                "JitterPayApp",
                "Failed to initialize recurring transaction scheduler",
                e
            )
        }

        // Initialize recurring reminder scheduler
        // This ensures that periodic checks for recurring transaction reminders
        // are scheduled and persist across app restarts
        try {
            recurringReminderScheduler.get().scheduleReminderChecks()
        } catch (e: Exception) {
            // Log error but don't crash app
            Log.e(
                "JitterPayApp",
                "Failed to initialize recurring reminder scheduler",
                e
            )
        }
    }
}

