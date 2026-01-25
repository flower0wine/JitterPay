package com.example.jitterpay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 目标交易类型枚举
 */
enum class GoalTransactionType {
    DEPOSIT,
    WITHDRAWAL
}

/**
 * 目标交易实体类 - 用于记录目标相关的资金流动

 * 字段设计说明：
 * - id: 主键，自增长
 * - goalId: 关联的目标ID，外键关联到goals表
 * - type: 交易类型（存入/取出）
 * - amountCents: 交易金额（分），避免浮点数精度问题
 * - description: 交易描述/备注
 * - dateMillis: 交易日期（epoch milliseconds），支持精确排序和筛选
 * - createdAt: 创建时间戳，用于审计和同步
 *
 * 外键说明：
 * - goalId: 关联goals表的id，当目标被删除时，级联删除该目标的所有交易记录
 */
@Entity(
    tableName = "goal_transactions",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["goalId"]),
        Index(value = ["dateMillis"])
    ]
)
data class GoalTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val type: String,
    val amountCents: Long,
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 判断是否为存入交易
     */
    fun isDeposit(): Boolean {
        return type == GoalTransactionType.DEPOSIT.name
    }

    /**
     * 判断是否为取出交易
     */
    fun isWithdrawal(): Boolean {
        return type == GoalTransactionType.WITHDRAWAL.name
    }

    /**
     * 将存储的金额（分）转换为可读金额字符串
     * 存入显示"+"，取出显示"-"
     */
    fun getFormattedAmount(): String {
        val dollars = amountCents / 100.0
        val dollarsString = String.format("%.2f", dollars)
        val sign = if (isDeposit()) "+" else "-"
        return "$sign$${dollarsString}"
    }

    companion object {
        /**
         * 将金额字符串（"0.00"格式）转换为存储格式（分）
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
