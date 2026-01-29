package com.example.jitterpay.data.local.entity

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * BudgetEntity 单元测试
 *
 * 测试实体类的核心功能，包括：
 * - 金额格式转换（分与美元之间的转换）
 * - 预算周期时间范围计算
 * - 静态方法功能
 * - 枚举类型验证
 */
class BudgetEntityTest {

    // ==================== 金额格式化测试 ====================

    @Test
    fun `getFormattedAmount returns correct format`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Food Budget",
            amountCents = 50000L, // $500.00
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis()
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("500.00", formatted)
    }

    @Test
    fun `getFormattedAmount handles zero amount`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Test Budget",
            amountCents = 0L,
            periodType = BudgetPeriodType.DAILY.name,
            startDate = System.currentTimeMillis()
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("0.00", formatted)
    }

    @Test
    fun `getFormattedAmount handles large amounts`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Large Budget",
            amountCents = 123456789L, // $1,234,567.89
            periodType = BudgetPeriodType.YEARLY.name,
            startDate = System.currentTimeMillis()
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("1234567.89", formatted)
    }

    @Test
    fun `getFormattedAmount handles cents properly`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Cents Test",
            amountCents = 1L, // $0.01
            periodType = BudgetPeriodType.WEEKLY.name,
            startDate = System.currentTimeMillis()
        )

        val formatted = entity.getFormattedAmount()

        assertEquals("0.01", formatted)
    }

    // ==================== parseAmountToCents 测试 ====================

    @Test
    fun `parseAmountToCents converts string correctly`() {
        val cents = BudgetEntity.parseAmountToCents("500.00")

        assertEquals(50000L, cents)
    }

    @Test
    fun `parseAmountToCents handles whole numbers`() {
        val cents = BudgetEntity.parseAmountToCents("1000")

        assertEquals(100000L, cents)
    }

    @Test
    fun `parseAmountToCents handles small amounts`() {
        val cents = BudgetEntity.parseAmountToCents("0.01")

        assertEquals(1L, cents)
    }

    @Test
    fun `parseAmountToCents returns zero for invalid input`() {
        val cents = BudgetEntity.parseAmountToCents("invalid")

        assertEquals(0L, cents)
    }

    @Test
    fun `parseAmountToCents handles empty string`() {
        val cents = BudgetEntity.parseAmountToCents("")

        assertEquals(0L, cents)
    }

    @Test
    fun `parseAmountToCents converts Double correctly`() {
        val cents = BudgetEntity.parseAmountToCents(500.00)

        assertEquals(50000L, cents)
    }

    @Test
    fun `parseAmountToCents handles Double with cents`() {
        val cents = BudgetEntity.parseAmountToCents(123.45)

        assertEquals(12345L, cents)
    }

    // ==================== DAILY 周期时间范围计算测试 ====================

    @Test
    fun `getCurrentPeriodRange for DAILY returns correct range`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Daily Budget",
            amountCents = 10000L,
            periodType = BudgetPeriodType.DAILY.name,
            startDate = System.currentTimeMillis()
        )

        val (start, end) = entity.getCurrentPeriodRange()
        val calendar = Calendar.getInstance()

        // 验证开始时间是今天凌晨0点
        calendar.timeInMillis = start
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))

        // 验证结束时间是今天23:59:59.999
        calendar.timeInMillis = end
        assertEquals(23, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendar.get(Calendar.MINUTE))
        assertEquals(59, calendar.get(Calendar.SECOND))
    }

    @Test
    fun `getCurrentPeriodRange for DAILY span is one day`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Daily Budget",
            amountCents = 10000L,
            periodType = BudgetPeriodType.DAILY.name,
            startDate = System.currentTimeMillis()
        )

        val (start, end) = entity.getCurrentPeriodRange()
        val oneDayInMillis = 24 * 60 * 60 * 1000L

        // 验证时间跨度大约为1天（毫秒数减1）
        assertEquals(oneDayInMillis - 1, end - start)
    }

    // ==================== WEEKLY 周期时间范围计算测试 ====================

    @Test
    fun `getCurrentPeriodRange for WEEKLY returns correct start`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Weekly Budget",
            amountCents = 50000L,
            periodType = BudgetPeriodType.WEEKLY.name,
            startDate = System.currentTimeMillis()
        )

        val (start, _) = entity.getCurrentPeriodRange()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = start

        // 验证开始时间是本周一凌晨0点
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
    }

    @Test
    fun `getCurrentPeriodRange for WEEKLY span is one week`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Weekly Budget",
            amountCents = 50000L,
            periodType = BudgetPeriodType.WEEKLY.name,
            startDate = System.currentTimeMillis()
        )

        val (start, end) = entity.getCurrentPeriodRange()
        val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L

        // 验证时间跨度大约为1周
        assertEquals(oneWeekInMillis - 1, end - start)
    }

    // ==================== MONTHLY 周期时间范围计算测试 ====================

    @Test
    fun `getCurrentPeriodRange for MONTHLY returns correct start`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Monthly Budget",
            amountCents = 100000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis()
        )

        val (start, _) = entity.getCurrentPeriodRange()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = start

        // 验证开始时间是本月1日凌晨0点
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
    }

    @Test
    fun `getCurrentPeriodRange for MONTHLY span is one month`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Monthly Budget",
            amountCents = 100000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis()
        )

        val (start, end) = entity.getCurrentPeriodRange()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = start

        // 获取当前月份的天数
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val oneMonthInMillis = daysInMonth * 24 * 60 * 60 * 1000L

        // 验证时间跨度大约为1个月
        assertTrue(end - start >= oneMonthInMillis - 1000) // 允许1秒误差
        assertTrue(end - start <= oneMonthInMillis + 1000)
    }

    // ==================== YEARLY 周期时间范围计算测试 ====================

    @Test
    fun `getCurrentPeriodRange for YEARLY returns correct start`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Yearly Budget",
            amountCents = 1200000L,
            periodType = BudgetPeriodType.YEARLY.name,
            startDate = System.currentTimeMillis()
        )

        val (start, _) = entity.getCurrentPeriodRange()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = start

        // 验证开始时间是本年1月1日凌晨0点
        assertEquals(1, calendar.get(Calendar.DAY_OF_YEAR))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
    }

    @Test
    fun `getCurrentPeriodRange for YEARLY span is one year`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Yearly Budget",
            amountCents = 1200000L,
            periodType = BudgetPeriodType.YEARLY.name,
            startDate = System.currentTimeMillis()
        )

        val (start, end) = entity.getCurrentPeriodRange()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = start

        // 获取当前年份的天数（考虑闰年）
        val daysInYear = if (calendar.get(Calendar.YEAR) % 4 == 0) 366 else 365
        val oneYearInMillis = daysInYear * 24 * 60 * 60 * 1000L

        // 验证时间跨度大约为1年
        assertTrue(end - start >= oneYearInMillis - 1000) // 允许1秒误差
        assertTrue(end - start <= oneYearInMillis + 1000)
    }

    // ==================== 实体字段测试 ====================

    @Test
    fun `entity creation with default values`() {
        val entity = BudgetEntity(
            title = "Test Budget",
            amountCents = 100000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis()
        )

        assertEquals(0L, entity.id)
        assertTrue(entity.notifyAt80)
        assertTrue(entity.notifyAt90)
        assertTrue(entity.notifyAt100)
        assertTrue(entity.isActive)
        assertTrue(entity.createdAt > 0)
        assertTrue(entity.updatedAt > 0)
        assertNull(entity.endDate)
    }

    @Test
    fun `entity with custom notification settings`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Custom Notifications",
            amountCents = 100000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis(),
            notifyAt80 = false,
            notifyAt90 = true,
            notifyAt100 = false
        )

        assertFalse(entity.notifyAt80)
        assertTrue(entity.notifyAt90)
        assertFalse(entity.notifyAt100)
    }

    @Test
    fun `entity with endDate`() {
        val now = System.currentTimeMillis()
        val oneMonthLater = now + (30 * 24 * 60 * 60 * 1000L)

        val entity = BudgetEntity(
            id = 1,
            title = "Fixed Period Budget",
            amountCents = 100000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = now,
            endDate = oneMonthLater
        )

        assertNotNull(entity.endDate)
        assertEquals(oneMonthLater, entity.endDate)
    }

    @Test
    fun `entity with inactive status`() {
        val entity = BudgetEntity(
            id = 1,
            title = "Inactive Budget",
            amountCents = 100000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis(),
            isActive = false
        )

        assertFalse(entity.isActive)
    }

    @Test
    fun `entity copy with updated timestamp`() {
        val originalTime = 1000L
        val entity = BudgetEntity(
            id = 1,
            title = "Test Budget",
            amountCents = 100000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis(),
            createdAt = originalTime,
            updatedAt = originalTime
        )

        val newTime = 2000L
        val updated = entity.copy(updatedAt = newTime)

        assertEquals(originalTime, updated.createdAt)
        assertEquals(newTime, updated.updatedAt)
    }

    // ==================== 预算周期枚举测试 ====================

    @Test
    fun `budget period types are correct`() {
        assertEquals("DAILY", BudgetPeriodType.DAILY.name)
        assertEquals("WEEKLY", BudgetPeriodType.WEEKLY.name)
        assertEquals("MONTHLY", BudgetPeriodType.MONTHLY.name)
        assertEquals("YEARLY", BudgetPeriodType.YEARLY.name)
    }

    @Test
    fun `budget period type values are distinct`() {
        val types = setOf(
            BudgetPeriodType.DAILY,
            BudgetPeriodType.WEEKLY,
            BudgetPeriodType.MONTHLY,
            BudgetPeriodType.YEARLY
        )

        assertEquals(4, types.size)
    }
}
