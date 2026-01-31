package com.example.jitterpay.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.RecurringRepository
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker that executes due recurring transactions
 *
 * This worker runs periodically to check for recurring transactions whose
 * nextExecutionDateMillis has passed. For each due transaction, it creates
 * an actual TransactionEntity record and advances the next execution date.
 *
 * Additionally, this worker cancels old reminder notifications when
 * the transaction executes and the next execution date advances.
 */
@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringRepository: RecurringRepository,
    private val transactionRepository: TransactionRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "RecurringTransactionWorker"
        const val UNIQUE_WORK_NAME = "recurring_transaction_check"
        const val REPEAT_INTERVAL_MINUTES = 30L
    }

    override suspend fun doWork(): Result {
        return try {

            // Get all recurring transactions that are due
            val dueTransactions = recurringRepository.getDueRecurring()

            if (dueTransactions.isEmpty()) {
                return Result.success()
            }


            // Create actual transaction records for each due recurring transaction
            dueTransactions.forEach { recurring ->
                try {
                    val transactionType = TransactionType.valueOf(recurring.type)
                    val description = "Recurring: ${recurring.title}"

                    // Create a new transaction record
                    transactionRepository.addTransaction(
                        type = transactionType,
                        amountCents = recurring.amountCents,
                        category = recurring.category,
                        description = description,
                        dateMillis = recurring.nextExecutionDateMillis
                    )


                    // Cancel old reminder notification since transaction has executed
                    if (recurring.reminderEnabled) {
                        notificationHelper.cancelRecurringReminder(recurring.id)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create transaction for recurring ID: ${recurring.id}", e)
                    // Continue processing other transactions even if one fails
                }
            }

            // Update next execution dates for all processed transactions
            recurringRepository.executeAndAdvance(dueTransactions)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing recurring transactions", e)
            // Return retry result to let WorkManager handle retries
            Result.retry()
        }
    }
}
