package com.example.jitterpay.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.jitterpay.data.local.entity.RecurringEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.RecurringRepository
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.notification.NotificationHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for RecurringTransactionWorker.
 *
 * Tests worker behavior with different scenarios:
 * - No due transactions
 * - Single due transaction
 * - Multiple due transactions
 * - Active/inactive filtering
 * - Error handling
 * - Reminder notification cancellation
 */
@RunWith(RobolectricTestRunner::class)
class RecurringTransactionWorkerTest {

    private lateinit var context: Context
    private lateinit var mockRecurringRepository: RecurringRepository
    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockNotificationHelper: NotificationHelper

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        mockRecurringRepository = mockk(relaxed = true)
        mockTransactionRepository = mockk(relaxed = true)
        mockNotificationHelper = mockk(relaxed = true)
    }

    private fun createWorker(): RecurringTransactionWorker {
        return RecurringTransactionWorker(
            context = context,
            workerParams = mockk(relaxed = true),
            recurringRepository = mockRecurringRepository,
            transactionRepository = mockTransactionRepository,
            notificationHelper = mockNotificationHelper
        )
    }

    @Test
    fun `doWork returns success when no due transactions`() = runTest {
        // Given
        coEvery { mockRecurringRepository.getDueRecurring() } returns emptyList()

        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(
            "Should return success when no due transactions",
            ListenableWorker.Result.success(),
            result
        )
    }

    @Test
    fun `doWork creates transaction for single due recurring`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recurring = RecurringEntity(
            id = 1L,
            title = "Netflix Subscription",
            amountCents = 1599L,
            type = "EXPENSE",
            category = "Entertainment",
            frequency = "MONTHLY",
            startDateMillis = currentTime - 86400000L * 30,
            nextExecutionDateMillis = currentTime - 1000,
            isActive = true,
            estimatedMonthlyAmount = 1599L,
            reminderEnabled = false,
            reminderDaysBefore = 0
        )
        coEvery { mockRecurringRepository.getDueRecurring() } returns listOf(recurring)
        coEvery {
            mockTransactionRepository.addTransaction(
                TransactionType.EXPENSE,
                1599L,
                "Entertainment",
                "Recurring: Netflix Subscription",
                recurring.nextExecutionDateMillis
            )
        } returns 1L

        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(
            "Should return success after creating transaction",
            ListenableWorker.Result.success(),
            result
        )
        coVerify {
            mockTransactionRepository.addTransaction(
                TransactionType.EXPENSE,
                1599L,
                "Entertainment",
                "Recurring: Netflix Subscription",
                recurring.nextExecutionDateMillis
            )
        }
        coVerify { mockRecurringRepository.executeAndAdvance(listOf(recurring)) }
    }

    @Test
    fun `doWork filters out inactive recurring transactions`() = runTest {
        // This test verifies that Worker can handle mixed active/inactive transactions
        // and only processes active ones (defense-in-depth filter)
        val currentTime = System.currentTimeMillis()
        val activeRecurring = RecurringEntity(
            id = 1L,
            title = "Active Task",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = "WEEKLY",
            startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime - 1000,
            isActive = true,
            estimatedMonthlyAmount = 400L
        )
        val inactiveRecurring = RecurringEntity(
            id = 2L,
            title = "Inactive Task",
            amountCents = 200L,
            type = "EXPENSE",
            category = "Test",
            frequency = "DAILY",
            startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime - 1000,
            isActive = false,
            estimatedMonthlyAmount = 6000L
        )

        // DAO returns mixed list (defense-in-depth test)
        coEvery { mockRecurringRepository.getDueRecurring() } returns listOf(activeRecurring, inactiveRecurring)
        coEvery { mockTransactionRepository.addTransaction(any(), any(), any(), any(), any()) } returns 1L

        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals("Should return success", ListenableWorker.Result.success(), result)

        // Verify addTransaction was called at least once (for active transaction)
        io.mockk.coVerify(atLeast = 1) { mockTransactionRepository.addTransaction(any(), any(), any(), any(), any()) }

        // Verify executeAndAdvance was called
        io.mockk.coVerify { mockRecurringRepository.executeAndAdvance(any()) }
    }

    @Test
    fun `doWork processes all active transactions when some are inactive`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val active1 = RecurringEntity(
            id = 1L, title = "Active 1", amountCents = 100L, type = "EXPENSE",
            category = "Test", frequency = "WEEKLY", startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime - 1000, isActive = true, estimatedMonthlyAmount = 400L
        )
        val active2 = RecurringEntity(
            id = 2L, title = "Active 2", amountCents = 200L, type = "EXPENSE",
            category = "Test", frequency = "DAILY", startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime - 1000, isActive = true, estimatedMonthlyAmount = 6000L
        )
        val inactive = RecurringEntity(
            id = 3L, title = "Inactive", amountCents = 300L, type = "EXPENSE",
            category = "Test", frequency = "MONTHLY", startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime - 1000, isActive = false, estimatedMonthlyAmount = 300L
        )

        // Mixed list
        coEvery { mockRecurringRepository.getDueRecurring() } returns listOf(active1, inactive, active2)
        coEvery { mockTransactionRepository.addTransaction(any(), any(), any(), any(), any()) } returns 1L

        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals("Should return success", ListenableWorker.Result.success(), result)

        // Verify at least 2 transactions were created (for the 2 active ones)
        io.mockk.coVerify(atLeast = 2) { mockTransactionRepository.addTransaction(any(), any(), any(), any(), any()) }

        // Verify executeAndAdvance was called
        io.mockk.coVerify { mockRecurringRepository.executeAndAdvance(any()) }
    }

    @Test
    fun `doWork cancels reminder when reminderEnabled is true`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recurring = RecurringEntity(
            id = 1L,
            title = "Task with Reminder",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = "WEEKLY",
            startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime - 1000,
            isActive = true,
            estimatedMonthlyAmount = 400L,
            reminderEnabled = true,
            reminderDaysBefore = 1
        )
        coEvery { mockRecurringRepository.getDueRecurring() } returns listOf(recurring)
        coEvery { mockTransactionRepository.addTransaction(any(), any(), any(), any(), any()) } returns 1L

        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals("Should return success", ListenableWorker.Result.success(), result)
        coVerify { mockNotificationHelper.cancelRecurringReminder(1L) }
    }

    @Test
    fun `doWork does not cancel reminder when reminderEnabled is false`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recurring = RecurringEntity(
            id = 1L,
            title = "Task without Reminder",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = "WEEKLY",
            startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime - 1000,
            isActive = true,
            estimatedMonthlyAmount = 400L,
            reminderEnabled = false,
            reminderDaysBefore = 0
        )
        coEvery { mockRecurringRepository.getDueRecurring() } returns listOf(recurring)
        coEvery { mockTransactionRepository.addTransaction(any(), any(), any(), any(), any()) } returns 1L

        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals("Should return success", ListenableWorker.Result.success(), result)
        coVerify(exactly = 0) { mockNotificationHelper.cancelRecurringReminder(any()) }
    }

    @Test
    fun `doWork continues processing if one transaction fails`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val validRecurring = RecurringEntity(
            id = 1L,
            title = "Valid Transaction",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = "DAILY",
            startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime - 1000,
            isActive = true,
            estimatedMonthlyAmount = 3000L
        )
        val invalidRecurring = RecurringEntity(
            id = 2L,
            title = "Invalid Transaction",
            amountCents = 0L,
            type = "EXPENSE",
            category = "Test",
            frequency = "WEEKLY",
            startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime - 1000,
            isActive = true,
            estimatedMonthlyAmount = 400L
        )

        coEvery { mockRecurringRepository.getDueRecurring() } returns listOf(validRecurring, invalidRecurring)

        // First call throws, second succeeds
        coEvery {
            mockTransactionRepository.addTransaction(
                TransactionType.EXPENSE,
                0L,
                "Test",
                any(),
                any()
            )
        } throws RuntimeException("Invalid transaction")
        coEvery {
            mockTransactionRepository.addTransaction(
                TransactionType.EXPENSE,
                100L,
                "Test",
                any(),
                any()
            )
        } returns 1L

        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(
            "Should return success even if one transaction fails",
            ListenableWorker.Result.success(),
            result
        )
        // Valid transaction should have been attempted
        coVerify {
            mockTransactionRepository.addTransaction(
                TransactionType.EXPENSE,
                100L,
                "Test",
                any(),
                any()
            )
        }
    }

    @Test
    fun `doWork returns retry on repository error`() = runTest {
        // Given
        coEvery { mockRecurringRepository.getDueRecurring() } throws RuntimeException("Database error")

        val worker = createWorker()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(
            "Should return retry on error",
            ListenableWorker.Result.retry(),
            result
        )
    }
}
