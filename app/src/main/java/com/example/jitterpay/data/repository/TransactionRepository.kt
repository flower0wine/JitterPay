package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.dao.CategoryTotal
import com.example.jitterpay.data.local.dao.TransactionDao
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交易数据仓库
 *
 * 封装所有交易相关的数据库操作，提供统一的数据访问接口。
 * 使用Repository模式隔离数据源，方便后续替换数据实现（如从本地DB改为远程API）。
 *
 * 所有金额单位说明：
 * - 内部存储和计算使用"分"（cents）为单位
 * - 对外暴露的方法使用Long类型表示金额（分）
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    // ==================== CRUD 操作 ====================

    /**
     * 添加交易记录
     *
     * @param type 交易类型（收入/支出）
     * @param amountCents 金额（分）
     * @param category 分类
     * @param description 描述/备注
     * @param dateMillis 交易日期（毫秒）
     * @return 插入记录的ID
     */
    suspend fun addTransaction(
        type: TransactionType,
        amountCents: Long,
        category: String,
        description: String = "",
        dateMillis: Long
    ): Long {
        val entity = TransactionEntity(
            type = type.name,
            amountCents = amountCents,
            category = category,
            description = description,
            dateMillis = dateMillis
        )
        return transactionDao.insertTransaction(entity)
    }

    /**
     * 更新交易记录
     */
    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(
            transaction.copy(updatedAt = System.currentTimeMillis())
        )
    }

    /**
     * 删除交易记录
     */
    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    /**
     * 根据ID删除交易记录
     */
    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    // ==================== 查询操作 ====================

    /**
     * 获取所有交易记录（按日期降序）
     */
    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    /**
     * 根据ID获取交易记录
     */
    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    /**
     * 根据类型获取交易记录
     */
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByType(type.name)
    }

    /**
     * 根据分类获取交易记录
     */
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCategory(category)
    }

    /**
     * 获取指定日期范围内的交易记录
     */
    fun getTransactionsByDateRange(
        startDateMillis: Long,
        endDateMillis: Long
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDateMillis, endDateMillis)
    }

    /**
     * 获取指定月份的交易记录
     */
    fun getTransactionsByMonth(
        startOfMonth: Long,
        endOfMonth: Long
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByMonth(startOfMonth, endOfMonth)
    }

    // ==================== 统计操作 ====================

    /**
     * 获取当前总余额（收入-支出，分单位）
     */
    fun getTotalBalance(): Flow<Long> {
        return transactionDao.getTotalBalance()
    }

    /**
     * 获取指定月份的收入总额（分单位）
     */
    fun getMonthlyIncome(startOfMonth: Long, endOfMonth: Long): Flow<Long> {
        return transactionDao.getMonthlyIncome(startOfMonth, endOfMonth)
    }

    /**
     * 获取指定月份的支出总额（分单位）
     */
    fun getMonthlyExpense(startOfMonth: Long, endOfMonth: Long): Flow<Long> {
        return transactionDao.getMonthlyExpense(startOfMonth, endOfMonth)
    }

    /**
     * 按分类统计指定月份的支出
     */
    fun getExpenseByCategory(startOfMonth: Long, endOfMonth: Long): Flow<List<CategoryTotal>> {
        return transactionDao.getExpenseByCategory(startOfMonth, endOfMonth)
    }

    // ==================== 实时统计操作 ====================

    /**
     * 获取当前周期的收入总额（分单位）
     * @param periodType 周期类型：WEEKLY, MONTHLY, YEARLY
     * @note 使用 kotlinx.datetime 动态计算当前周期的时间范围
     */
    fun getCurrentPeriodIncome(periodType: String): Flow<Long> {
        return getAllTransactions().map { transactions ->
            val (startMillis, endMillis) = calculateCurrentPeriodRange(periodType)
            transactions
                .filter { it.type == TransactionType.INCOME.name }
                .filter { it.dateMillis >= startMillis && it.dateMillis < endMillis }
                .sumOf { it.amountCents }
        }
    }

    /**
     * 获取当前周期的支出总额（分单位）
     * @param periodType 周期类型：WEEKLY, MONTHLY, YEARLY
     */
    fun getCurrentPeriodExpense(periodType: String): Flow<Long> {
        return getAllTransactions().map { transactions ->
            val (startMillis, endMillis) = calculateCurrentPeriodRange(periodType)
            transactions
                .filter { it.type == TransactionType.EXPENSE.name }
                .filter { it.dateMillis >= startMillis && it.dateMillis < endMillis }
                .sumOf { it.amountCents }
        }
    }

    /**
     * 按分类统计当前周期的支出
     * @param periodType 周期类型：WEEKLY, MONTHLY, YEARLY
     */
    fun getCurrentPeriodExpenseByCategory(periodType: String): Flow<List<CategoryTotal>> {
        return getAllTransactions().map { transactions ->
            val (startMillis, endMillis) = calculateCurrentPeriodRange(periodType)
            transactions
                .filter { it.type == TransactionType.EXPENSE.name }
                .filter { it.dateMillis >= startMillis && it.dateMillis < endMillis }
                .groupBy { it.category }
                .map { (category, txs) ->
                    CategoryTotal(category, txs.sumOf { it.amountCents })
                }
                .sortedByDescending { it.totalAmount }
        }
    }

    /**
     * 计算当前周期的时间范围
     * @param periodType 周期类型：WEEKLY, MONTHLY, YEARLY
     * @return Pair<开始时间戳(毫秒), 结束时间戳(毫秒)>
     */
    private fun calculateCurrentPeriodRange(periodType: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endMillis = calendar.timeInMillis

        when (periodType) {
            "WEEKLY" -> {
                // 计算本周一
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                // Calendar.SUNDAY = 1, Calendar.MONDAY = 2
                // 需要找到本周的周一
                val daysFromMonday = when (dayOfWeek) {
                    Calendar.SUNDAY -> 6  // 周日往前6天
                    else -> dayOfWeek - 2  // 周一到周六往前(dayOfWeek-2)天
                }
                calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
            }
            "MONTHLY" -> {
                // 设置到本月1日
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            "YEARLY" -> {
                // 设置到本年1月1日
                calendar.set(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // 清除时分秒
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startMillis = calendar.timeInMillis
        return Pair(startMillis, endMillis)
    }

    /**
     * 获取交易记录总数
     */
    suspend fun getTransactionCount(): Int {
        return transactionDao.getTransactionCount()
    }

    /**
     * 清空所有交易记录（谨慎使用）
     */
    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }
}
