package com.example.jitterpay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.jitterpay.data.local.entity.GoalTransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 目标交易数据访问对象 (DAO)

 * 提供所有目标交易数据的CRUD操作，使用Flow实现响应式数据流。
 * 所有查询操作都返回Flow以支持Compose的响应式UI更新。
 */
@Dao
interface GoalTransactionDao {

    /**
     * 插入单条目标交易记录
     * @param transaction 目标交易实体
     * @return 插入记录的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalTransaction(transaction: GoalTransactionEntity): Long

    /**
     * 批量插入目标交易记录
     * @param transactions 目标交易实体列表
     * @return 插入记录ID列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalTransactions(transactions: List<GoalTransactionEntity>): List<Long>

    /**
     * 删除目标交易记录
     * @param transaction 目标交易实体
     */
    @Delete
    suspend fun deleteGoalTransaction(transaction: GoalTransactionEntity)

    /**
     * 根据ID删除目标交易记录
     * @param id 交易ID
     */
    @Query("DELETE FROM goal_transactions WHERE id = :id")
    suspend fun deleteGoalTransactionById(id: Long)

    /**
     * 删除指定目标的所有交易记录
     * @param goalId 目标ID
     */
    @Query("DELETE FROM goal_transactions WHERE goalId = :goalId")
    suspend fun deleteTransactionsByGoalId(goalId: Long)

    /**
     * 获取所有目标交易记录（按日期降序排序）
     * @return 目标交易实体Flow
     */
    @Query("SELECT * FROM goal_transactions ORDER BY dateMillis DESC")
    fun getAllGoalTransactions(): Flow<List<GoalTransactionEntity>>

    /**
     * 根据ID获取目标交易记录
     * @param id 交易ID
     * @return 目标交易实体
     */
    @Query("SELECT * FROM goal_transactions WHERE id = :id")
    suspend fun getGoalTransactionById(id: Long): GoalTransactionEntity?

    /**
     * 获取指定目标的所有交易记录（按日期降序排序）
     * @param goalId 目标ID
     * @return 目标交易实体Flow
     */
    @Query("SELECT * FROM goal_transactions WHERE goalId = :goalId ORDER BY dateMillis DESC")
    fun getTransactionsByGoalId(goalId: Long): Flow<List<GoalTransactionEntity>>

    /**
     * 获取指定目标的存入记录总和
     * @param goalId 目标ID
     * @return 存入金额总和（分）
     */
    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM goal_transactions WHERE goalId = :goalId AND type = 'DEPOSIT'")
    suspend fun getTotalDepositAmount(goalId: Long): Long

    /**
     * 获取指定目标的取出记录总和
     * @param goalId 目标ID
     * @return 取出金额总和（分）
     */
    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM goal_transactions WHERE goalId = :goalId AND type = 'WITHDRAWAL'")
    suspend fun getTotalWithdrawalAmount(goalId: Long): Long

    /**
     * 获取指定目标的净存入金额（存入 - 取出）
     * @param goalId 目标ID
     * @return 净存入金额（分）
     */
    @Query("""
        SELECT COALESCE(
            (SELECT COALESCE(SUM(amountCents), 0) FROM goal_transactions WHERE goalId = :goalId AND type = 'DEPOSIT') -
            (SELECT COALESCE(SUM(amountCents), 0) FROM goal_transactions WHERE goalId = :goalId AND type = 'WITHDRAWAL'),
            0
        )
    """)
    suspend fun getNetAmount(goalId: Long): Long

    /**
     * 获取指定日期范围内的目标交易记录
     * @param startDateMillis 起始日期（毫秒）
     * @param endDateMillis 结束日期（毫秒）
     * @return 目标交易实体Flow
     */
    @Query("SELECT * FROM goal_transactions WHERE dateMillis BETWEEN :startDateMillis AND :endDateMillis ORDER BY dateMillis DESC")
    fun getTransactionsByDateRange(startDateMillis: Long, endDateMillis: Long): Flow<List<GoalTransactionEntity>>

    /**
     * 获取指定日期范围内指定目标的交易记录
     * @param goalId 目标ID
     * @param startDateMillis 起始日期（毫秒）
     * @param endDateMillis 结束日期（毫秒）
     * @return 目标交易实体Flow
     */
    @Query("SELECT * FROM goal_transactions WHERE goalId = :goalId AND dateMillis BETWEEN :startDateMillis AND :endDateMillis ORDER BY dateMillis DESC")
    fun getTransactionsByGoalIdAndDateRange(goalId: Long, startDateMillis: Long, endDateMillis: Long): Flow<List<GoalTransactionEntity>>

    /**
     * 获取目标交易记录数量
     * @return 记录数量
     */
    @Query("SELECT COUNT(*) FROM goal_transactions")
    suspend fun getTransactionCount(): Int

    /**
     * 获取指定目标的交易记录数量
     * @param goalId 目标ID
     * @return 记录数量
     */
    @Query("SELECT COUNT(*) FROM goal_transactions WHERE goalId = :goalId")
    suspend fun getTransactionCountByGoalId(goalId: Long): Int

    /**
     * 清空所有目标交易记录（谨慎使用）
     */
    @Query("DELETE FROM goal_transactions")
    suspend fun deleteAllGoalTransactions()
}
