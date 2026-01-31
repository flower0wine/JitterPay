package com.example.jitterpay

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
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
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        @Volatile
        private var preloadedSplashComposition: LottieComposition? = null

        fun getPreloadedSplashComposition(): LottieComposition? = preloadedSplashComposition
    }

    /**
     * Provide WorkManager configuration with Hilt worker factory
     *
     * This enables Hilt to inject dependencies into Workers.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Preload Lottie splash animation for faster splash screen display
        preloadedSplashComposition = LottieCompositionFactory
            .fromRawResSync(this, R.raw.splash_animation)
            .value
    }
}
