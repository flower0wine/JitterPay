package com.example.jitterpay.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 金额值对象 - 封装所有金额相关操作
 *
 * 设计原则：
 * - 内部使用 long (cents) 存储，避免浮点数精度问题
 * - 所有运算保持整数精度
 * - 提供安全的转换方法
 */
@JvmInline
value class Money private constructor(private val cents: Long) {

    companion object {
        /**
         * 从分创建 Money 对象
         */
        fun fromCents(cents: Long): Money = Money(cents)

        /**
         * 从 BigDecimal 创建 Money 对象
         * 自动四舍五入到两位小数
         */
        fun fromBigDecimal(amount: BigDecimal): Money {
            val cents = amount
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .toLong()
            return Money(cents)
        }

        /**
         * 从字符串解析 Money 对象
         * 支持格式： "5", "5.00", "1,234.56", ".99", "0.99"
         */
        fun parse(input: String): Money? {
            val cleaned = input
                .replace(",", "")
                .trim()
                .ifEmpty { return null }

            val bd = cleaned.toBigDecimalOrNull() ?: return null
            return fromBigDecimal(bd)
        }

        /**
         * 零金额
         */
        val ZERO: Money = Money(0)
    }

    /**
     * 获取分表示
     */
    fun toCents(): Long = cents

    /**
     * 获取 BigDecimal 表示
     */
    fun toBigDecimal(): BigDecimal =
        BigDecimal(cents).divide(BigDecimal(100), 2, RoundingMode.UNNECESSARY)

    /**
     * 格式化为显示字符串 (如 "123.45")
     */
    fun format(): String {
        val wholePart = kotlin.math.abs(cents / 100)
        val decimalPart = kotlin.math.abs(cents % 100)
        return "$wholePart.${decimalPart.toString().padStart(2, '0')}"
    }

    /**
     * 格式化为简化的显示字符串，移除不必要的尾随零
     * 例如：
     * - 2200 cents -> "22" (而不是 "22.00")
     * - 550 cents -> "5.5" (而不是 "5.50")
     * - 2245 cents -> "22.45" (保持两位小数)
     */
    fun formatSimplified(): String {
        val wholePart = kotlin.math.abs(cents / 100)
        val decimalPart = kotlin.math.abs(cents % 100)
        
        return when {
            decimalPart == 0L -> wholePart.toString()
            decimalPart % 10 == 0L -> "$wholePart.${decimalPart / 10}"
            else -> "$wholePart.${decimalPart.toString().padStart(2, '0')}"
        }
    }

    /**
     * 格式化为货币字符串 (如 "$123.45")
     */
    fun formatCurrency(): String = "$${format()}"

    /**
     * 格式化为带符号的金额字符串
     */
    fun formatWithSign(isIncome: Boolean): String {
        val sign = if (isIncome) "+" else "-"
        return "$sign$${format()}"
    }

    /**
     * 加法
     */
    operator fun plus(other: Money): Money = Money(cents + other.cents)

    /**
     * 减法
     */
    operator fun minus(other: Money): Money = Money(cents - other.cents)

    /**
     * 乘法 (用于汇率转换等)
     */
    operator fun times(multiplier: BigDecimal): Money {
        val result = toBigDecimal().multiply(multiplier)
        return fromBigDecimal(result)
    }

    /**
     * 判断是否大于零
     */
    fun isPositive(): Boolean = cents > 0

    /**
     * 判断是否为零
     */
    fun isZero(): Boolean = cents == 0L

    /**
     * 判断是否小于零
     */
    fun isNegative(): Boolean = cents < 0

    override fun toString(): String = "Money(cents=$cents, display=${format()})"
}
