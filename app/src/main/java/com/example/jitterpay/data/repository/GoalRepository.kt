package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.dao.GoalDao
import com.example.jitterpay.data.local.dao.GoalTransactionDao
import com.example.jitterpay.data.local.entity.GoalEntity
import com.example.jitterpay.data.local.entity.GoalTransactionEntity
import com.example.jitterpay.data.local.entity.GoalTransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 目标数据仓库
 *
 * 封装所有目标相关的数据库操作，提供统一的数据访问接口。
 * 使用Repository模式隔离数据源，方便后续替换数据实现。
 *
 * 所有金额单位说明：
 * - 内部存储和计算使用"分"（cents）为单位
 * - 对外暴露的方法使用Long类型表示金额（分）
 */
@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val goalTransactionDao: GoalTransactionDao
) {

    // ==================== 目标 CRUD 操作 ====================

    /**
     * 创建新目标
     *
     * @param title 目标标题
     * @param targetAmountCents 目标金额（分）
     * @param iconType 图标类型
     * @return 创建的目标ID
     */
    suspend fun createGoal(
        title: String,
        targetAmountCents: Long,
        iconType: String
    ): Long {
        val entity = GoalEntity(
            title = title,
            targetAmountCents = targetAmountCents,
            currentAmountCents = 0L,
            iconType = iconType,
            isCompleted = false
        )
        return goalDao.insertGoal(entity)
    }

    /**
     * 更新目标
     */
    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }

    /**
     * 删除目标
     */
    suspend fun deleteGoal(goal: GoalEntity) {
        goalDao.deleteGoal(goal)
    }

    /**
     * 根据ID删除目标
     */
    suspend fun deleteGoalById(goalId: Long) {
        goalDao.deleteGoalById(goalId)
    }

    // ==================== 目标查询操作 ====================

    /**
     * 获取所有目标（按创建时间降序）
     */
    fun getAllGoals(): Flow<List<GoalEntity>> {
        return goalDao.getAllGoals()
    }

    /**
     * 根据ID获取目标
     */
    suspend fun getGoalById(goalId: Long): GoalEntity? {
        return goalDao.getGoalById(goalId)
    }

    /**
     * 根据ID获取目标（Flow版本）
     */
    fun getGoalByIdFlow(goalId: Long): Flow<GoalEntity?> {
        return goalDao.getGoalByIdFlow(goalId)
    }

    /**
     * 获取所有已完成的目标
     */
    fun getCompletedGoals(): Flow<List<GoalEntity>> {
        return goalDao.getCompletedGoals()
    }

    /**
     * 获取所有进行中的目标
     */
    fun getActiveGoals(): Flow<List<GoalEntity>> {
        return goalDao.getActiveGoals()
    }

    // ==================== 目标资金操作 ====================

    /**
     * 向目标存入资金
     *
     * @param goalId 目标ID
     * @param amountCents 存入金额（分）
     * @param description 交易描述
     * @return 目标的当前金额
     */
    suspend fun addFundsToGoal(
        goalId: Long,
        amountCents: Long,
        description: String = ""
    ): Long {
        // 获取当前目标
        val goal = goalDao.getGoalById(goalId) ?: throw IllegalArgumentException("Goal not found")

        // 更新目标当前金额
        val newAmount = goal.currentAmountCents + amountCents
        val isCompleted = newAmount >= goal.targetAmountCents

        val updatedGoal = goal.copy(
            currentAmountCents = newAmount,
            isCompleted = isCompleted,
            updatedAt = System.currentTimeMillis()
        )
        goalDao.updateGoal(updatedGoal)

        // 记录交易
        val transaction = GoalTransactionEntity(
            goalId = goalId,
            type = GoalTransactionType.DEPOSIT.name,
            amountCents = amountCents,
            description = description,
            dateMillis = System.currentTimeMillis()
        )
        goalTransactionDao.insertGoalTransaction(transaction)

        return newAmount
    }

    /**
     * 从目标取出资金
     *
     * @param goalId 目标ID
     * @param amountCents 取出金额（分）
     * @param description 交易描述
     * @return 目标的当前金额
     */
    suspend fun withdrawFromGoal(
        goalId: Long,
        amountCents: Long,
        description: String = ""
    ): Long {
        // 获取当前目标
        val goal = goalDao.getGoalById(goalId) ?: throw IllegalArgumentException("Goal not found")

        // 检查是否有足够金额
        if (goal.currentAmountCents < amountCents) {
            throw IllegalArgumentException("Insufficient funds in goal")
        }

        // 更新目标当前金额
        val newAmount = goal.currentAmountCents - amountCents
        val isCompleted = newAmount >= goal.targetAmountCents

        val updatedGoal = goal.copy(
            currentAmountCents = newAmount,
            isCompleted = isCompleted,
            updatedAt = System.currentTimeMillis()
        )
        goalDao.updateGoal(updatedGoal)

        // 记录交易
        val transaction = GoalTransactionEntity(
            goalId = goalId,
            type = GoalTransactionType.WITHDRAWAL.name,
            amountCents = amountCents,
            description = description,
            dateMillis = System.currentTimeMillis()
        )
        goalTransactionDao.insertGoalTransaction(transaction)

        return newAmount
    }

    // ==================== 目标交易查询 ====================

    /**
     * 获取目标的所有交易记录
     *
     * @param goalId 目标ID
     * @return 交易记录Flow
     */
    fun getTransactionsByGoalId(goalId: Long): Flow<List<GoalTransactionEntity>> {
        return goalTransactionDao.getTransactionsByGoalId(goalId)
    }

    /**
     * 删除目标交易记录
     */
    suspend fun deleteGoalTransaction(transaction: GoalTransactionEntity) {
        goalTransactionDao.deleteGoalTransaction(transaction)
    }

    // ==================== 统计操作 ====================

    /**
     * 获取目标总数
     */
    suspend fun getGoalCount(): Int {
        return goalDao.getGoalCount()
    }

    /**
     * 获取已完成目标数量
     */
    suspend fun getCompletedGoalCount(): Int {
        return goalDao.getCompletedGoalCount()
    }

    /**
     * 获取所有目标的总目标金额
     *
     * @return 总目标金额（分）
     */
    suspend fun getTotalTargetAmount(): Long {
        return goalDao.getTotalTargetAmount()
    }

    /**
     * 获取所有目标的总当前金额
     *
     * @return 总当前金额（分）
     */
    suspend fun getTotalCurrentAmount(): Long {
        return goalDao.getTotalCurrentAmount()
    }

    /**
     * 计算所有目标的总进度
     *
     * @return 总进度百分比 (0.0 ~ 1.0)
     */
    fun getTotalProgress(): Flow<Float> {
        return goalDao.getAllGoals().map { goals ->
            if (goals.isEmpty()) return@map 0f

            val totalTarget = goals.sumOf { it.targetAmountCents }
            val totalCurrent = goals.sumOf { it.currentAmountCents }

            if (totalTarget == 0L) return@map 0f
            return@map (totalCurrent.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f)
        }
    }
}
