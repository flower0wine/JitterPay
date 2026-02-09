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

/**
 * 通知助手类
 *
 * 管理应用内所有通知的创建和发送，包括：
 * - 周期性交易提醒 (Recurring Reminder)
 * - 预算阈值警告 (Budget Alert)
 */
class NotificationHelper(private val context: Context) {

    private var channelCreated = false

    companion object {
        // Recurring Reminder Constants
        private const val CHANNEL_ID_RECURRING_REMINDER = "recurring_reminder"
        private const val CHANNEL_NAME_RECURRING_REMINDER = "Recurring Transaction Reminders"
        private const val CHANNEL_DESC_RECURRING_REMINDER =
            "Notifications for upcoming recurring transactions"
        internal const val NOTIFICATION_ID_BASE = 1000

        // Budget Alert Constants
        private const val CHANNEL_ID_BUDGET_ALERT = "budget_alert"
        private const val CHANNEL_NAME_BUDGET_ALERT = "Budget Alerts"
        private const val CHANNEL_DESC_BUDGET_ALERT =
            "Notifications when you reach budget thresholds"
        internal const val NOTIFICATION_ID_BUDGET_BASE = 2000
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

            // Recurring Reminder Channel
            val recurringReminderChannel = NotificationChannel(
                CHANNEL_ID_RECURRING_REMINDER,
                CHANNEL_NAME_RECURRING_REMINDER,
                NotificationManager.IMPORTANCE_HIGH
            )
            recurringReminderChannel.description = CHANNEL_DESC_RECURRING_REMINDER

            // Budget Alert Channel
            val budgetAlertChannel = NotificationChannel(
                CHANNEL_ID_BUDGET_ALERT,
                CHANNEL_NAME_BUDGET_ALERT,
                NotificationManager.IMPORTANCE_HIGH
            )
            budgetAlertChannel.description = CHANNEL_DESC_BUDGET_ALERT

            notificationManager.createNotificationChannel(recurringReminderChannel)
            notificationManager.createNotificationChannel(budgetAlertChannel)
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

    // ==================== Budget Alert Notifications ====================

    /**
     * 显示预算警告通知
     *
     * @param budgetId 预算ID，用于生成通知ID和导航
     * @param budgetTitle 预算标题
     * @param spentAmount 已花费金额（格式化字符串）
     * @param totalBudget 预算总额（格式化字符串）
     * @param threshold 触发阈值：80%, 90%, 或 100%
     */
    @Suppress("MissingPermission")
    fun showBudgetWarning(
        budgetId: Long,
        budgetTitle: String,
        spentAmount: String,
        totalBudget: String,
        threshold: Float
    ) {
        ensureNotificationChannels()

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val contentText = when {
            threshold >= 1.0f -> {
                "You've exceeded your $budgetTitle budget! " +
                    "Spent: $spentAmount of $totalBudget"
            }
            threshold >= 0.9f -> {
                "Caution! You've used ${(threshold * 100).toInt()}% of your $budgetTitle budget. " +
                    "Spent: $spentAmount of $totalBudget"
            }
            else -> {
                "Warning: You've used ${(threshold * 100).toInt()}% of your $budgetTitle budget. " +
                    "Spent: $spentAmount of $totalBudget"
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "budget_detail")
            putExtra("budget_id", budgetId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            budgetId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_BUDGET_ALERT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getBudgetNotificationTitle(budgetTitle, threshold))
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
                    .setBigContentTitle(getBudgetNotificationTitle(budgetTitle, threshold))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationId = NOTIFICATION_ID_BUDGET_BASE + budgetId.toInt()
        notificationManager.notify(notificationId, notification)
    }

    /**
     * 获取预算通知标题
     */
    private fun getBudgetNotificationTitle(budgetTitle: String, threshold: Float): String {
        return when {
            threshold >= 1.0f -> "Budget Exceeded: $budgetTitle"
            threshold >= 0.9f -> "Budget Critical: $budgetTitle"
            else -> "Budget Warning: $budgetTitle"
        }
    }

    /**
     * 取消预算通知
     */
    fun cancelBudgetNotification(budgetId: Long) {
        val notificationId = NOTIFICATION_ID_BUDGET_BASE + budgetId.toInt()
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * 取消所有预算通知
     */
    fun cancelAllBudgetNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
