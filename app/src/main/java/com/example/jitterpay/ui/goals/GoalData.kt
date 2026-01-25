package com.example.jitterpay.ui.goals

/**
 * 目标数据类 - UI层使用的数据模型
 *
 * 用于在UI层展示目标信息，不直接与数据库实体绑定。
 * 从GoalEntity转换而来。
 */
data class GoalData(
    val id: Long,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val iconType: GoalIconType
) {
    val progress: Float
        get() = if (targetAmount > 0.0) {
            (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

    val isCompleted: Boolean
        get() = currentAmount >= targetAmount

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
}

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
