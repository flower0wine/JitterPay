package com.example.jitterpay.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.jitterpay.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * 目标数据访问对象 (DAO)

 * 提供所有目标数据的CRUD操作，使用Flow实现响应式数据流。
 * 所有查询操作都返回Flow以支持Compose的响应式UI更新。
 */
@Dao
interface GoalDao {

    /**
     * 插入单条目标记录
     * @param goal 目标实体
     * @return 插入记录的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    /**
     * 批量插入目标记录
     * @param goals 目标实体列表
     * @return 插入记录ID列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>): List<Long>

    /**
     * 更新目标记录
     * @param goal 目标实体
     */
    @Update
    suspend fun updateGoal(goal: GoalEntity)

    /**
     * 删除目标记录
     * @param goal 目标实体
     */
    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    /**
     * 根据ID删除目标记录
     * @param id 目标ID
     */
    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    /**
     * 获取所有目标记录（按创建时间降序排序）
     * @return 目标实体Flow
     */
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    /**
     * 根据ID获取目标记录
     * @param id 目标ID
     * @return 目标实体Flow
     */
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    /**
     * 根据ID获取目标记录（Flow版本）
     * @param id 目标ID
     * @return 目标实体Flow
     */
    @Query("SELECT * FROM goals WHERE id = :id")
    fun getGoalByIdFlow(id: Long): Flow<GoalEntity?>

    /**
     * 获取所有已完成的目标
     * @return 已完成目标实体Flow
     */
    @Query("SELECT * FROM goals WHERE isCompleted = 1 ORDER BY updatedAt DESC")
    fun getCompletedGoals(): Flow<List<GoalEntity>>

    /**
     * 获取所有进行中的目标
     * @return 进行中目标实体Flow
     */
    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    /**
     * 获取目标总数
     * @return 目标总数
     */
    @Query("SELECT COUNT(*) FROM goals")
    suspend fun getGoalCount(): Int

    /**
     * 获取已完成目标数量
     * @return 已完成目标数量
     */
    @Query("SELECT COUNT(*) FROM goals WHERE isCompleted = 1")
    suspend fun getCompletedGoalCount(): Int

    /**
     * 计算所有目标的目标金额总和
     * @return 目标金额总和（分）
     */
    @Query("SELECT COALESCE(SUM(targetAmountCents), 0) FROM goals")
    suspend fun getTotalTargetAmount(): Long

    /**
     * 计算所有目标的当前金额总和
     * @return 当前金额总和（分）
     */
    @Query("SELECT COALESCE(SUM(currentAmountCents), 0) FROM goals")
    suspend fun getTotalCurrentAmount(): Long

    /**
     * 清空所有目标记录（谨慎使用）
     */
    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()
}
