package com.example.jitterpay.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.jitterpay.MainActivity
import com.example.jitterpay.R

class NotificationHelper(private val context: Context) {

    private var channelCreated = false

    companion object {
        private const val CHANNEL_ID_RECURRING_REMINDER = "recurring_reminder"
        private const val CHANNEL_NAME_RECURRING_REMINDER = "Recurring Transaction Reminders"
        internal const val NOTIFICATION_ID_BASE = 1000
        private const val CHANNEL_DESC_RECURRING_REMINDER =
            "Notifications for upcoming recurring transactions"
    }

    init {
        // Removed channel creation from init block to avoid blocking startup
        // Channels will be created lazily when first needed
    }

    private fun ensureNotificationChannels() {
        if (channelCreated) return

        synchronized(this) {
            if (channelCreated) return

            try {
                createNotificationChannels()
                channelCreated = true
            } catch (e: Exception) {
                android.util.Log.e("NotificationHelper", "Failed to create notification channels", e)
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val recurringReminderChannel = NotificationChannel(
                CHANNEL_ID_RECURRING_REMINDER,
                CHANNEL_NAME_RECURRING_REMINDER,
                NotificationManager.IMPORTANCE_HIGH
            )
            recurringReminderChannel.description = CHANNEL_DESC_RECURRING_REMINDER

            notificationManager.createNotificationChannel(recurringReminderChannel)
        }
    }

    @Suppress("MissingPermission")
    fun showRecurringReminder(
        recurringId: Long,
        title: String,
        amount: String,
        daysBefore: Int,
        nextExecutionDate: Long
    ) {
        // Ensure notification channels are created before showing notifications
        ensureNotificationChannels()

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val contentText = when (daysBefore) {
            0 -> "Your recurring payment of $amount is due today"
            1 -> "Your recurring payment of $amount is due tomorrow"
            else -> "Your recurring payment of $amount is due in $daysBefore days"
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("navigate_to", "recurring_detail")
        intent.putExtra("recurring_id", recurringId)

        val pendingIntent = PendingIntent.getActivity(
            context,
            recurringId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_RECURRING_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
                    .setBigContentTitle(title)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationId = NOTIFICATION_ID_BASE + recurringId.toInt()
        notificationManager.notify(notificationId, notification)
    }

    fun cancelRecurringReminder(recurringId: Long) {
        val notificationId = NOTIFICATION_ID_BASE + recurringId.toInt()
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    fun cancelAllRecurringReminders() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }
}
