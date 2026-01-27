package com.example.jitterpay.data.local.entity

import org.junit.Assert.*
import org.junit.Test

/**
 * TransactionEntity 单元测试
 *
 * 测试实体类的核心功能，包括：
 * - 金额格式转换（分与美元之间的转换）
 * - 格式化输出
 * - 静态方法功能
 */
class TransactionEntityTest {

    @Test
    fun `getFormattedAmount returns correct format for expense`() {
        val entity = TransactionEntity(
            id = 1,
            type = TransactionType.EXPENSE.name,
            amountCents = 550L,
            category = "Dining",
            dateMillis = System.currentTimeMillis()
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("-$5.50", formatted)
    }

    @Test
    fun `getFormattedAmount returns correct format for income`() {
        val entity = TransactionEntity(
            id = 1,
            type = TransactionType.INCOME.name,
            amountCents = 500000L,
            category = "Salary",
            dateMillis = System.currentTimeMillis()
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("+$5000.00", formatted)
    }

    @Test
    fun `getFormattedAmount handles zero amount`() {
        val entity = TransactionEntity(
            id = 1,
            type = TransactionType.EXPENSE.name,
            amountCents = 0L,
            category = "Test",
            dateMillis = System.currentTimeMillis()
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("-$0.00", formatted)
    }

    @Test
    fun `getFormattedAmount handles large amounts`() {
        val entity = TransactionEntity(
            id = 1,
            type = TransactionType.INCOME.name,
            amountCents = 123456789L,
            category = "Investment",
            dateMillis = System.currentTimeMillis()
        )

        val formatted = entity.getFormattedAmount()

        // Note: Number formatting may vary by locale
        // Just check that sign and basic format are correct
        assertTrue(formatted.startsWith("+"))
        assertTrue(formatted.endsWith(".89"))
    }

    @Test
    fun `parseAmountToCents converts correctly`() {
        val cents = TransactionEntity.parseAmountToCents("5.50")

        assertEquals(550L, cents)
    }

    @Test
    fun `parseAmountToCents handles whole numbers`() {
        val cents = TransactionEntity.parseAmountToCents("10.00")

        assertEquals(1000L, cents)
    }

    @Test
    fun `parseAmountToCents handles small amounts`() {
        val cents = TransactionEntity.parseAmountToCents("0.01")

        assertEquals(1L, cents)
    }

    @Test
    fun `parseAmountToCents handles leading zeros`() {
        val cents = TransactionEntity.parseAmountToCents("0005.50")

        assertEquals(550L, cents)
    }

    @Test
    fun `parseAmountToCents returns zero for invalid input`() {
        val cents = TransactionEntity.parseAmountToCents("invalid")

        assertEquals(0L, cents)
    }

    @Test
    fun `parseAmountToCents handles empty string`() {
        val cents = TransactionEntity.parseAmountToCents("")

        assertEquals(0L, cents)
    }

    @Test
    fun `parseAmountToCents handles large amounts`() {
        val cents = TransactionEntity.parseAmountToCents("12345.67")

        assertEquals(1234567L, cents)
    }

    @Test
    fun `entity creation with default values`() {
        val entity = TransactionEntity(
            type = TransactionType.EXPENSE.name,
            amountCents = 1000L,
            category = "Test",
            dateMillis = 1234567890L
        )

        assertEquals(0L, entity.id)
        assertEquals("", entity.description)
        assertEquals(TransactionStatus.COMPLETED.name, entity.status)
        assertTrue(entity.createdAt > 0)
        assertTrue(entity.updatedAt > 0)
    }

    @Test
    fun `entity copy with updated timestamp`() {
        val originalTime = 1000L
        val entity = TransactionEntity(
            id = 1,
            type = TransactionType.EXPENSE.name,
            amountCents = 1000L,
            category = "Test",
            dateMillis = 1234567890L,
            createdAt = originalTime,
            updatedAt = originalTime
        )

        val newTime = 2000L
        val updated = entity.copy(updatedAt = newTime)

        assertEquals(originalTime, updated.createdAt)
        assertEquals(newTime, updated.updatedAt)
    }

    @Test
    fun `transaction types are correct`() {
        assertEquals("EXPENSE", TransactionType.EXPENSE.name)
        assertEquals("INCOME", TransactionType.INCOME.name)
    }

    @Test
    fun `transaction statuses are correct`() {
        assertEquals("PENDING", TransactionStatus.PENDING.name)
        assertEquals("COMPLETED", TransactionStatus.COMPLETED.name)
        assertEquals("RECEIVED", TransactionStatus.RECEIVED.name)
        assertEquals("FAILED", TransactionStatus.FAILED.name)
    }
}
