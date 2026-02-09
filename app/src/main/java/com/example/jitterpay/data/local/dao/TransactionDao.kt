package com.example.jitterpay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.jitterpay.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 交易数据访问对象 (DAO)
 *
 * 提供所有交易数据的CRUD操作，使用Flow实现响应式数据流。
 * 所有查询操作都返回Flow以支持Compose的响应式UI更新。
 */
@Dao
interface TransactionDao {

    /**
     * 插入单条交易记录
     * @param transaction 交易实体
     * @return 插入记录的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    /**
     * 批量插入交易记录
     * @param transactions 交易实体列表
     * @return 插入记录ID列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    /**
     * 更新交易记录
     * @param transaction 交易实体
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    /**
     * 更新交易的预算ID
     * @param transactionId 交易ID
     * @param budgetId 预算ID（可为null表示不关联预算）
     */
    @Query("UPDATE transactions SET budgetId = :budgetId, updatedAt = :updatedAt WHERE id = :transactionId")
    suspend fun updateTransactionBudgetId(transactionId: Long, budgetId: Long?, updatedAt: Long = System.currentTimeMillis())

    /**
     * 删除交易记录
     * @param transaction 交易实体
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    /**
     * 根据ID删除交易记录
     * @param id 交易ID
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    /**
     * 获取所有交易记录（按日期降序排序）
     * @return 交易实体Flow
     */
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * 根据ID获取交易记录
     * @param id 交易ID
     * @return 交易实体Flow
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    /**
     * 根据类型获取交易记录（收入或支出）
     * @param type 交易类型
     * @return 交易实体Flow
     */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateMillis DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    /**
     * 根据分类获取交易记录
     * @param category 分类名称
     * @return 交易实体Flow
     */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY dateMillis DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    /**
     * 获取指定日期范围内的交易记录
     * @param startDateMillis 起始日期（毫秒）
     * @param endDateMillis 结束日期（毫秒）
     * @return 交易实体Flow
     */
    @Query("SELECT * FROM transactions WHERE dateMillis BETWEEN :startDateMillis AND :endDateMillis ORDER BY dateMillis DESC")
    fun getTransactionsByDateRange(startDateMillis: Long, endDateMillis: Long): Flow<List<TransactionEntity>>

    /**
     * 获取指定月份的交易记录（用于月度统计）
     * @param yearMonthMillis 任意属于该月份的毫秒时间戳
     * @return 交易实体Flow
     */
    @Query("""
        SELECT * FROM transactions
        WHERE dateMillis >= :startOfMonth AND dateMillis < :endOfMonth
        ORDER BY dateMillis DESC
    """)
    fun getTransactionsByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<TransactionEntity>>

    /**
     * 计算总余额（收入-支出）
     * @return 总金额（分）
     */
    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END), 0)
        FROM transactions
    """)
    fun getTotalBalance(): Flow<Long>

    /**
     * 计算指定月份的总收入
     * @param startOfMonth 月初毫秒时间戳
     * @param endOfMonth 月末毫秒时间戳
     * @return 月收入总额（分）
     */
    @Query("""
        SELECT COALESCE(SUM(amountCents), 0)
        FROM transactions
        WHERE type = 'INCOME' AND dateMillis >= :startOfMonth AND dateMillis < :endOfMonth
    """)
    fun getMonthlyIncome(startOfMonth: Long, endOfMonth: Long): Flow<Long>

    /**
     * 计算指定月份的总支出
     * @param startOfMonth 月初毫秒时间戳
     * @param endOfMonth 月末毫秒时间戳
     * @return 月支出总额（分）
     */
    @Query("""
        SELECT COALESCE(SUM(amountCents), 0)
        FROM transactions
        WHERE type = 'EXPENSE' AND dateMillis >= :startOfMonth AND dateMillis < :endOfMonth
    """)
    fun getMonthlyExpense(startOfMonth: Long, endOfMonth: Long): Flow<Long>

    /**
     * 按分类统计支出金额
     * @param startOfMonth 月初毫秒时间戳
     * @param endOfMonth 月末毫秒时间戳
     * @return 分类支出列表（包含分类名和金额）
     */
    @Query("""
        SELECT category, SUM(amountCents) as totalAmount
        FROM transactions
        WHERE type = 'EXPENSE' AND dateMillis >= :startOfMonth AND dateMillis < :endOfMonth
        GROUP BY category
        ORDER BY totalAmount DESC
    """)
    fun getExpenseByCategory(startOfMonth: Long, endOfMonth: Long): Flow<List<CategoryTotal>>

    /**
     * 获取交易记录数量
     * @return 记录数量
     */
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    /**
     * 清空所有交易记录（谨慎使用）
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}

/**
 * 分类汇总数据类（用于统计查询结果）
 */
data class CategoryTotal(
    val category: String,
    val totalAmount: Long
)
