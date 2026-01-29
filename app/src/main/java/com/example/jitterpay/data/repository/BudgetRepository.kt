package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.dao.BudgetDao
import com.example.jitterpay.data.local.dao.TransactionDao
import com.example.jitterpay.data.local.entity.BudgetEntity
import com.example.jitterpay.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 预算数据仓库
 *
 * 封装所有预算相关的数据库操作，提供统一的数据访问接口。
 */
@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) {

    // ==================== 预算 CRUD 操作 ====================

    /**
     * 创建新预算
     */
    suspend fun createBudget(
        title: String,
        amountCents: Long,
        periodType: String,
        startDate: Long,
        endDate: Long? = null,
        notifyAt80: Boolean = true,
        notifyAt90: Boolean = true,
        notifyAt100: Boolean = true
    ): Long {
        val entity = BudgetEntity(
            title = title,
            amountCents = amountCents,
            periodType = periodType,
            startDate = startDate,
            endDate = endDate,
            notifyAt80 = notifyAt80,
            notifyAt90 = notifyAt90,
            notifyAt100 = notifyAt100,
            isActive = true
        )
        return budgetDao.insertBudget(entity)
    }

    /**
     * 更新预算
     */
    suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget.copy(updatedAt = System.currentTimeMillis()))
    }

    /**
     * 删除预算
     */
    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
    }

    /**
     * 根据ID删除预算
     */
    suspend fun deleteBudgetById(budgetId: Long) {
        budgetDao.deleteBudgetById(budgetId)
    }

    // ==================== 预算查询操作 ====================

    /**
     * 获取所有预算
     */
    fun getAllBudgets(): Flow<List<BudgetEntity>> {
        return budgetDao.getAllBudgets()
    }

    /**
     * 获取所有激活的预算
     */
    fun getActiveBudgets(): Flow<List<BudgetEntity>> {
        return budgetDao.getActiveBudgets()
    }

    /**
     * 根据ID获取预算
     */
    suspend fun getBudgetById(budgetId: Long): BudgetEntity? {
        return budgetDao.getBudgetById(budgetId)
    }

    /**
     * 根据ID获取预算（Flow版本）
     */
    fun getBudgetByIdFlow(budgetId: Long): Flow<BudgetEntity?> {
        return budgetDao.getBudgetByIdFlow(budgetId)
    }

    // ==================== 预算统计操作 ====================

    /**
     * 获取预算的支出金额（Flow版本）
     */
    fun getSpentAmountForPeriodFlow(startDate: Long, endDate: Long): Flow<Long> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate).map { transactions ->
            transactions
                .filter { it.dateMillis >= startDate && it.dateMillis <= endDate }
                .filter { it.type == TransactionType.EXPENSE.name }
                .sumOf { it.amountCents }
        }
    }

    /**
     * 获取预算总数
     */
    suspend fun getBudgetCount(): Int {
        return budgetDao.getBudgetCount()
    }

    /**
     * 获取激活的预算总数
     */
    suspend fun getActiveBudgetCount(): Int {
        return budgetDao.getActiveBudgetCount()
    }
}
