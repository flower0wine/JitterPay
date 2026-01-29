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
import com.example.jitterpay.worker.RecurringReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler for managing recurring transaction reminder background work
 *
 * This class provides methods to schedule and cancel the periodic work
 * that checks for and sends notifications for due recurring transaction reminders.
 * Uses WorkManager to ensure tasks run even when the app is closed.
 */
@Singleton
class RecurringReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "RecurringReminderScheduler"
    }

    /**
     * Start scheduling recurring transaction reminder checks
     *
     * Schedules a periodic worker that runs every hour to check for
     * and send notifications for due recurring transaction reminders.
     * The task will persist across app restarts and device reboots.
     *
     * Uses ExistingPeriodicWorkPolicy.UPDATE to replace any existing work
     * with the same unique name.
     */
    fun scheduleReminderChecks() {
        val workManager = WorkManager.getInstance(context)

        // Create constraints for when the worker should run
        // We want minimal constraints to ensure reminders are sent reliably
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .build()

        // Build periodic work request
        val workRequest = PeriodicWorkRequestBuilder<RecurringReminderWorker>(
            RecurringReminderWorker.REPEAT_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(RecurringReminderWorker.UNIQUE_WORK_NAME)
            .build()

        // Enqueue unique periodic work
        workManager.enqueueUniquePeriodicWork(
            RecurringReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Log.d(TAG, "Scheduled recurring reminder checks every ${RecurringReminderWorker.REPEAT_INTERVAL_HOURS} hour(s)")
    }

    /**
     * Stop scheduling recurring transaction reminder checks
     *
     * Cancels all work associated with the recurring reminder worker.
     * This effectively disables automatic reminder notifications.
     */
    fun stopReminderChecks() {
        val workManager = WorkManager.getInstance(context)

        // Cancel all work by unique name
        workManager.cancelUniqueWork(RecurringReminderWorker.UNIQUE_WORK_NAME)

        Log.d(TAG, "Stopped recurring reminder checks")
    }

    /**
     * Schedule an immediate check for recurring transaction reminders
     *
     * This can be used to trigger an immediate check when the user
     * creates or edits a recurring transaction with reminders enabled.
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
        val workRequest = OneTimeWorkRequestBuilder<RecurringReminderWorker>()
            .setConstraints(constraints)
            .addTag(RecurringReminderWorker.UNIQUE_WORK_NAME)
            .build()

        workManager.enqueue(workRequest)

        Log.d(TAG, "Scheduled immediate recurring reminder check")
    }

    /**
     * Check if recurring reminder work is currently scheduled
     *
     * @return true if there's work scheduled, false otherwise
     */
    fun isScheduled(): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(RecurringReminderWorker.UNIQUE_WORK_NAME)

        return try {
            // Check if any work with this tag exists
            // For periodic work, this indicates the recurring reminder is set up
            workInfos.get().isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if reminder work is scheduled", e)
            false
        }
    }
}
