package com.example.jitterpay.data.local.entity

import org.junit.Assert.*
import org.junit.Test

/**
 * GoalEntity 单元测试
 *
 * 测试实体类的核心功能，包括：
 * - 金额格式转换（分与美元之间的转换）
 * - 格式化输出
 * - 进度计算
 * - 静态方法功能
 */
class GoalEntityTest {

    @Test
    fun `getProgress returns correct percentage`() {
        val entity = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 5000L,
            iconType = "SHIELD",
            isCompleted = false
        )

        val progress = entity.getProgress()

        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun `getProgress returns 1 when fully funded`() {
        val entity = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 10000L,
            iconType = "SHIELD",
            isCompleted = false
        )

        val progress = entity.getProgress()

        assertEquals(1.0f, progress, 0.001f)
    }

    @Test
    fun `getProgress returns 0 when no funds`() {
        val entity = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 0L,
            iconType = "SHIELD",
            isCompleted = false
        )

        val progress = entity.getProgress()

        assertEquals(0f, progress, 0.001f)
    }

    @Test
    fun `checkIsCompleted returns true when funded`() {
        val entity = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 10000L,
            iconType = "SHIELD",
            isCompleted = false
        )

        val isCompleted = entity.checkIsCompleted()

        assertTrue(isCompleted)
    }

    @Test
    fun `checkIsCompleted returns false when underfunded`() {
        val entity = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 7500L,
            iconType = "SHIELD",
            isCompleted = false
        )

        val isCompleted = entity.checkIsCompleted()

        assertFalse(isCompleted)
    }

    @Test
    fun `getRemainingAmountCents returns correct amount`() {
        val entity = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 7500L,
            iconType = "SHIELD",
            isCompleted = false
        )

        val remaining = entity.getRemainingAmountCents()

        assertEquals(2500L, remaining)
    }

    @Test
    fun `getRemainingAmountCents returns 0 when completed`() {
        val entity = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 10000L,
            iconType = "SHIELD",
            isCompleted = false
        )

        val remaining = entity.getRemainingAmountCents()

        assertEquals(0L, remaining)
    }

    @Test
    fun `getFormattedTargetAmount formats correctly`() {
        val entity = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 0L,
            iconType = "SHIELD"
        )

        val formatted = entity.getFormattedTargetAmount()

        assertEquals("$100.00", formatted)
    }

    @Test
    fun `getFormattedCurrentAmount formats correctly`() {
        val entity = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 5500L,
            iconType = "SHIELD"
        )

        val formatted = entity.getFormattedCurrentAmount()

        assertEquals("$55.00", formatted)
    }

    @Test
    fun `parseAmountToCents converts dollars to cents`() {
        val cents = GoalEntity.parseAmountToCents("100.50")

        assertEquals(10050L, cents)
    }

    @Test
    fun `parseAmountToCents handles whole numbers`() {
        val cents = GoalEntity.parseAmountToCents("100")

        assertEquals(10000L, cents)
    }

    @Test
    fun `parseAmountToCents handles small amounts`() {
        val cents = GoalEntity.parseAmountToCents("0.01")

        assertEquals(1L, cents)
    }

    @Test
    fun `parseAmountToCents returns zero for invalid input`() {
        val cents = GoalEntity.parseAmountToCents("invalid")

        assertEquals(0L, cents)
    }

    @Test
    fun `parseAmountToCents handles empty string`() {
        val cents = GoalEntity.parseAmountToCents("")

        assertEquals(0L, cents)
    }

    @Test
    fun `parseAmountToCents converts Double to cents`() {
        val cents = GoalEntity.parseAmountToCents(100.50)

        assertEquals(10050L, cents)
    }

    @Test
    fun `entity creation with default values`() {
        val entity = GoalEntity(
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 0L,
            iconType = "SHIELD"
        )

        assertEquals(0L, entity.id)
        assertFalse(entity.isCompleted)
        assertTrue(entity.createdAt > 0)
        assertTrue(entity.updatedAt > 0)
        assertNull(entity.completedAt)
    }

    @Test
    fun `entity copy updates all fields`() {
        val original = GoalEntity(
            id = 1,
            title = "Original Title",
            targetAmountCents = 10000L,
            currentAmountCents = 5000L,
            iconType = "SHIELD",
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val updated = original.copy(
            title = "Updated Title",
            currentAmountCents = 7500L
        )

        assertEquals("Updated Title", updated.title)
        assertEquals(7500L, updated.currentAmountCents)
        assertEquals(1000L, updated.createdAt)
        assertEquals(1000L, original.targetAmountCents)
    }

    @Test
    fun `isDeposit returns true for DEPOSIT type`() {
        val entity = GoalTransactionEntity(
            id = 1,
            goalId = 1L,
            type = GoalTransactionType.DEPOSIT.name,
            amountCents = 100L,
            dateMillis = System.currentTimeMillis()
        )

        assertTrue(entity.isDeposit())
        assertFalse(entity.isWithdrawal())
    }

    @Test
    fun `isWithdrawal returns true for WITHDRAWAL type`() {
        val entity = GoalTransactionEntity(
            id = 1,
            goalId = 1L,
            type = GoalTransactionType.WITHDRAWAL.name,
            amountCents = 100L,
            dateMillis = System.currentTimeMillis()
        )

        assertFalse(entity.isDeposit())
        assertTrue(entity.isWithdrawal())
    }
}
