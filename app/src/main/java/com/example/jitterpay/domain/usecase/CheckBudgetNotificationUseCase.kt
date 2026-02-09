package com.example.jitterpay.domain.usecase

import android.util.Log
import com.example.jitterpay.data.local.entity.BudgetEntity
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.BudgetRepository
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.notification.NotificationHelper
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 预算通知检查用例
 *
 * 负责检查新交易后是否需要发送预算警告通知。
 * 设计为纯业务逻辑，不关心触发时机。
 *
 * 核心职责：
 * 1. 检查新交易是否需要触发预算通知
 * 2. 计算预算进度并判断阈值
 * 3. 委托 NotificationHelper 发送通知
 */
@Singleton
class CheckBudgetNotificationUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val notificationHelper: NotificationHelper
) {
    companion object {
        private const val TAG = "BudgetNotification"
    }

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    /**
     * 检查新交易后是否需要发送预算通知
     *
     * @param newTransaction 新添加的交易
     */
    suspend fun checkAndNotify(newTransaction: TransactionEntity) {
        // 只处理支出交易
        if (newTransaction.type != TransactionType.EXPENSE.name) {
            return
        }

        try {
            // 获取所有激活的预算
            val budgets: List<BudgetEntity> = budgetRepository.getActiveBudgets().first()

            for (budget in budgets) {
                checkBudgetThreshold(budget, newTransaction)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check budget notifications", e)
        }
    }

    /**
     * 检查单个预算的阈值
     */
    private suspend fun checkBudgetThreshold(
        budget: BudgetEntity,
        newTransaction: TransactionEntity
    ) {
        val (periodStart, periodEnd) = budget.getCurrentPeriodRange()

        // 如果新交易不在预算周期内，跳过
        if (newTransaction.dateMillis !in periodStart..periodEnd) {
            return
        }

        // 计算当前周期的总支出
        val totalSpent: Long = calculatePeriodSpent(periodStart, periodEnd)

        // 计算进度
        val progress: Float = totalSpent.toFloat() / budget.amountCents

        // 判断应该触发哪个阈值通知
        val triggeredThreshold: Float? = determineTriggeredThreshold(budget, progress)

        if (triggeredThreshold != null) {
            sendBudgetNotification(budget, totalSpent, triggeredThreshold)
        }
    }

    /**
     * 计算指定时间范围内的支出总额
     */
    private suspend fun calculatePeriodSpent(periodStart: Long, periodEnd: Long): Long {
        val transactions: List<TransactionEntity> =
            transactionRepository.getTransactionsByDateRange(periodStart, periodEnd).first()

        return transactions
            .filter { tx: TransactionEntity -> tx.type == TransactionType.EXPENSE.name }
            .sumOf { tx: TransactionEntity -> tx.amountCents }
    }

    /**
     * 判断应该触发哪个阈值通知
     *
     * @return 需要触发的阈值（0.8, 0.9, 或 1.0），如果都不需要则返回 null
     */
    private fun determineTriggeredThreshold(budget: BudgetEntity, progress: Float): Float? {
        return when {
            // 100% 阈值：超出预算
            progress >= 1.0f && budget.notifyAt100 -> 1.0f

            // 90% 阈值：危险区
            progress >= 0.9f && progress < 1.0f && budget.notifyAt90 -> 0.9f

            // 80% 阈值：警告区
            progress >= 0.8f && progress < 0.9f && budget.notifyAt80 -> 0.8f

            // 未达到任何阈值
            else -> null
        }
    }

    /**
     * 发送预算警告通知
     */
    private fun sendBudgetNotification(
        budget: BudgetEntity,
        spentCents: Long,
        threshold: Float
    ) {
        val spentFormatted: String = currencyFormatter.format(spentCents / 100.0)
        val totalFormatted: String = currencyFormatter.format(budget.amountCents / 100.0)

        notificationHelper.showBudgetWarning(
            budgetId = budget.id,
            budgetTitle = budget.title,
            spentAmount = spentFormatted,
            totalBudget = totalFormatted,
            threshold = threshold
        )

        Log.d(TAG, "Budget notification sent: ${budget.title} at ${(threshold * 100).toInt()}%")
    }
}
