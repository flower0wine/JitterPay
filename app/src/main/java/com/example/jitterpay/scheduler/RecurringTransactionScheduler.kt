package com.example.jitterpay.scheduler

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jitterpay.worker.RecurringTransactionWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler for managing recurring transaction background work
 *
 * This class provides methods to schedule and cancel the periodic work
 * that checks for and executes due recurring transactions.
 * Uses WorkManager to ensure tasks run even when the app is closed.
 */
@Singleton
class RecurringTransactionScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "RecurringTxScheduler"
    }

    /**
     * Start scheduling recurring transaction checks
     *
     * Schedules a periodic worker that runs every 30 minutes to check for
     * and execute due recurring transactions. The task will persist across
     * app restarts and device reboots.
     *
     * Uses ExistingPeriodicWorkPolicy.UPDATE to replace any existing work
     * with the same unique name.
     */
    fun scheduleRecurringTransactionChecks() {
        val workManager = WorkManager.getInstance(context)

        // Create constraints for when the worker should run
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            // No battery or storage constraints - we want it to run reliably
            .build()

        // Build periodic work request
        val workRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            RecurringTransactionWorker.REPEAT_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(RecurringTransactionWorker.UNIQUE_WORK_NAME)
            .build()

        // Enqueue unique periodic work
        workManager.enqueueUniquePeriodicWork(
            RecurringTransactionWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Log.d(TAG, "Scheduled recurring transaction checks every ${RecurringTransactionWorker.REPEAT_INTERVAL_MINUTES} minutes")
    }

    /**
     * Stop scheduling recurring transaction checks
     *
     * Cancels all work associated with the recurring transaction worker.
     * This effectively disables automatic recurring transaction execution.
     */
    fun stopRecurringTransactionChecks() {
        val workManager = WorkManager.getInstance(context)

        // Cancel all work by unique name
        workManager.cancelUniqueWork(RecurringTransactionWorker.UNIQUE_WORK_NAME)

        Log.d(TAG, "Stopped recurring transaction checks")
    }

    /**
     * Schedule an immediate check for recurring transactions
     *
     * This can be used to trigger an immediate check when the user
     * toggles a recurring transaction on or creates a new one.
     *
     * Uses a one-time work request that will execute as soon as possible.
     */
    fun scheduleImmediateCheck() {
        val workManager = WorkManager.getInstance(context)

        // Create constraints for when the worker should run
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Build one-time work request with no initial delay
        val workRequest = OneTimeWorkRequestBuilder<RecurringTransactionWorker>()
            .setConstraints(constraints)
            .addTag(RecurringTransactionWorker.UNIQUE_WORK_NAME)
            .build()

        workManager.enqueue(workRequest)

        Log.d(TAG, "Scheduled immediate recurring transaction check")
    }

    /**
     * Check if recurring transaction work is currently scheduled
     *
     * @return true if there's work scheduled, false otherwise
     */
    fun isScheduled(): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(RecurringTransactionWorker.UNIQUE_WORK_NAME)

        return try {
            workInfos.get().any { !it.state.isFinished }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if work is scheduled", e)
            false
        }
    }
}
