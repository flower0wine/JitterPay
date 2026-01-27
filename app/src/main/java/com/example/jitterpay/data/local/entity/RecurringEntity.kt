package com.example.jitterpay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 定时记账实体类 - 用于存储周期性交易记录
 *
 * 字段设计说明：
 * - id: 主键，自增长
 * - title: 记账标题/描述
 * - amountCents: 金额（分），避免浮点数精度问题，与TransactionEntity保持一致
 * - type: 交易类型（收入/支出），使用字符串存储便于查询和索引
 * - category: 分类名称，与TransactionEntity保持一致
 * - frequency: 执行频率，使用字符串存储（对应UI层RecurringFrequency枚举）
 * - startDateMillis: 首次执行日期（毫秒）
 * - nextExecutionDateMillis: 下次执行日期（毫秒），用于快速查询和显示
 * - isActive: 是否激活，用于启用/暂停定时记账
 * - estimatedMonthlyAmount: 预估月金额（分），用于UI展示，避免每次计算
 * - createdAt: 创建时间戳，用于审计和同步
 * - updatedAt: 更新时间戳，支持数据迁移和冲突检测
 */
@Entity(tableName = "recurring_transactions")
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amountCents: Long,
    val type: String,
    val category: String,
    val frequency: String,
    val startDateMillis: Long,
    val nextExecutionDateMillis: Long,
    val isActive: Boolean = true,
    val estimatedMonthlyAmount: Long,
    val reminderEnabled: Boolean = false,
    val reminderDaysBefore: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 计算指定频率的预估月金额
         * @param amountCents 单次金额（分）
         * @param frequency 执行频率（字符串）
         * @return 预估月金额（分）
         */
        fun calculateEstimatedMonthlyAmount(amountCents: Long, frequency: String): Long {
            val multiplier = when (frequency.uppercase()) {
                "DAILY" -> 30L       // 每月约30天
                "WEEKLY" -> 4L       // 每月约4周
                "BIWEEKLY" -> 2L     // 每两周一次
                "MONTHLY" -> 1L      // 每月一次
                "YEARLY" -> amountCents / 12  // 每年一次，约为1/12月（这里直接返回计算结果）
                else -> 1L           // 未知频率，假设每月一次
            }

            // 对于 YEARLY，multiplier 已经是计算结果，不需要再乘
            // 对于其他频率，需要将 amountCents 乘以 multiplier
            return if (frequency.uppercase() == "YEARLY") multiplier else amountCents * multiplier
        }

        /**
         * 计算下次执行日期
         * @param startDateMillis 起始日期（毫秒）
         * @param frequency 执行频率（字符串）
         * @return 下次执行日期（毫秒）
         */
        fun calculateNextExecutionDate(startDateMillis: Long, frequency: String): Long {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = startDateMillis

            when (frequency.uppercase()) {
                "DAILY" -> calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                "BIWEEKLY" -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 2)
                "MONTHLY" -> calendar.add(java.util.Calendar.MONTH, 1)
                "YEARLY" -> calendar.add(java.util.Calendar.YEAR, 1)
            }

            return calendar.timeInMillis
        }

        /**
         * 将金额字符串（"0.00"格式）转换为存储格式（分）
         */
        fun parseAmountToCents(amountString: String): Long {
            return try {
                val amount = amountString.toDoubleOrNull() ?: 0.0
                (amount * 100).toLong()
            } catch (e: Exception) {
                0L
            }
        }
    }

    /**
     * 将存储的金额（分）转换为可读金额字符串
     */
    fun getFormattedAmount(): String {
        val amount = amountCents.toDouble() / 100.0
        val sign = when (type) {
            "INCOME" -> "+"
            "EXPENSE" -> "-"
            else -> ""
        }
        val wholePart = kotlin.math.abs((amountCents / 100).toInt())
        val decimalPart = kotlin.math.abs(amountCents % 100)
        // Use US locale for comma separators
        val usLocale = java.util.Locale.US
        val numberFormat = java.text.NumberFormat.getIntegerInstance(usLocale)
        numberFormat.isGroupingUsed = true
        val formattedWholePart = numberFormat.format(wholePart)
        val decimalStr = decimalPart.toString().padStart(2, '0')
        return "$sign\$$formattedWholePart.$decimalStr"
    }
}
