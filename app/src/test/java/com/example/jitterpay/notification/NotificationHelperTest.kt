package com.example.jitterpay.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

/**
 * Unit tests for NotificationHelper.
 *
 * Tests notification channel creation, notification building,
 * and helper methods.
 */
@RunWith(RobolectricTestRunner::class)
class NotificationHelperTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockNotificationManager: NotificationManager

    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Mock getSystemService to return our mock notification manager
        whenever(mockContext.getSystemService(Context.NOTIFICATION_SERVICE))
            .thenReturn(mockNotificationManager)

        notificationHelper = NotificationHelper(mockContext)
    }

    @Test
    fun `createNotificationChannels should create channel for recurring reminders`() {
        // Given
        val channelId = "recurring_reminder"
        val channelName = "Recurring Transaction Reminders"
        val channelDescription = "Notifications for upcoming recurring transactions"

        // When
        // NotificationHelper creates channels in constructor
        val shadowNotificationManager = Shadows.shadowOf(mockNotificationManager)
        val createdChannels = shadowNotificationManager.notificationChannels

        // Then
        val recurringChannel = createdChannels.find { it.id == channelId }

        assert(recurringChannel != null) {
            "Recurring reminder channel should be created"
        }

        assert(recurringChannel!!.name == channelName) {
            "Channel name should match expected"
        }

        // Note: description might not be set in API < 26
        // Robolectric creates mock channel without description
    }

    @Test
    fun `showRecurringReminder should build notification with correct title and text`() {
        // Given
        val recurringId = 123L
        val title = "Netflix Subscription"
        val amount = "+$15.00"
        val daysBefore = 1
        val nextExecutionDate = System.currentTimeMillis() + 86400000L

        // When
        notificationHelper.showRecurringReminder(
            recurringId = recurringId,
            title = title,
            amount = amount,
            daysBefore = daysBefore,
            nextExecutionDate = nextExecutionDate
        )

        // Then
        val shadowNotificationManager = Shadows.shadowOf(mockNotificationManager)
        val notification = shadowNotificationManager.getNotification(
            NotificationHelper.NOTIFICATION_ID_BASE + recurringId.toInt()
        )

        assert(notification != null) {
            "Notification should be shown"
        }

        assert(notification!!.contentTitle == title) {
            "Notification title should match"
        }

        // Content text should contain amount and "tomorrow"
        val contentText = notification!!.contentText.toString()
        assert(contentText.contains("tomorrow")) {
            "Notification text should mention 'tomorrow' for 1 day before"
        }
        assert(contentText.contains(amount)) {
            "Notification text should contain amount"
        }
    }

    @Test
    fun `showRecurringReminder for 0 days before should show 'due today'`() {
        // Given
        val recurringId = 124L
        val title = "Rent Payment"
        val amount = "-$1,200.00"
        val daysBefore = 0
        val nextExecutionDate = System.currentTimeMillis()

        // When
        notificationHelper.showRecurringReminder(
            recurringId = recurringId,
            title = title,
            amount = amount,
            daysBefore = daysBefore,
            nextExecutionDate = nextExecutionDate
        )

        // Then
        val shadowNotificationManager = Shadows.shadowOf(mockNotificationManager)
        val notification = shadowNotificationManager.getNotification(
            NotificationHelper.NOTIFICATION_ID_BASE + recurringId.toInt()
        )

        assert(notification != null) {
            "Notification should be shown"
        }

        val contentText = notification!!.contentText.toString()
        assert(contentText.contains("due today")) {
            "Notification text should mention 'due today' for 0 days before"
        }
    }

    @Test
    fun `showRecurringReminder for multiple days before should show 'in X days'`() {
        // Given
        val recurringId = 125L
        val title = "Car Insurance"
        val amount = "-$150.00"
        val daysBefore = 3
        val nextExecutionDate = System.currentTimeMillis() + (3 * 86400000L)

        // When
        notificationHelper.showRecurringReminder(
            recurringId = recurringId,
            title = title,
            amount = amount,
            daysBefore = daysBefore,
            nextExecutionDate = nextExecutionDate
        )

        // Then
        val shadowNotificationManager = Shadows.shadowOf(mockNotificationManager)
        val notification = shadowNotificationManager.getNotification(
            NotificationHelper.NOTIFICATION_ID_BASE + recurringId.toInt()
        )

        assert(notification != null) {
            "Notification should be shown"
        }

        val contentText = notification!!.contentText.toString()
        assert(contentText.contains("in 3 days")) {
            "Notification text should mention 'in 3 days'"
        }
    }

    @Test
    fun `cancelRecurringReminder should cancel specific notification`() {
        // Given
        val recurringId = 126L
        val expectedNotificationId = NotificationHelper.NOTIFICATION_ID_BASE + recurringId.toInt()

        // First show a notification
        notificationHelper.showRecurringReminder(
            recurringId = recurringId,
            title = "Test",
            amount = "$10.00",
            daysBefore = 1,
            nextExecutionDate = System.currentTimeMillis()
        )

        val shadowNotificationManager = Shadows.shadowOf(mockNotificationManager)
        assert(shadowNotificationManager.getNotification(expectedNotificationId) != null) {
            "Notification should exist before cancel"
        }

        // When
        notificationHelper.cancelRecurringReminder(recurringId)

        // Then
        assert(shadowNotificationManager.getNotification(expectedNotificationId) == null) {
            "Notification should be cancelled"
        }
        verify(mockNotificationManager).cancel(eq(expectedNotificationId))
    }

    @Test
    fun `cancelAllRecurringReminders should cancel all notifications`() {
        // Given
        notificationHelper.showRecurringReminder(
            recurringId = 1L,
            title = "Test 1",
            amount = "$10.00",
            daysBefore = 1,
            nextExecutionDate = System.currentTimeMillis()
        )

        notificationHelper.showRecurringReminder(
            recurringId = 2L,
            title = "Test 2",
            amount = "$20.00",
            daysBefore = 1,
            nextExecutionDate = System.currentTimeMillis()
        )

        val shadowNotificationManager = Shadows.shadowOf(mockNotificationManager)
        assert(shadowNotificationManager.allNotifications.size == 2) {
            "Two notifications should exist before cancel all"
        }

        // When
        notificationHelper.cancelAllRecurringReminders()

        // Then
        assert(shadowNotificationManager.allNotifications.isEmpty()) {
            "All notifications should be cancelled"
        }
        verify(mockNotificationManager).cancelAll()
    }
}
