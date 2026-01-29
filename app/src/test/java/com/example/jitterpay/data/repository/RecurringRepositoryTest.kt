package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.dao.RecurringDao
import com.example.jitterpay.data.local.entity.RecurringEntity
import com.example.jitterpay.ui.recurring.RecurringFrequency
import com.example.jitterpay.ui.recurring.RecurringTransaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * RecurringRepository 单元测试
 *
 * 测试仓库层的核心功能：
 * - 定时记账添加
 * - 定时记账查询
 * - 激活状态切换
 * - 统计计算
 *
 * 注意：测试中使用UI层RecurringFrequency枚举，数据库层使用字符串存储
 */
class RecurringRepositoryTest {

    private lateinit var recurringDao: RecurringDao
    private lateinit var repository: RecurringRepository

    @Before
    fun setup() {
        recurringDao = mockk(relaxed = true)
        repository = RecurringRepository(recurringDao)
    }

    // ==================== 添加测试 ====================

    @Test
    fun `addRecurring calls dao insert and returns id`() = runTest {
        // Given
        coEvery { recurringDao.insertRecurring(any()) } returns 1L

        // When
        val result = repository.addRecurring(
            title = "Daily Commute",
            amountCents = 350L,
            type = "EXPENSE",
            category = "Transport",
            frequency = RecurringFrequency.DAILY.name,
            startDateMillis = System.currentTimeMillis()
        )

        // Then
        assertEquals(1L, result)
        coVerify {
            recurringDao.insertRecurring(match {
                it.title == "Daily Commute" &&
                it.amountCents == 350L &&
                it.type == "EXPENSE" &&
                it.category == "Transport" &&
                it.frequency == "DAILY"
            })
        }
    }

    @Test
    fun `addRecurring calculates correct monthly amount for daily frequency`() = runTest {
        // Given
        coEvery { recurringDao.insertRecurring(any()) } returns 1L

        // When
        repository.addRecurring(
            title = "Test",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = RecurringFrequency.DAILY.name,
            startDateMillis = System.currentTimeMillis()
        )

        // Then
        coVerify {
            recurringDao.insertRecurring(match {
                it.estimatedMonthlyAmount == 3000L // 100 * 30
            })
        }
    }

    @Test
    fun `addRecurring calculates correct monthly amount for monthly frequency`() = runTest {
        // Given
        coEvery { recurringDao.insertRecurring(any()) } returns 1L

        // When
        repository.addRecurring(
            title = "Netflix",
            amountCents = 1599L,
            type = "EXPENSE",
            category = "Entertainment",
            frequency = RecurringFrequency.MONTHLY.name,
            startDateMillis = System.currentTimeMillis()
        )

        // Then
        coVerify {
            recurringDao.insertRecurring(match {
                it.estimatedMonthlyAmount == 1599L // 1599 * 1
            })
        }
    }

    @Test
    fun `addRecurring calculates next execution date`() = runTest {
        // Given
        val startDate = 1704067200000L // 2024-01-01
        coEvery { recurringDao.insertRecurring(any()) } returns 1L

        // When
        repository.addRecurring(
            title = "Test",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = RecurringFrequency.WEEKLY.name,
            startDateMillis = startDate
        )

        // Then
        coVerify {
            recurringDao.insertRecurring(match {
                it.nextExecutionDateMillis == startDate + 604800000L // +7 days
            })
        }
    }

    // ==================== 查询测试 ====================

    @Test
    fun `getAllRecurring returns flow from dao mapped to ui model`() = runTest {
        // Given
        val entities = listOf(
            RecurringEntity(
                id = 1,
                title = "Morning Commute",
                amountCents = 350L,
                type = "EXPENSE",
                category = "Transport",
                frequency = "DAILY",
                startDateMillis = System.currentTimeMillis(),
                nextExecutionDateMillis = System.currentTimeMillis(),
                estimatedMonthlyAmount = 10500L
            )
        )
        every { recurringDao.getAllRecurring() } returns flowOf(entities)

        // When
        val result = repository.getAllRecurring().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Morning Commute", result[0].title)
        assertEquals(RecurringFrequency.DAILY, result[0].frequency)
    }

    @Test
    fun `getActiveRecurring returns only active transactions`() = runTest {
        // Given
        val entities = listOf(
            RecurringEntity(
                id = 1,
                title = "Active Transaction",
                amountCents = 100L,
                type = "EXPENSE",
                category = "Test",
                frequency = "MONTHLY",
                startDateMillis = System.currentTimeMillis(),
                nextExecutionDateMillis = System.currentTimeMillis(),
                estimatedMonthlyAmount = 100L,
                isActive = true
            )
        )
        every { recurringDao.getActiveRecurring() } returns flowOf(entities)

        // When
        val result = repository.getActiveRecurring().first()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0].isActive)
    }

    @Test
    fun `getById returns from dao mapped to ui model`() = runTest {
        // Given
        val entity = RecurringEntity(
            id = 1,
            title = "Salary",
            amountCents = 500000L,
            type = "INCOME",
            category = "Income",
            frequency = "MONTHLY",
            startDateMillis = System.currentTimeMillis(),
            nextExecutionDateMillis = System.currentTimeMillis(),
            estimatedMonthlyAmount = 500000L
        )
        coEvery { recurringDao.getById(1L) } returns entity

        // When
        val result = repository.getById(1L)

        // Then
        assertNotNull(result)
        assertEquals("Salary", result?.title)
        assertEquals(500000L, result?.amount)
        assertEquals(RecurringFrequency.MONTHLY, result?.frequency)
    }

    @Test
    fun `getById returns null for non-existent id`() = runTest {
        // Given
        coEvery { recurringDao.getById(999L) } returns null

        // When
        val result = repository.getById(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `getByIdFlow returns flow from dao`() = runTest {
        // Given
        val entity = RecurringEntity(
            id = 1,
            title = "Test",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = "DAILY",
            startDateMillis = System.currentTimeMillis(),
            nextExecutionDateMillis = System.currentTimeMillis(),
            estimatedMonthlyAmount = 3000L
        )
        every { recurringDao.getByIdFlow(1L) } returns flowOf(entity)

        // When
        val result = repository.getByIdFlow(1L).first()

        // Then
        assertNotNull(result)
        assertEquals(1L, result?.id)
    }

    @Test
    fun `getByType filters by type`() = runTest {
        // Given
        val expenses = listOf(
            RecurringEntity(
                id = 1,
                title = "Expense",
                amountCents = 100L,
                type = "EXPENSE",
                category = "Test",
                frequency = "MONTHLY",
                startDateMillis = System.currentTimeMillis(),
                nextExecutionDateMillis = System.currentTimeMillis(),
                estimatedMonthlyAmount = 100L
            )
        )
        every { recurringDao.getByType("EXPENSE") } returns flowOf(expenses)

        // When
        val result = repository.getByType("EXPENSE").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("EXPENSE", result[0].type)
    }

    // ==================== 状态切换测试 ====================

    @Test
    fun `toggleActive toggles existing entity`() = runTest {
        // Given
        val entity = RecurringEntity(
            id = 1,
            title = "Test",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = "DAILY",
            startDateMillis = System.currentTimeMillis(),
            nextExecutionDateMillis = System.currentTimeMillis(),
            estimatedMonthlyAmount = 3000L,
            isActive = true
        )
        coEvery { recurringDao.getById(1L) } returns entity

        // When
        repository.toggleActive(1L)

        // Then
        coVerify {
            recurringDao.setActive(1L, false, any())
        }
    }

    @Test
    fun `setActive sets specific active state`() = runTest {
        // When
        repository.setActive(1L, true)

        // Then
        coVerify {
            recurringDao.setActive(1L, true, any())
        }
    }

    @Test
    fun `toggleActive does nothing when entity not found`() = runTest {
        // Given
        coEvery { recurringDao.getById(999L) } returns null

        // When
        repository.toggleActive(999L)

        // Then
        coVerify(exactly = 0) { recurringDao.setActive(any(), any(), any()) }
    }

    // ==================== 删除测试 ====================

    @Test
    fun `deleteRecurring calls dao delete`() = runTest {
        // Given
        val entity = RecurringEntity(
            id = 1,
            title = "Test",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = "DAILY",
            startDateMillis = System.currentTimeMillis(),
            nextExecutionDateMillis = System.currentTimeMillis(),
            estimatedMonthlyAmount = 3000L
        )

        // When
        repository.deleteRecurring(entity)

        // Then
        coVerify { recurringDao.deleteRecurring(entity) }
    }

    @Test
    fun `deleteById calls dao with correct id`() = runTest {
        // When
        repository.deleteById(1L)

        // Then
        coVerify { recurringDao.deleteById(1L) }
    }

    @Test
    fun `deleteAll calls dao method`() = runTest {
        // When
        repository.deleteAll()

        // Then
        coVerify { recurringDao.deleteAll() }
    }

    // ==================== 更新测试 ====================

    @Test
    fun `updateRecurring updates with new timestamp`() = runTest {
        // Given
        val entity = RecurringEntity(
            id = 1,
            title = "Updated Title",
            amountCents = 100L,
            type = "EXPENSE",
            category = "Test",
            frequency = "DAILY",
            startDateMillis = System.currentTimeMillis(),
            nextExecutionDateMillis = System.currentTimeMillis(),
            estimatedMonthlyAmount = 3000L,
            updatedAt = 0L
        )

        // When
        repository.updateRecurring(entity)

        // Then
        coVerify {
            recurringDao.updateRecurring(match {
                it.updatedAt > 0
            })
        }
    }

    // ==================== 统计测试 ====================

    @Test
    fun `getTotalEstimatedMonthlyExpense returns flow from dao`() = runTest {
        // Given
        val expected = 50000L
        every { recurringDao.getTotalEstimatedMonthlyExpense() } returns flowOf(expected)

        // When
        val result = repository.getTotalEstimatedMonthlyExpense().first()

        // Then
        assertEquals(50000L, result)
    }

    @Test
    fun `getTotalEstimatedMonthlyIncome returns flow from dao`() = runTest {
        // Given
        val expected = 500000L
        every { recurringDao.getTotalEstimatedMonthlyIncome() } returns flowOf(expected)

        // When
        val result = repository.getTotalEstimatedMonthlyIncome().first()

        // Then
        assertEquals(500000L, result)
    }

    @Test
    fun `getCount returns from dao`() = runTest {
        // Given
        coEvery { recurringDao.getCount() } returns 5

        // When
        val result = repository.getCount()

        // Then
        assertEquals(5, result)
    }

    @Test
    fun `getActiveCount returns from dao`() = runTest {
        // Given
        coEvery { recurringDao.getActiveCount() } returns 3

        // When
        val result = repository.getActiveCount()

        // Then
        assertEquals(3, result)
    }

    // ==================== 执行到期任务测试 ====================

    @Test
    fun `executeAndAdvance updates next execution dates`() = runTest {
        // Given
        val dueList = listOf(
            RecurringEntity(
                id = 1,
                title = "Due Task",
                amountCents = 100L,
                type = "EXPENSE",
                category = "Test",
                frequency = "WEEKLY",
                startDateMillis = System.currentTimeMillis(),
                nextExecutionDateMillis = System.currentTimeMillis() - 86400000L, // Yesterday
                estimatedMonthlyAmount = 400L
            )
        )
        coEvery { recurringDao.getDueRecurring(any()) } returns dueList

        // When
        repository.executeAndAdvance(dueList)

        // Then
        coVerify {
            recurringDao.updateNextExecutionDates(
                listOf(1L),
                any(),
                any()
            )
        }
    }

    @Test
    fun `executeAndAdvance does nothing for empty list`() = runTest {
        // When
        repository.executeAndAdvance(emptyList())

        // Then
        coVerify(exactly = 0) { recurringDao.updateNextExecutionDates(any(), any(), any()) }
    }

    @Test
    fun `getDueRecurring returns due items from dao`() = runTest {
        // Given
        val dueList = listOf(
            RecurringEntity(
                id = 1,
                title = "Due Task",
                amountCents = 100L,
                type = "EXPENSE",
                category = "Test",
                frequency = "DAILY",
                startDateMillis = System.currentTimeMillis(),
                nextExecutionDateMillis = System.currentTimeMillis() - 1000L,
                estimatedMonthlyAmount = 3000L
            )
        )
        coEvery { recurringDao.getDueRecurring(any()) } returns dueList

        // When
        val result = repository.getDueRecurring()

        // Then
        assertEquals(1, result.size)
        assertEquals("Due Task", result[0].title)
    }

    // ==================== 提醒相关测试 ====================

    @Test
    fun `getRecurringTransactionsNeedingReminder returns items with reminders due`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val tomorrow = currentTime + 86400000L
        val reminderList = listOf(
            RecurringEntity(
                id = 1,
                title = "Netflix Subscription",
                amountCents = 1599L,
                type = "EXPENSE",
                category = "Entertainment",
                frequency = "MONTHLY",
                startDateMillis = currentTime - 86400000L * 30,
                nextExecutionDateMillis = tomorrow,
                isActive = true,
                estimatedMonthlyAmount = 1599L,
                reminderEnabled = true,
                reminderDaysBefore = 1
            )
        )
        coEvery {
            recurringDao.getRecurringTransactionsNeedingReminder(
                eq(currentTime)
            )
        } returns reminderList

        // When
        val result = repository.getRecurringTransactionsNeedingReminder(currentTime)

        // Then
        assertEquals(1, result.size)
        assertEquals("Netflix Subscription", result[0].title)
        assertTrue(result[0].reminderEnabled)
        assertEquals(1, result[0].reminderDaysBefore)
    }

    @Test
    fun `getRecurringTransactionsNeedingReminder excludes disabled reminders`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val reminderList = emptyList<com.example.jitterpay.data.local.entity.RecurringEntity>()
        coEvery {
            recurringDao.getRecurringTransactionsNeedingReminder(
                eq(currentTime)
            )
        } returns reminderList

        // When
        val result = repository.getRecurringTransactionsNeedingReminder(currentTime)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `updateReminderSettings calls dao with correct parameters`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        coEvery {
            recurringDao.updateReminderSettings(
                any(), any(), any(), any()
            )
        } just Runs

        // When
        repository.updateReminderSettings(
            id = 1L,
            reminderEnabled = true,
            reminderDaysBefore = 3
        )

        // Then
        coVerify {
            recurringDao.updateReminderSettings(
                id = 1L,
                reminderEnabled = true,
                reminderDaysBefore = 3,
                updatedAt = any()
            )
        }
    }

    @Test
    fun `updateReminderSettings allows disabling reminders`() = runTest {
        // When
        repository.updateReminderSettings(
            id = 1L,
            reminderEnabled = false,
            reminderDaysBefore = 0
        )

        // Then
        coVerify {
            recurringDao.updateReminderSettings(
                id = 1L,
                reminderEnabled = false,
                reminderDaysBefore = 0,
                updatedAt = any()
            )
        }
    }
}
