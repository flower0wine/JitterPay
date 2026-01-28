package com.example.jitterpay.data.local.dao

import androidx.room.*
import com.example.jitterpay.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 预算数据访问对象
 */
@Dao
interface BudgetDao {

    /**
     * 插入新预算
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    /**
     * 更新预算
     */
    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    /**
     * 删除预算
     */
    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    /**
     * 根据ID删除预算
     */
    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudgetById(budgetId: Long)

    /**
     * 获取所有预算（按创建时间降序）
     */
    @Query("SELECT * FROM budgets ORDER BY createdAt DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    /**
     * 获取所有激活的预算
     */
    @Query("SELECT * FROM budgets WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveBudgets(): Flow<List<BudgetEntity>>

    /**
     * 根据ID获取预算
     */
    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: Long): BudgetEntity?

    /**
     * 根据ID获取预算（Flow版本）
     */
    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    fun getBudgetByIdFlow(budgetId: Long): Flow<BudgetEntity?>

    /**
     * 获取预算总数
     */
    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun getBudgetCount(): Int

    /**
     * 获取激活的预算总数
     */
    @Query("SELECT COUNT(*) FROM budgets WHERE isActive = 1")
    suspend fun getActiveBudgetCount(): Int
}
