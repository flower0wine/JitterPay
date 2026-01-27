package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.dao.RecurringDao
import com.example.jitterpay.data.local.entity.RecurringEntity
import com.example.jitterpay.ui.recurring.RecurringFrequency
import com.example.jitterpay.ui.recurring.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 定时记账数据仓库
 *
 * 封装所有定时记账相关的数据库操作，提供统一的数据访问接口。
 * 使用Repository模式隔离数据源，方便后续替换数据实现。
 *
 * 负责Entity与UI层RecurringTransaction之间的转换。
 *
 * 所有金额单位说明：
 * - 内部存储和计算使用"分"（cents）为单位
 * - 对外暴露的方法使用Long类型表示金额（分）
 */
@Singleton
class RecurringRepository @Inject constructor(
    private val recurringDao: RecurringDao
) {

    /**
     * 添加定时记账记录
     *
     * @param title 标题
     * @param amountCents 金额（分）
     * @param type 交易类型（INCOME/EXPENSE）
     * @param category 分类
     * @param frequency 执行频率
     * @param startDateMillis 起始日期（毫秒）
     * @param reminderEnabled 是否启用提醒
     * @param reminderDaysBefore 提前几天提醒
     * @return 插入记录的ID
     */
    suspend fun addRecurring(
        title: String,
        amountCents: Long,
        type: String,
        category: String,
        frequency: String,
        startDateMillis: Long,
        reminderEnabled: Boolean = false,
        reminderDaysBefore: Int = 0
    ): Long {
        val nextExecutionDate = RecurringEntity.calculateNextExecutionDate(startDateMillis, frequency)
        val estimatedMonthly = RecurringEntity.calculateEstimatedMonthlyAmount(amountCents, frequency)

        val entity = RecurringEntity(
            title = title,
            amountCents = amountCents,
            type = type,
            category = category,
            frequency = frequency,
            startDateMillis = startDateMillis,
            nextExecutionDateMillis = nextExecutionDate,
            estimatedMonthlyAmount = estimatedMonthly,
            reminderEnabled = reminderEnabled,
            reminderDaysBefore = reminderDaysBefore
        )

        return recurringDao.insertRecurring(entity)
    }

    /**
     * 更新定时记账记录
     *
     * @param id 记录ID
     * @param title 标题
     * @param amountCents 金额（分）
     * @param type 交易类型（INCOME/EXPENSE）
     * @param category 分类
     * @param frequency 执行频率
     * @param startDateMillis 起始日期（毫秒）
     * @param reminderEnabled 是否启用提醒
     * @param reminderDaysBefore 提前几天提醒
     */
    suspend fun updateRecurring(
        id: Long,
        title: String,
        amountCents: Long,
        type: String,
        category: String,
        frequency: String,
        startDateMillis: Long,
        reminderEnabled: Boolean = false,
        reminderDaysBefore: Int = 0
    ) {
        val existing = recurringDao.getById(id) ?: return

        // Recalculate derived values
        // If start date or frequency changed, we might need to recalculate next execution date.
        // For simplicity in this edit, we'll reset next execution date based on new start date and frequency
        // This effectively "resets" the schedule if edited.
        // A more complex logic might try to keep the relative progress, but resetting is safer for user intent when editing parameters.
        val nextExecutionDate = RecurringEntity.calculateNextExecutionDate(startDateMillis, frequency)
        val estimatedMonthly = RecurringEntity.calculateEstimatedMonthlyAmount(amountCents, frequency)

        val updatedEntity = existing.copy(
            title = title,
            amountCents = amountCents,
            type = type,
            category = category,
            frequency = frequency,
            startDateMillis = startDateMillis,
            nextExecutionDateMillis = nextExecutionDate,
            estimatedMonthlyAmount = estimatedMonthly,
            reminderEnabled = reminderEnabled,
            reminderDaysBefore = reminderDaysBefore,
            updatedAt = System.currentTimeMillis()
        )

        recurringDao.updateRecurring(updatedEntity)
    }

    /**
     * 更新定时记账记录
     */
    suspend fun updateRecurring(entity: RecurringEntity) {
        recurringDao.updateRecurring(
            entity.copy(updatedAt = System.currentTimeMillis())
        )
    }

    /**
     * 删除定时记账记录
     */
    suspend fun deleteRecurring(entity: RecurringEntity) {
        recurringDao.deleteRecurring(entity)
    }

    /**
     * 根据ID删除定时记账记录
     */
    suspend fun deleteById(id: Long) {
        recurringDao.deleteById(id)
    }

    /**
     * 获取所有定时记账记录（按下次执行日期排序）
     */
    fun getAllRecurring(): Flow<List<RecurringTransaction>> {
        return recurringDao.getAllRecurring().map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    /**
     * 获取所有激活的定时记账记录
     */
    fun getActiveRecurring(): Flow<List<RecurringTransaction>> {
        return recurringDao.getActiveRecurring().map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    /**
     * 根据ID获取定时记账记录
     */
    suspend fun getById(id: Long): RecurringTransaction? {
        return recurringDao.getById(id)?.toUiModel()
    }

    /**
     * 根据ID获取定时记账实体（包含所有字段）
     */
    suspend fun getByIdEntity(id: Long): RecurringEntity? {
        return recurringDao.getById(id)
    }

    /**
     * 根据ID获取定时记账记录（Flow版本）
     */
    fun getByIdFlow(id: Long): Flow<RecurringTransaction?> {
        return recurringDao.getByIdFlow(id).map { it?.toUiModel() }
    }

    /**
     * 根据类型获取定时记账记录
     */
    fun getByType(type: String): Flow<List<RecurringTransaction>> {
        return recurringDao.getByType(type).map { entities ->
            entities.map { it.toUiModel() }
        }
    }

    /**
     * 切换激活状态
     */
    suspend fun toggleActive(id: Long) {
        val entity = recurringDao.getById(id) ?: return
        recurringDao.setActive(id, !entity.isActive, System.currentTimeMillis())
    }

    /**
     * 设置激活状态
     */
    suspend fun setActive(id: Long, isActive: Boolean) {
        recurringDao.setActive(id, isActive, System.currentTimeMillis())
    }

    /**
     * 获取需要执行的任务列表
     */
    suspend fun getDueRecurring(): List<RecurringEntity> {
        return recurringDao.getDueRecurring(System.currentTimeMillis())
    }

    /**
     * 执行到期的定时记账并更新下次执行日期
     * @param dueList 到期的定时记账列表
     */
    suspend fun executeAndAdvance(dueList: List<RecurringEntity>) {
        if (dueList.isEmpty()) return

        val ids = dueList.map { it.id }
        val nextDate = dueList.firstOrNull()?.let { entity ->
            RecurringEntity.calculateNextExecutionDate(entity.nextExecutionDateMillis, entity.frequency)
        } ?: System.currentTimeMillis()

        recurringDao.updateNextExecutionDates(ids, nextDate, System.currentTimeMillis())
    }

    /**
     * 获取总预估月支出
     */
    fun getTotalEstimatedMonthlyExpense(): Flow<Long> {
        return recurringDao.getTotalEstimatedMonthlyExpense()
    }

    /**
     * 获取总预估月收入
     */
    fun getTotalEstimatedMonthlyIncome(): Flow<Long> {
        return recurringDao.getTotalEstimatedMonthlyIncome()
    }

    /**
     * 获取记录数量
     */
    suspend fun getCount(): Int {
        return recurringDao.getCount()
    }

    /**
     * 获取激活记录数量
     */
    suspend fun getActiveCount(): Int {
        return recurringDao.getActiveCount()
    }

    /**
     * 清空所有定时记账记录（谨慎使用）
     */
    suspend fun deleteAll() {
        recurringDao.deleteAll()
    }

    /**
     * 将Entity转换为UI层使用的RecurringTransaction
     */
    private fun RecurringEntity.toUiModel(): RecurringTransaction {
        return RecurringTransaction(
            id = id,
            title = title,
            category = category,
            amount = amountCents,
            frequency = RecurringFrequency.valueOf(frequency.uppercase()),
            nextExecutionDate = nextExecutionDateMillis,
            isActive = isActive,
            type = type,
            estimatedMonthlyAmount = estimatedMonthlyAmount
        )
    }
}
