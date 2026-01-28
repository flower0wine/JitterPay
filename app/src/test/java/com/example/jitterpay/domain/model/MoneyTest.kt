package com.example.jitterpay.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

/**
 * Money 单元测试
 *
 * 测试 Money 值对象的核心功能，包括：
 * - 格式化输出（标准格式和简化格式）
 * - 金额转换
 * - 运算操作
 */
class MoneyTest {

    @Test
    fun `formatSimplified removes trailing zeros for whole numbers`() {
        val money = Money.fromCents(2200L)
        assertEquals("22", money.formatSimplified())
    }

    @Test
    fun `formatSimplified removes single trailing zero`() {
        val money = Money.fromCents(550L)
        assertEquals("5.5", money.formatSimplified())
    }

    @Test
    fun `formatSimplified keeps both decimal places when needed`() {
        val money = Money.fromCents(2245L)
        assertEquals("22.45", money.formatSimplified())
    }

    @Test
    fun `formatSimplified handles zero amount`() {
        val money = Money.fromCents(0L)
        assertEquals("0", money.formatSimplified())
    }

    @Test
    fun `formatSimplified handles single cent`() {
        val money = Money.fromCents(1L)
        assertEquals("0.01", money.formatSimplified())
    }

    @Test
    fun `formatSimplified handles ten cents`() {
        val money = Money.fromCents(10L)
        assertEquals("0.1", money.formatSimplified())
    }

    @Test
    fun `formatSimplified handles large amounts`() {
        val money = Money.fromCents(123456789L)
        assertEquals("1234567.89", money.formatSimplified())
    }

    @Test
    fun `format always shows two decimal places`() {
        val money = Money.fromCents(2200L)
        assertEquals("22.00", money.format())
    }

    @Test
    fun `fromCents creates correct Money object`() {
        val money = Money.fromCents(550L)
        assertEquals(550L, money.toCents())
    }

    @Test
    fun `fromBigDecimal converts correctly`() {
        val money = Money.fromBigDecimal(BigDecimal("5.50"))
        assertEquals(550L, money.toCents())
    }

    @Test
    fun `parse handles various formats`() {
        assertEquals(500L, Money.parse("5")?.toCents())
        assertEquals(500L, Money.parse("5.00")?.toCents())
        assertEquals(123456L, Money.parse("1,234.56")?.toCents())
        assertEquals(99L, Money.parse(".99")?.toCents())
        assertEquals(99L, Money.parse("0.99")?.toCents())
    }

    @Test
    fun `parse returns null for invalid input`() {
        assertNull(Money.parse(""))
        assertNull(Money.parse("invalid"))
        assertNull(Money.parse("abc"))
    }

    @Test
    fun `addition works correctly`() {
        val money1 = Money.fromCents(100L)
        val money2 = Money.fromCents(50L)
        val result = money1 + money2
        assertEquals(150L, result.toCents())
    }

    @Test
    fun `subtraction works correctly`() {
        val money1 = Money.fromCents(100L)
        val money2 = Money.fromCents(50L)
        val result = money1 - money2
        assertEquals(50L, result.toCents())
    }

    @Test
    fun `multiplication works correctly`() {
        val money = Money.fromCents(100L)
        val result = money * BigDecimal("2.5")
        assertEquals(250L, result.toCents())
    }

    @Test
    fun `isZero returns true for zero amount`() {
        val money = Money.fromCents(0L)
        assertTrue(money.isZero())
    }

    @Test
    fun `isPositive returns true for positive amount`() {
        val money = Money.fromCents(100L)
        assertTrue(money.isPositive())
    }

    @Test
    fun `isNegative returns true for negative amount`() {
        val money = Money.fromCents(-100L)
        assertTrue(money.isNegative())
    }

    @Test
    fun `ZERO constant is correct`() {
        assertEquals(0L, Money.ZERO.toCents())
        assertTrue(Money.ZERO.isZero())
    }
}
