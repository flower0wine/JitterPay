package com.example.jitterpay.data.local.entity

import org.junit.Assert.*
import org.junit.Test

/**
 * RecurringEntity 单元测试
 *
 * 测试实体类的核心功能，包括：
 * - 金额格式转换（分与美元之间的转换）
 * - 月度预估金额计算
 * - 下次执行日期计算
 * - 金额字符串解析
 * - 格式化输出
 *
 * 注意：频率值使用字符串常量（"DAILY", "WEEKLY", "BIWEEKLY", "MONTHLY", "YEARLY"），
 * 对应UI层的RecurringFrequency枚举
 */
class RecurringEntityTest {

    // ==================== 金额格式转换测试 ====================

    @Test
    fun `getFormattedAmount returns correct format for expense`() {
        val entity = RecurringEntity(
            id = 1,
            title = "Test Expense",
            amountCents = 550L,
            type = "EXPENSE",
            category = "Transport",
            frequency = "DAILY",
            startDateMillis = System.currentTimeMillis(),
            nextExecutionDateMillis = System.currentTimeMillis(),
            estimatedMonthlyAmount = 16500L
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("-$5.50", formatted)
    }

    @Test
    fun `getFormattedAmount returns correct format for income`() {
        val entity = RecurringEntity(
            id = 1,
            title = "Test Income",
            amountCents = 500000L,
            type = "INCOME",
            category = "Income",
            frequency = "MONTHLY",
            startDateMillis = System.currentTimeMillis(),
            nextExecutionDateMillis = System.currentTimeMillis(),
            estimatedMonthlyAmount = 500000L
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("+$5,000.00", formatted)
    }

    @Test
    fun `getFormattedAmount handles zero amount`() {
        val entity = RecurringEntity(
            id = 1,
            title = "Test",
            amountCents = 0L,
            type = "EXPENSE",
            category = "Test",
            frequency = "DAILY",
            startDateMillis = System.currentTimeMillis(),
            nextExecutionDateMillis = System.currentTimeMillis(),
            estimatedMonthlyAmount = 0L
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("-$0.00", formatted)
    }

    @Test
    fun `getFormattedAmount handles large amounts`() {
        val entity = RecurringEntity(
            id = 1,
            title = "Test",
            amountCents = 123456789L,
            type = "INCOME",
            category = "Investment",
            frequency = "MONTHLY",
            startDateMillis = System.currentTimeMillis(),
            nextExecutionDateMillis = System.currentTimeMillis(),
            estimatedMonthlyAmount = 123456789L
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("+$1,234,567.89", formatted)
    }

    // ==================== 金额解析测试 ====================

    @Test
    fun `parseAmountToCents converts correctly`() {
        val cents = RecurringEntity.parseAmountToCents("5.50")

        assertEquals(550L, cents)
    }

    @Test
    fun `parseAmountToCents handles whole numbers`() {
        val cents = RecurringEntity.parseAmountToCents("10.00")

        assertEquals(1000L, cents)
    }

    @Test
    fun `parseAmountToCents handles small amounts`() {
        val cents = RecurringEntity.parseAmountToCents("0.01")

        assertEquals(1L, cents)
    }

    @Test
    fun `parseAmountToCents handles leading zeros`() {
        val cents = RecurringEntity.parseAmountToCents("0005.50")

        assertEquals(550L, cents)
    }

    @Test
    fun `parseAmountToCents returns zero for invalid input`() {
        val cents = RecurringEntity.parseAmountToCents("invalid")

        assertEquals(0L, cents)
    }

    @Test
    fun `parseAmountToCents handles empty string`() {
        val cents = RecurringEntity.parseAmountToCents("")

        assertEquals(0L, cents)
    }

    @Test
    fun `parseAmountToCents handles large amounts`() {
        val cents = RecurringEntity.parseAmountToCents("12345.67")

        assertEquals(1234567L, cents)
    }

    // ==================== 月度预估金额计算测试 ====================

    @Test
    fun `calculateEstimatedMonthlyAmount for DAILY frequency`() {
        val monthly = RecurringEntity.calculateEstimatedMonthlyAmount(100L, "DAILY")

        assertEquals(3000L, monthly) // 100 * 30 = 3000
    }

    @Test
    fun `calculateEstimatedMonthlyAmount for WEEKLY frequency`() {
        val monthly = RecurringEntity.calculateEstimatedMonthlyAmount(100L, "WEEKLY")

        assertEquals(400L, monthly) // 100 * 4 = 400
    }

    @Test
    fun `calculateEstimatedMonthlyAmount for BIWEEKLY frequency`() {
        val monthly = RecurringEntity.calculateEstimatedMonthlyAmount(100L, "BIWEEKLY")

        assertEquals(200L, monthly) // 100 * 2 = 200
    }

    @Test
    fun `calculateEstimatedMonthlyAmount for MONTHLY frequency`() {
        val monthly = RecurringEntity.calculateEstimatedMonthlyAmount(100L, "MONTHLY")

        assertEquals(100L, monthly) // 100 * 1 = 100
    }

    @Test
    fun `calculateEstimatedMonthlyAmount for YEARLY frequency`() {
        val monthly = RecurringEntity.calculateEstimatedMonthlyAmount(1200L, "YEARLY")

        assertEquals(100L, monthly) // 1200 / 12 = 100
    }

    @Test
    fun `calculateEstimatedMonthlyAmount handles case insensitive frequency`() {
        val monthly = RecurringEntity.calculateEstimatedMonthlyAmount(100L, "daily")

        assertEquals(3000L, monthly)
    }

    @Test
    fun `calculateEstimatedMonthlyAmount handles unknown frequency`() {
        val monthly = RecurringEntity.calculateEstimatedMonthlyAmount(100L, "UNKNOWN")

        assertEquals(100L, monthly) // Default to same amount
    }

    // ==================== 下次执行日期计算测试 ====================

    @Test
    fun `calculateNextExecutionDate for DAILY frequency`() {
        val startDate = 1704067200000L // 2024-01-01 00:00:00 UTC
        val nextDate = RecurringEntity.calculateNextExecutionDate(startDate, "DAILY")

        assertEquals(startDate + 86400000L, nextDate) // +1 day in milliseconds
    }

    @Test
    fun `calculateNextExecutionDate for WEEKLY frequency`() {
        val startDate = 1704067200000L // 2024-01-01 00:00:00 UTC
        val nextDate = RecurringEntity.calculateNextExecutionDate(startDate, "WEEKLY")

        assertEquals(startDate + 604800000L, nextDate) // +7 days in milliseconds
    }

    @Test
    fun `calculateNextExecutionDate for BIWEEKLY frequency`() {
        val startDate = 1704067200000L // 2024-01-01 00:00:00 UTC
        val nextDate = RecurringEntity.calculateNextExecutionDate(startDate, "BIWEEKLY")

        assertEquals(startDate + 1209600000L, nextDate) // +14 days in milliseconds
    }

    @Test
    fun `calculateNextExecutionDate for MONTHLY frequency`() {
        val startDate = 1704067200000L // 2024-01-01 00:00:00 UTC
        val nextDate = RecurringEntity.calculateNextExecutionDate(startDate, "MONTHLY")

        // Should be approximately 1 month later (using Calendar.add(Calendar.MONTH, 1))
        assertTrue(nextDate > startDate)
        assertTrue(nextDate < startDate + 32L * 24 * 60 * 60 * 1000) // Less than 32 days
    }

    @Test
    fun `calculateNextExecutionDate for YEARLY frequency`() {
        val startDate = 1704067200000L // 2024-01-01 00:00:00 UTC
        val nextDate = RecurringEntity.calculateNextExecutionDate(startDate, "YEARLY")

        // Should be exactly 1 year later
        assertEquals(startDate + 31622400000L, nextDate) // 365.25 days * 24 * 60 * 60 * 1000
    }

    @Test
    fun `calculateNextExecutionDate handles case insensitive frequency`() {
        val startDate = 1704067200000L
        val nextDate = RecurringEntity.calculateNextExecutionDate(startDate, "daily")

        assertEquals(startDate + 86400000L, nextDate)
    }

    // ==================== 实体创建测试 ====================

    @Test
    fun `entity creation with default values`() {
        val entity = RecurringEntity(
            title = "Test Recurring",
            amountCents = 1000L,
            type = "EXPENSE",
            category = "Transport",
            frequency = "DAILY",
            startDateMillis = 1704067200000L,
            nextExecutionDateMillis = 1704153600000L,
            estimatedMonthlyAmount = 30000L
        )

        assertEquals(0L, entity.id)
        assertTrue(entity.isActive)
        assertTrue(entity.createdAt > 0)
        assertTrue(entity.updatedAt > 0)
    }

    @Test
    fun `entity copy updates fields correctly`() {
        val original = RecurringEntity(
            id = 1,
            title = "Original Title",
            amountCents = 1000L,
            type = "EXPENSE",
            category = "Test",
            frequency = "DAILY",
            startDateMillis = 1704067200000L,
            nextExecutionDateMillis = 1704153600000L,
            estimatedMonthlyAmount = 30000L,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val updated = original.copy(
            title = "Updated Title",
            isActive = false
        )

        assertEquals("Updated Title", updated.title)
        assertFalse(updated.isActive)
        assertEquals(1000L, updated.createdAt)
        assertEquals(1000L, original.amountCents)
    }

    @Test
    fun `entity creation with all fields`() {
        val currentTime = System.currentTimeMillis()
        val entity = RecurringEntity(
            id = 1,
            title = "Monthly Subscription",
            amountCents = 1599L,
            type = "EXPENSE",
            category = "Entertainment",
            frequency = "MONTHLY",
            startDateMillis = currentTime,
            nextExecutionDateMillis = currentTime + 86400000L * 30,
            isActive = true,
            estimatedMonthlyAmount = 1599L,
            createdAt = currentTime,
            updatedAt = currentTime
        )

        assertEquals(1L, entity.id)
        assertEquals("Monthly Subscription", entity.title)
        assertEquals(1599L, entity.amountCents)
        assertEquals("EXPENSE", entity.type)
        assertEquals("Entertainment", entity.category)
        assertEquals("MONTHLY", entity.frequency)
        assertTrue(entity.isActive)
    }
}
