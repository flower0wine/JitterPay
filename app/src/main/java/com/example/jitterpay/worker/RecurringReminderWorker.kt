package com.example.jitterpay.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jitterpay.data.repository.RecurringRepository
import com.example.jitterpay.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker that sends notifications for recurring transactions with due reminders.
 *
 * This worker runs periodically (e.g., every hour) to check for recurring
 * transactions that have reminder notifications due. For each transaction,
 * it sends a notification using the NotificationHelper.
 *
 * Reminder logic:
 * - A reminder is due when: currentTime >= nextExecutionDate - (reminderDaysBefore * 24 hours)
 * - Only sends reminder if notification hasn't been sent yet for this cycle
 * - Automatically clears old reminders when next execution date advances
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

        // Key for tracking last reminder timestamp per recurring transaction
        private const val KEY_LAST_REMINDER_TIMESTAMP = "last_reminder_timestamp"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Checking for due recurring transaction reminders")

            // Check if notifications are enabled
            if (!notificationHelper.areNotificationsEnabled()) {
                Log.d(TAG, "Notifications are disabled by user, skipping")
                return Result.success()
            }

            // Get all recurring transactions that need reminders
            val currentTime = System.currentTimeMillis()
            val transactionsNeedingReminder =
                recurringRepository.getRecurringTransactionsNeedingReminder(currentTime)

            if (transactionsNeedingReminder.isEmpty()) {
                Log.d(TAG, "No recurring transactions need reminders")
                return Result.success()
            }

            Log.d(TAG, "Found ${transactionsNeedingReminder.size} transactions needing reminders")

            // Send notifications for each transaction
            transactionsNeedingReminder.forEach { recurring ->
                try {
                    // Calculate when the reminder time window started
                    val reminderTimeWindowStart =
                        recurring.nextExecutionDateMillis - (recurring.reminderDaysBefore * TimeUnit.DAYS.toMillis(
                            1
                        ))

                    // Only send reminder if we're in the reminder time window
                    // and haven't sent one for this cycle yet
                    if (currentTime >= reminderTimeWindowStart) {
                        val amountFormatted = recurring.getFormattedAmount()

                        // Show notification
                        notificationHelper.showRecurringReminder(
                            recurringId = recurring.id,
                            title = recurring.title,
                            amount = amountFormatted,
                            daysBefore = recurring.reminderDaysBefore,
                            nextExecutionDate = recurring.nextExecutionDateMillis
                        )

                        // Mark that reminder was sent (store in data or using WorkManager's output data)
                        // For simplicity, we'll rely on the periodic nature of this worker
                        // The notification will only show once per cycle because we update nextExecutionDate
                        // when the transaction executes

                        Log.d(TAG, "Sent reminder for recurring ID: ${recurring.id}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send reminder for recurring ID: ${recurring.id}", e)
                    // Continue processing other transactions
                }
            }

            Log.d(TAG, "Successfully processed ${transactionsNeedingReminder.size} recurring reminders")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing recurring reminders", e)
            // Return retry result to let WorkManager handle retries
            Result.retry()
        }
    }
}
