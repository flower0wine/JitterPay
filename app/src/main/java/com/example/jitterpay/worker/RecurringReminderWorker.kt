package com.example.jitterpay.worker

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jitterpay.data.repository.RecurringRepository
import com.example.jitterpay.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker that sends notifications for recurring transactions with due reminders.
 *
 * This worker runs periodically (e.g., every hour) to check for recurring
 * transactions that have reminder notifications due. For each transaction,
 * it sends a notification using the NotificationHelper.
 *
 * Reminder logic:
 * - A reminder is due when: currentTime >= nextExecutionDate - (reminderDaysBefore * 24 hours)
 * - Uses SharedPreferences to track sent reminders per transaction cycle
 * - Automatically allows re-sending when next execution date advances
 */
@HiltWorker
class RecurringReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringRepository: RecurringRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "RecurringReminderWorker"
        const val UNIQUE_WORK_NAME = "recurring_reminder_check"
        const val REPEAT_INTERVAL_HOURS = 1L // Check every hour

        // SharedPreferences file name for tracking sent reminders
        const val PREFS_NAME = "recurring_reminders"
        const val KEY_REMINDER_SENT_PREFIX = "reminder_sent_"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun doWork(): Result {
        return try {

            // Check if notifications are enabled
            if (!notificationHelper.areNotificationsEnabled()) {
                return Result.success()
            }

            // Get all recurring transactions that need reminders
            val currentTime = System.currentTimeMillis()
            val transactionsNeedingReminder =
                recurringRepository.getRecurringTransactionsNeedingReminder(currentTime)

            if (transactionsNeedingReminder.isEmpty()) {
                return Result.success()
            }

            // Send notifications for each transaction
            transactionsNeedingReminder.forEach { recurring ->
                val reminderKey = getReminderKey(recurring.id, recurring.nextExecutionDateMillis)

                // Check if reminder already sent for this cycle
                if (prefs.getBoolean(reminderKey, false)) {
                    // Reminder already sent, skip
                    Log.d(TAG, "Reminder already sent for recurring ID: ${recurring.id}")
                    return@forEach
                }

                try {
                    val amountFormatted = recurring.getFormattedAmount()

                    // Show notification
                    notificationHelper.showRecurringReminder(
                        recurringId = recurring.id,
                        title = recurring.title,
                        amount = amountFormatted,
                        daysBefore = recurring.reminderDaysBefore,
                        nextExecutionDate = recurring.nextExecutionDateMillis
                    )

                    // Mark that reminder was sent for this cycle
                    prefs.edit().putBoolean(reminderKey, true).apply()
                    Log.d(TAG, "Reminder sent for recurring ID: ${recurring.id}")

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send reminder for recurring ID: ${recurring.id}", e)
                    // Continue processing other transactions
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing recurring reminders", e)
            // Return retry result to let WorkManager handle retries
            Result.retry()
        }
    }

    /**
     * Generate a unique key for tracking reminder sent status.
     *
     * Key includes nextExecutionDateMillis so that when the transaction executes
     * and nextExecutionDate advances, a new key will be generated automatically.
     */
    private fun getReminderKey(recurringId: Long, nextExecutionDateMillis: Long): String {
        return "$KEY_REMINDER_SENT_PREFIX${recurringId}_$nextExecutionDateMillis"
    }

    /**
     * Clear reminder sent status for a recurring transaction.
     * Called when a transaction executes so reminders can be sent for the new cycle.
     */
    fun clearReminderStatus(recurringId: Long, nextExecutionDateMillis: Long) {
        val reminderKey = getReminderKey(recurringId, nextExecutionDateMillis)
        prefs.edit().remove(reminderKey).apply()
    }
}
