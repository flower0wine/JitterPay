package com.example.jitterpay.data.local.entity

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 目标图标类型枚举
 */
enum class GoalIconType {
    SHIELD,
    FLIGHT,
    LAPTOP,
    HOME,
    CAR,
    EDUCATION,
    HEALTH,
    GIFT
}

/**
 * 目标实体类 - 用于存储用户财务目标

 * 字段设计说明：
 * - id: 主键，自增长
 * - title: 目标标题
 * - targetAmountCents: 目标金额（分），避免浮点数精度问题
 * - currentAmountCents: 当前已存金额（分）
 * - iconType: 目标图标类型，使用字符串存储便于查询和索引
 * - createdAt: 创建时间戳，用于审计和同步
 * - updatedAt: 更新时间戳，支持数据迁移和冲突检测
 * - isCompleted: 目标是否已完成，用于快速查询筛选
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmountCents: Long,
    val currentAmountCents: Long,
    val iconType: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isCompleted: Boolean = false
) {
    /**
     * 计算完成进度比例（0.0 ~ 1.0）
     */
    fun getProgress(): Float {
        if (targetAmountCents == 0L) return 0f
        return (currentAmountCents.toFloat() / targetAmountCents.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * 判断目标是否已完成
     */
    fun checkIsCompleted(): Boolean {
        return currentAmountCents >= targetAmountCents
    }

    /**
     * 获取剩余需要存入的金额（分）
     */
    fun getRemainingAmountCents(): Long {
        return (targetAmountCents - currentAmountCents).coerceAtLeast(0L)
    }

    /**
     * 将存储的金额（分）转换为可读金额字符串
     */
    fun getFormattedTargetAmount(): String {
        val dollars = targetAmountCents / 100.0
        val dollarsString = String.format("%.2f", dollars)
        return "$${dollarsString}"
    }

    /**
     * 将存储的当前金额（分）转换为可读金额字符串
     */
    fun getFormattedCurrentAmount(): String {
        val dollars = currentAmountCents / 100.0
        val dollarsString = String.format("%.2f", dollars)
        return "$${dollarsString}"
    }

    /**
     * 将存储的剩余金额（分）转换为可读金额字符串
     */
    fun getFormattedRemainingAmount(): String {
        val dollars = getRemainingAmountCents() / 100.0
        val dollarsString = String.format("%.2f", dollars)
        return "$${dollarsString}"
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
