package com.example.jitterpay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 预算周期类型枚举
 */
enum class BudgetPeriodType {
    DAILY,      // 每日预算
    WEEKLY,     // 每周预算
    MONTHLY,    // 每月预算
    YEARLY      // 每年预算
}

/**
 * 预算实体类 - 用于存储用户预算设置
 *
 * 字段设计说明：
 * - id: 主键，自增长
 * - title: 预算标题
 * - amountCents: 预算金额（分），避免浮点数精度问题
 * - periodType: 预算周期类型
 * - startDate: 预算开始日期（时间戳）
 * - endDate: 预算结束日期（时间戳，可选）
 * - notifyAt80: 是否在达到80%时通知
 * - notifyAt90: 是否在达到90%时通知
 * - notifyAt100: 是否在达到100%时通知
 * - isActive: 预算是否激活
 * - createdAt: 创建时间戳
 * - updatedAt: 更新时间戳
 */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amountCents: Long,
    val periodType: String,
    val startDate: Long,
    val endDate: Long? = null,
    val notifyAt80: Boolean = true,
    val notifyAt90: Boolean = true,
    val notifyAt100: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取格式化的预算金额
     */
    fun getFormattedAmount(): String {
        val dollars = amountCents / 100.0
        return String.format("%.2f", dollars)
    }

    /**
     * 计算当前周期的开始和结束时间
     */
    fun getCurrentPeriodRange(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now

        return when (BudgetPeriodType.valueOf(periodType)) {
            BudgetPeriodType.DAILY -> {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                val end = calendar.timeInMillis - 1

                Pair(start, end)
            }
            BudgetPeriodType.WEEKLY -> {
                calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                val end = calendar.timeInMillis - 1

                Pair(start, end)
            }
            BudgetPeriodType.MONTHLY -> {
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.add(java.util.Calendar.MONTH, 1)
                val end = calendar.timeInMillis - 1

                Pair(start, end)
            }
            BudgetPeriodType.YEARLY -> {
                calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.add(java.util.Calendar.YEAR, 1)
                val end = calendar.timeInMillis - 1

                Pair(start, end)
            }
        }
    }

    companion object {
        /**
         * 将金额字符串转换为存储格式（分）
         */
        fun parseAmountToCents(amountString: String): Long {
            val dollars = amountString.toDoubleOrNull() ?: 0.0
            return (dollars * 100).toLong()
        }

        /**
         * 将 Double 金额转换为存储格式（分）
         */
        fun parseAmountToCents(amount: Double): Long {
            return (amount * 100).toLong()
        }
    }
}
