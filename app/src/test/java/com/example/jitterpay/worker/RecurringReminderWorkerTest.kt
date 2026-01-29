package com.example.jitterpay.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.jitterpay.data.local.entity.RecurringEntity
import com.example.jitterpay.data.repository.RecurringRepository
import com.example.jitterpay.notification.NotificationHelper
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for RecurringReminderWorker.
 *
 * Tests worker behavior with different scenarios:
 * - Notifications disabled
 * - No reminders due
 * - Single reminder due
 * - Multiple reminders due
 * - Error handling
 */
@RunWith(RobolectricTestRunner::class)
class RecurringReminderWorkerTest {

    private lateinit var context: Context

    @Mock
    private lateinit var mockRecurringRepository: RecurringRepository

    @Mock
    private lateinit var mockNotificationHelper: NotificationHelper

    private lateinit var workerFactory: TestWorkerFactory

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Use real Application context from Robolectric (required by WorkManagerTestInitHelper)
        context = RuntimeEnvironment.getApplication()

        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context)

        // Create custom factory with mocked dependencies
        workerFactory = TestWorkerFactory(
            mockRecurringRepository,
            mockNotificationHelper
        )
    }

    @After
    fun tearDown() {
        // No cleanup needed - WorkManagerTestInitHelper initializes for the test context
    }

    @Test
    fun `doWork should return success when notifications are disabled`() {
        // Given
        whenever(mockNotificationHelper.areNotificationsEnabled())
            .thenReturn(false)

        val worker = TestListenableWorkerBuilder<RecurringReminderWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        // When
        val result = worker.startWork().get()

        // Then
        assertEquals(
            "Should return success without querying repository",
            ListenableWorker.Result.success(),
            result
        )
        // Verify notification helper was never called (implies repository was also never called)
        verify(mockNotificationHelper, never()).showRecurringReminder(
            any(), any(), any(), any(), any()
        )
    }

    @Test
    fun `doWork should return success when no reminders due`() = runTest {
        // Given
        whenever(mockNotificationHelper.areNotificationsEnabled())
            .thenReturn(true)

        whenever(mockRecurringRepository.getRecurringTransactionsNeedingReminder(any()))
            .thenReturn(emptyList())

        val worker = TestListenableWorkerBuilder<RecurringReminderWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        // When
        val result = worker.startWork().get()

        // Then
        assertEquals(
            "Should return success when no reminders due",
            ListenableWorker.Result.success(),
            result
        )
        verify(mockNotificationHelper, never()).showRecurringReminder(
            any(), any(), any(), any(), any()
        )
    }

    @Test
    fun `doWork should send notification for single due reminder`() = runTest {
        // Given
        whenever(mockNotificationHelper.areNotificationsEnabled())
            .thenReturn(true)

        val currentTime = System.currentTimeMillis()
        val tomorrow = currentTime + 86400000L // 24 hours
        val recurring = RecurringEntity(
            id = 1L,
            title = "Netflix Subscription",
            amountCents = 1500L, // $15.00
            type = "EXPENSE",
            category = "Entertainment",
            frequency = "MONTHLY",
            startDateMillis = currentTime - 86400000L * 30,
            nextExecutionDateMillis = tomorrow,
            isActive = true,
            estimatedMonthlyAmount = 1500L,
            reminderEnabled = true,
            reminderDaysBefore = 1
        )

        whenever(
            mockRecurringRepository.getRecurringTransactionsNeedingReminder(any())
        ).thenReturn(listOf(recurring))

        val worker = TestListenableWorkerBuilder<RecurringReminderWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        // When
        val result = worker.startWork().get()

        // Then
        assertEquals(
            "Should return success after sending notification",
            ListenableWorker.Result.success(),
            result
        )
        verify(mockNotificationHelper).showRecurringReminder(
            eq(1L),
            eq("Netflix Subscription"),
            eq(recurring.getFormattedAmount()),
            eq(1),
            eq(tomorrow)
        )
    }

    @Test
    fun `doWork should send notifications for multiple due reminders`() = runTest {
        // Given
        whenever(mockNotificationHelper.areNotificationsEnabled())
            .thenReturn(true)

        val currentTime = System.currentTimeMillis()
        val tomorrow = currentTime + 86400000L
        val inTwoDays = currentTime + 86400000L * 2

        val recurring1 = RecurringEntity(
            id = 1L,
            title = "Netflix",
            amountCents = 1500L,
            type = "EXPENSE",
            category = "Entertainment",
            frequency = "MONTHLY",
            startDateMillis = currentTime - 86400000L * 30,
            nextExecutionDateMillis = tomorrow,
            isActive = true,
            estimatedMonthlyAmount = 1500L,
            reminderEnabled = true,
            reminderDaysBefore = 1
        )

        val recurring2 = RecurringEntity(
            id = 2L,
            title = "Rent",
            amountCents = 120000L, // $1,200.00
            type = "EXPENSE",
            category = "Housing",
            frequency = "MONTHLY",
            startDateMillis = currentTime - 86400000L * 30,
            nextExecutionDateMillis = inTwoDays,
            isActive = true,
            estimatedMonthlyAmount = 120000L,
            reminderEnabled = true,
            reminderDaysBefore = 2
        )

        whenever(
            mockRecurringRepository.getRecurringTransactionsNeedingReminder(
                any()
            )
        ).thenReturn(listOf(recurring1, recurring2))

        val worker = TestListenableWorkerBuilder<RecurringReminderWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        // When
        val result = worker.startWork().get()

        // Then
        assertEquals(
            "Should return success after sending all notifications",
            ListenableWorker.Result.success(),
            result
        )
        verify(mockNotificationHelper).showRecurringReminder(
            eq(1L),
            eq("Netflix"),
            eq(recurring1.getFormattedAmount()),
            eq(1),
            eq(tomorrow)
        )
        verify(mockNotificationHelper).showRecurringReminder(
            eq(2L),
            eq("Rent"),
            eq(recurring2.getFormattedAmount()),
            eq(2),
            eq(inTwoDays)
        )
    }

    @Test
    fun `doWork should continue if one notification fails`() = runTest {
        // Given
        whenever(mockNotificationHelper.areNotificationsEnabled())
            .thenReturn(true)

        val currentTime = System.currentTimeMillis()
        val tomorrow = currentTime + 86400000L

        val recurring1 = RecurringEntity(
            id = 1L,
            title = "Valid Transaction",
            amountCents = 1500L,
            type = "EXPENSE",
            category = "Entertainment",
            frequency = "MONTHLY",
            startDateMillis = currentTime - 86400000L * 30,
            nextExecutionDateMillis = tomorrow,
            isActive = true,
            estimatedMonthlyAmount = 1500L,
            reminderEnabled = true,
            reminderDaysBefore = 1
        )

        val recurring2 = RecurringEntity(
            id = 2L,
            title = "Invalid Transaction",
            amountCents = 0L, // This will cause issue
            type = "EXPENSE",
            category = "Test",
            frequency = "MONTHLY",
            startDateMillis = currentTime - 86400000L * 30,
            nextExecutionDateMillis = tomorrow,
            isActive = true,
            estimatedMonthlyAmount = 0L,
            reminderEnabled = true,
            reminderDaysBefore = 1
        )

        whenever(
            mockRecurringRepository.getRecurringTransactionsNeedingReminder(any())
        ).thenReturn(listOf(recurring1, recurring2))

        // Throw exception for second recurring transaction
        whenever(mockNotificationHelper.showRecurringReminder(
            eq(2L), eq("Invalid Transaction"), any(), any(), any()
        )).thenThrow(RuntimeException("Test exception for invalid transaction"))

        val worker = TestListenableWorkerBuilder<RecurringReminderWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        // When
        val result = worker.startWork().get()

        // Then
        assertEquals(
            "Should return success even if one notification fails",
            ListenableWorker.Result.success(),
            result
        )
        // First transaction's notification should have been attempted
        verify(mockNotificationHelper, atLeastOnce()).showRecurringReminder(
            eq(1L),
            eq("Valid Transaction"),
            eq(recurring1.getFormattedAmount()),
            eq(1),
            eq(tomorrow)
        )
    }

    /**
     * Custom WorkerFactory for testing RecurringReminderWorker with mocked dependencies.
     */
    private class TestWorkerFactory(
        private val mockRecurringRepository: RecurringRepository,
        private val mockNotificationHelper: NotificationHelper
    ) : WorkerFactory() {

        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            return if (workerClassName == RecurringReminderWorker::class.java.name) {
                RecurringReminderWorker(
                    appContext,
                    workerParameters,
                    mockRecurringRepository,
                    mockNotificationHelper
                )
            } else {
                null
            }
        }
    }
}
