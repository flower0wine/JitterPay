package com.example.jitterpay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.jitterpay.data.local.entity.RecurringEntity
import kotlinx.coroutines.flow.Flow

/**
 * 定时记账数据访问对象 (DAO)
 *
 * 提供所有定时记账数据的CRUD操作，使用Flow实现响应式数据流。
 * 所有查询操作都返回Flow以支持Compose的响应式UI更新。
 */
@Dao
interface RecurringDao {

    /**
     * 插入单条定时记账记录
     * @param recurring 定时记账实体
     * @return 插入记录的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: RecurringEntity): Long

    /**
     * 批量插入定时记账记录
     * @param recurringList 定时记账实体列表
     * @return 插入记录ID列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recurringList: List<RecurringEntity>): List<Long>

    /**
     * 更新定时记账记录
     * @param recurring 定时记账实体
     */
    @Update
    suspend fun updateRecurring(recurring: RecurringEntity)

    /**
     * 删除定时记账记录
     * @param recurring 定时记账实体
     */
    @Delete
    suspend fun deleteRecurring(recurring: RecurringEntity)

    /**
     * 根据ID删除定时记账记录
     * @param id 记录ID
     */
    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 获取所有定时记账记录（按下次执行日期升序排序）
     * @return 定时记账实体Flow
     */
    @Query("SELECT * FROM recurring_transactions ORDER BY nextExecutionDateMillis ASC")
    fun getAllRecurring(): Flow<List<RecurringEntity>>

    /**
     * 获取所有激活的定时记账记录
     * @return 激活的定时记账实体Flow
     */
    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextExecutionDateMillis ASC")
    fun getActiveRecurring(): Flow<List<RecurringEntity>>

    /**
     * 根据ID获取定时记账记录
     * @param id 记录ID
     * @return 定时记账实体
     */
    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringEntity?

    /**
     * 根据ID获取定时记账记录（Flow版本）
     * @param id 记录ID
     * @return 定时记账实体Flow
     */
    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<RecurringEntity?>

    /**
     * 根据类型获取定时记账记录（收入或支出）
     * @param type 交易类型
     * @return 定时记账实体Flow
     */
    @Query("SELECT * FROM recurring_transactions WHERE type = :type ORDER BY nextExecutionDateMillis ASC")
    fun getByType(type: String): Flow<List<RecurringEntity>>

    /**
     * 根据分类获取定时记账记录
     * @param category 分类名称
     * @return 定时记账实体Flow
     */
    @Query("SELECT * FROM recurring_transactions WHERE category = :category ORDER BY nextExecutionDateMillis ASC")
    fun getByCategory(category: String): Flow<List<RecurringEntity>>

    /**
     * 获取需要执行的任务（下次执行日期已过且已激活）
     * @param currentTimeMillis 当前时间（毫秒）
     * @return 需要执行的定时记账实体列表
     */
    @Query("""
        SELECT * FROM recurring_transactions
        WHERE isActive = 1 AND nextExecutionDateMillis <= :currentTimeMillis
        ORDER BY nextExecutionDateMillis ASC
    """)
    suspend fun getDueRecurring(currentTimeMillis: Long): List<RecurringEntity>

    /**
     * 批量更新下次执行日期
     * @param ids 需要更新的记录ID列表
     * @param nextDateMillis 新的下次执行日期（毫秒）
     */
    @Query("""
        UPDATE recurring_transactions
        SET nextExecutionDateMillis = :nextDateMillis, updatedAt = :updatedAt
        WHERE id IN (:ids)
    """)
    suspend fun updateNextExecutionDates(ids: List<Long>, nextDateMillis: Long, updatedAt: Long)

    /**
     * 切换激活状态
     * @param id 记录ID
     * @param isActive 新的激活状态
     */
    @Query("""
        UPDATE recurring_transactions
        SET isActive = :isActive, updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun setActive(id: Long, isActive: Boolean, updatedAt: Long)

    /**
     * 获取定时记账记录数量
     * @return 记录数量
     */
    @Query("SELECT COUNT(*) FROM recurring_transactions")
    suspend fun getCount(): Int

    /**
     * 获取激活的定时记账记录数量
     * @return 激活记录数量
     */
    @Query("SELECT COUNT(*) FROM recurring_transactions WHERE isActive = 1")
    suspend fun getActiveCount(): Int

    /**
     * 计算激活记录的总预估月支出
     * @return 总预估月金额（分）
     */
    @Query("""
        SELECT COALESCE(SUM(estimatedMonthlyAmount), 0)
        FROM recurring_transactions
        WHERE isActive = 1 AND type = 'EXPENSE'
    """)
    fun getTotalEstimatedMonthlyExpense(): Flow<Long>

    /**
     * 计算激活记录的总预估月收入
     * @return 总预估月金额（分）
     */
    @Query("""
        SELECT COALESCE(SUM(estimatedMonthlyAmount), 0)
        FROM recurring_transactions
        WHERE isActive = 1 AND type = 'INCOME'
    """)
    fun getTotalEstimatedMonthlyIncome(): Flow<Long>

    /**
     * 清空所有定时记账记录（谨慎使用）
     */
    @Query("DELETE FROM recurring_transactions")
    suspend fun deleteAll()
}
