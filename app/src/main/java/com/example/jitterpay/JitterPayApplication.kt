package com.example.jitterpay

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    companion object {
        @Volatile
        private var preloadedSplashComposition: LottieComposition? = null

        fun getPreloadedSplashComposition(): LottieComposition? = preloadedSplashComposition
    }

    /**
     * Provide WorkManager configuration
     *
     * This enables Hilt to inject dependencies into Workers.
     */
    override val workManagerConfiguration: Configuration
        get() = wmConfiguration.get()

    override fun onCreate() {
        super.onCreate()

        // Preload Lottie splash animation for faster splash screen display
        preloadedSplashComposition = LottieCompositionFactory
            .fromRawResSync(this, R.raw.splash_animation)
            .value

        // WorkManager is automatically initialized by Configuration.Provider interface
        // No need to call WorkManager.initialize() manually

        // Initialize schedulers asynchronously to avoid blocking startup
        // Delay by 500ms to let UI complete first
        CoroutineScope(Dispatchers.IO).launch {
            try {
                kotlinx.coroutines.delay(500)

                // Initialize recurring transaction scheduler
                // This ensures that periodic checks for recurring transactions
                // are scheduled and persist across app restarts
                recurringTransactionScheduler.get().scheduleRecurringTransactionChecks()
                Log.d("JitterPayApp", "Recurring transaction scheduler initialized")
            } catch (e: Exception) {
                // Log error but don't crash app
                Log.e(
                    "JitterPayApp",
                    "Failed to initialize recurring transaction scheduler",
                    e
                )
            }

            try {
                // Initialize recurring reminder scheduler
                // This ensures that periodic checks for recurring transaction reminders
                // are scheduled and persist across app restarts
                recurringReminderScheduler.get().scheduleReminderChecks()
                Log.d("JitterPayApp", "Recurring reminder scheduler initialized")
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
}

