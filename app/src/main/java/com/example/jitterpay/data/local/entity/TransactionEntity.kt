package com.example.jitterpay.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

/**
 * 交易类型枚举
 */
enum class TransactionType {
    EXPENSE,
    INCOME
}

/**
 * 交易状态枚举
 */
enum class TransactionStatus {
    PENDING,
    COMPLETED,
    RECEIVED,
    FAILED
}

/**
 * 交易实体类 - 用于存储记账数据
 *
 * 字段设计说明：
 * - id: 主键，自增长
 * - type: 交易类型（收入/支出），使用字符串存储便于查询和索引
 * - amountCents: 金额以分为单位存储，避免浮点数精度问题
 * - category: 分类名称，与UI层CategoryGrid保持一致
 * - description: 交易描述/备注
 * - dateMillis: 交易日期（epoch milliseconds），支持精确排序和筛选
 * - status: 交易状态
 * - createdAt: 创建时间戳，用于审计和同步
 * - updatedAt: 更新时间戳，支持数据迁移和冲突检测
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val amountCents: Long,
    val category: String,
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val status: String = TransactionStatus.COMPLETED.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 将存储的金额（分）转换为可读金额字符串
     */
    fun getFormattedAmount(): String {
        // 使用整数运算避免浮点数精度问题
        val wholePart = amountCents / 100
        val decimalPart = amountCents % 100

        // 格式化整数部分（添加逗号分隔）
        val wholePartFormatted = NumberFormat.getNumberInstance(Locale.US).format(wholePart)
        val decimalPartFormatted = String.format(Locale.US, "%02d", decimalPart)
        val formattedAmount = "$wholePartFormatted.$decimalPartFormatted"

        return if (type == TransactionType.INCOME.name) {
            "+$$formattedAmount"
        } else {
            "-$$formattedAmount"
        }
    }

    /**
     * 将金额字符串（"0.00"格式）转换为存储格式（分）
     */
    companion object {
        fun parseAmountToCents(amountString: String): Long {
            val cleaned = amountString.replace(".", "")
            return cleaned.toLongOrNull() ?: 0L
        }
    }
}
