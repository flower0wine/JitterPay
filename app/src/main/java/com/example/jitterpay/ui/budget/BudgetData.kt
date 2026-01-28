package com.example.jitterpay.ui.budget

import com.example.jitterpay.data.local.entity.BudgetEntity
import com.example.jitterpay.data.local.entity.BudgetPeriodType

/**
 * 预算UI数据模型
 */
data class BudgetData(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val periodType: BudgetPeriodType,
    val startDate: Long,
    val endDate: Long? = null,
    val spentAmount: Double = 0.0,
    val notifyAt80: Boolean = true,
    val notifyAt90: Boolean = true,
    val notifyAt100: Boolean = true,
    val isActive: Boolean = true
) {
    /**
     * 计算预算使用进度（0.0 ~ 1.0）
     */
    val progress: Float
        get() = if (amount == 0.0) 0f else (spentAmount / amount).toFloat().coerceIn(0f, 1f)

    /**
     * 计算剩余预算
     */
    val remainingAmount: Double
        get() = (amount - spentAmount).coerceAtLeast(0.0)

    /**
     * 判断是否超出预算
     */
    val isOverBudget: Boolean
        get() = spentAmount > amount

    /**
     * 获取预算状态
     */
    val status: BudgetStatus
        get() = when {
            isOverBudget -> BudgetStatus.OVER_BUDGET
            progress >= 0.9f -> BudgetStatus.CRITICAL
            progress >= 0.8f -> BudgetStatus.WARNING
            else -> BudgetStatus.HEALTHY
        }

    /**
     * 获取周期描述
     */
    val periodDescription: String
        get() = when (periodType) {
            BudgetPeriodType.DAILY -> "Daily"
            BudgetPeriodType.WEEKLY -> "Weekly"
            BudgetPeriodType.MONTHLY -> "Monthly"
            BudgetPeriodType.YEARLY -> "Yearly"
        }
}

/**
 * 预算状态枚举
 */
enum class BudgetStatus {
    HEALTHY,        // 健康状态（< 80%）
    WARNING,        // 警告状态（80% ~ 90%）
    CRITICAL,       // 危险状态（90% ~ 100%）
    OVER_BUDGET     // 超出预算（> 100%）
}

/**
 * 将BudgetEntity转换为BudgetData
 */
fun BudgetEntity.toBudgetData(spentAmount: Double = 0.0): BudgetData {
    return BudgetData(
        id = id,
        title = title,
        amount = amountCents / 100.0,
        periodType = BudgetPeriodType.valueOf(periodType),
        startDate = startDate,
        endDate = endDate,
        spentAmount = spentAmount,
        notifyAt80 = notifyAt80,
        notifyAt90 = notifyAt90,
        notifyAt100 = notifyAt100,
        isActive = isActive
    )
}
