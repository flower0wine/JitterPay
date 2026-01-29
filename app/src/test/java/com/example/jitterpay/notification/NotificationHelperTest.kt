package com.example.jitterpay.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

/**
 * Unit tests for NotificationHelper.
 *
 * Tests notification channel creation, notification building,
 * and helper methods.
 */
@RunWith(RobolectricTestRunner::class)
class NotificationHelperTest {

    private lateinit var context: Context
    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        notificationHelper = NotificationHelper(context)
    }

    @Test
    fun `createNotificationChannels should create channel for recurring reminders`() {
        // Given
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = Shadows.shadowOf(notificationManager)

        // When - trigger channel creation by showing a notification
        notificationHelper.showRecurringReminder(
            recurringId = 1L,
            title = "Test",
            amount = "$10.00",
            daysBefore = 1,
            nextExecutionDate = System.currentTimeMillis()
        )

        // Then - verify that a notification was created
        val notification = shadowNotificationManager.getNotification(NotificationHelper.NOTIFICATION_ID_BASE + 1)

        assert(notification != null) {
            "Notification should be created, which implies channel creation succeeded"
        }
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
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = Shadows.shadowOf(notificationManager)
        val notification = shadowNotificationManager.getNotification(
            NotificationHelper.NOTIFICATION_ID_BASE + recurringId.toInt()
        )

        assert(notification != null) {
            "Notification should be shown"
        }

        // Extract content from NotificationCompat
        val extras = notification!!.extras
        val contentTitle = extras.getCharSequence(NotificationCompat.EXTRA_TITLE)
        val contentText = extras.getCharSequence(NotificationCompat.EXTRA_TEXT)

        assert(contentTitle == title) {
            "Notification title should match"
        }

        // Content text should contain amount and "tomorrow"
        val contentTextStr = contentText.toString()
        assert(contentTextStr.contains("tomorrow")) {
            "Notification text should mention 'tomorrow' for 1 day before"
        }
        assert(contentTextStr.contains(amount)) {
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
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = Shadows.shadowOf(notificationManager)
        val notification = shadowNotificationManager.getNotification(
            NotificationHelper.NOTIFICATION_ID_BASE + recurringId.toInt()
        )

        assert(notification != null) {
            "Notification should be shown"
        }

        val extras = notification!!.extras
        val contentText = extras.getCharSequence(NotificationCompat.EXTRA_TEXT).toString()
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
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = Shadows.shadowOf(notificationManager)
        val notification = shadowNotificationManager.getNotification(
            NotificationHelper.NOTIFICATION_ID_BASE + recurringId.toInt()
        )

        assert(notification != null) {
            "Notification should be shown"
        }

        val extras = notification!!.extras
        val contentText = extras.getCharSequence(NotificationCompat.EXTRA_TEXT).toString()
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

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = Shadows.shadowOf(notificationManager)
        assert(shadowNotificationManager.getNotification(expectedNotificationId) != null) {
            "Notification should exist before cancel"
        }

        // When
        notificationHelper.cancelRecurringReminder(recurringId)

        // Then
        assert(shadowNotificationManager.getNotification(expectedNotificationId) == null) {
            "Notification should be cancelled"
        }
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

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val shadowNotificationManager = Shadows.shadowOf(notificationManager)
        assert(shadowNotificationManager.allNotifications.size == 2) {
            "Two notifications should exist before cancel all"
        }

        // When
        notificationHelper.cancelAllRecurringReminders()

        // Then
        assert(shadowNotificationManager.allNotifications.isEmpty()) {
            "All notifications should be cancelled"
        }
    }
}
