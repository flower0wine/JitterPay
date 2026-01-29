package com.example.jitterpay.util

import com.example.jitterpay.data.local.entity.BudgetEntity
import com.example.jitterpay.data.local.entity.BudgetPeriodType
import com.example.jitterpay.data.local.entity.GoalEntity
import com.example.jitterpay.data.local.entity.RecurringEntity
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionStatus
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.ui.budget.BudgetData
import java.util.Calendar

/**
 * 测试数据工厂
 *
 * 用于创建测试所需的模拟数据，保持测试代码的一致性和可维护性。
 */
object TestDataFactory {

    // ==================== 预算测试数据 ====================

    /**
     * 创建预算实体
     */
    fun createBudgetEntity(
        id: Long = 1,
        title: String = "Food Budget",
        amountCents: Long = 50000L, // $500.00
        periodType: String = BudgetPeriodType.MONTHLY.name,
        startDate: Long = System.currentTimeMillis(),
        endDate: Long? = null,
        notifyAt80: Boolean = true,
        notifyAt90: Boolean = true,
        notifyAt100: Boolean = true,
        isActive: Boolean = true
    ): BudgetEntity {
        return BudgetEntity(
            id = id,
            title = title,
            amountCents = amountCents,
            periodType = periodType,
            startDate = startDate,
            endDate = endDate,
            notifyAt80 = notifyAt80,
            notifyAt90 = notifyAt90,
            notifyAt100 = notifyAt100,
            isActive = isActive,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * 创建预算数据列表
     */
    fun createBudgetEntities(count: Int): List<BudgetEntity> {
        return List(count) { index ->
            createBudgetEntity(
                id = (index + 1).toLong(),
                title = "Budget ${index + 1}",
                amountCents = ((index + 1) * 10000).toLong()
            )
        }
    }

    /**
     * 创建预算UI数据
     */
    fun createBudgetData(
        id: Long = 1,
        title: String = "Food Budget",
        amount: Double = 500.0,
        periodType: BudgetPeriodType = BudgetPeriodType.MONTHLY,
        startDate: Long = System.currentTimeMillis(),
        endDate: Long? = null,
        spentAmount: Double = 250.0,
        notifyAt80: Boolean = true,
        notifyAt90: Boolean = true,
        notifyAt100: Boolean = true,
        isActive: Boolean = true
    ): BudgetData {
        return BudgetData(
            id = id,
            title = title,
            amount = amount,
            periodType = periodType,
            startDate = startDate,
            endDate = endDate,
            spentAmount = spentAmount,
            notifyAt80 = notifyAt80,
            notifyAt90 = notifyAt90,
            notifyAt100 = notifyAt100,
            isActive = isActive
        )
    }

    // ==================== 交易测试数据 ====================

    /**
     * 创建交易实体
     */
    fun createTransactionEntity(
        id: Long = 1,
        type: TransactionType = TransactionType.EXPENSE,
        amountCents: Long = 550L, // $5.50
        category: String = "Dining",
        description: String = "",
        dateMillis: Long = System.currentTimeMillis(),
        status: String = TransactionStatus.COMPLETED.name
    ): TransactionEntity {
        return TransactionEntity(
            id = id,
            type = type.name,
            amountCents = amountCents,
            category = category,
            description = description,
            dateMillis = dateMillis,
            status = status,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * 创建交易数据列表
     */
    fun createTransactionEntities(count: Int): List<TransactionEntity> {
        return List(count) { index ->
            createTransactionEntity(
                id = (index + 1).toLong(),
                amountCents = ((index + 1) * 100).toLong(),
                category = "Category ${index + 1}"
            )
        }
    }

    /**
     * 创建支出交易
     */
    fun createExpenseTransaction(
        amountCents: Long = 550L,
        category: String = "Dining",
        description: String = ""
    ): TransactionEntity {
        return createTransactionEntity(
            type = TransactionType.EXPENSE,
            amountCents = amountCents,
            category = category,
            description = description
        )
    }

    /**
     * 创建收入交易
     */
    fun createIncomeTransaction(
        amountCents: Long = 500000L, // $5000.00
        category: String = "Salary",
        description: String = ""
    ): TransactionEntity {
        return createTransactionEntity(
            type = TransactionType.INCOME,
            amountCents = amountCents,
            category = category,
            description = description
        )
    }

    // ==================== 目标测试数据 ====================

    /**
     * 创建目标实体
     */
    fun createGoalEntity(
        id: Long = 1,
        title: String = "New Laptop",
        targetAmountCents: Long = 150000L, // $1500.00
        currentAmountCents: Long = 50000L, // $500.00
        iconType: String = "LAPTOP"
    ): GoalEntity {
        return GoalEntity(
            id = id,
            title = title,
            targetAmountCents = targetAmountCents,
            currentAmountCents = currentAmountCents,
            iconType = iconType,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isCompleted = false
        )
    }

    /**
     * 创建目标数据列表
     */
    fun createGoalEntities(count: Int): List<GoalEntity> {
        return List(count) { index ->
            createGoalEntity(
                id = (index + 1).toLong(),
                title = "Goal ${index + 1}",
                targetAmountCents = ((index + 1) * 50000).toLong()
            )
        }
    }

    // ==================== 定期交易测试数据 ====================

    /**
     * 创建定期交易实体
     */
    fun createRecurringEntity(
        id: Long = 1,
        title: String = "Netflix Subscription",
        amountCents: Long = 1500L, // $15.00
        type: TransactionType = TransactionType.EXPENSE,
        category: String = "Entertainment",
        frequency: String = "MONTHLY",
        startDateMillis: Long = System.currentTimeMillis()
    ): RecurringEntity {
        return RecurringEntity(
            id = id,
            title = title,
            amountCents = amountCents,
            type = type.name,
            category = category,
            frequency = frequency,
            startDateMillis = startDateMillis,
            nextExecutionDateMillis = startDateMillis,
            isActive = true,
            estimatedMonthlyAmount = amountCents,
            reminderEnabled = false,
            reminderDaysBefore = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * 创建定期交易数据列表
     */
    fun createRecurringEntities(count: Int): List<RecurringEntity> {
        return List(count) { index ->
            createRecurringEntity(
                id = (index + 1).toLong(),
                title = "Recurring ${index + 1}",
                amountCents = ((index + 1) * 500).toLong()
            )
        }
    }

    // ==================== 时间戳辅助方法 ====================

    /**
     * 获取N天前的时间戳
     */
    fun getDaysAgo(days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return calendar.timeInMillis
    }

    /**
     * 获取N天后的时间戳
     */
    fun getDaysAfter(days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.timeInMillis
    }

    /**
     * 获取本月开始时间戳
     */
    fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取本月结束时间戳
     */
    fun getEndOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    // ==================== 金额转换辅助方法 ====================

    /**
     * 将美元转换为分
     */
    fun dollarsToCents(dollars: Double): Long {
        return (dollars * 100).toLong()
    }

    /**
     * 将分转换为美元
     */
    fun centsToDollars(cents: Long): Double {
        return cents / 100.0
    }
}