package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.dao.BudgetDao
import com.example.jitterpay.data.local.dao.TransactionDao
import com.example.jitterpay.data.local.entity.BudgetEntity
import com.example.jitterpay.data.local.entity.BudgetPeriodType
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * BudgetRepository 单元测试
 *
 * 测试仓库层的核心功能：
 * - 预算 CRUD 操作
 * - 预算查询
 * - 支出金额统计
 * - 数据转换
 */
class BudgetRepositoryTest {

    private lateinit var budgetDao: BudgetDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: BudgetRepository

    @Before
    fun setup() {
        budgetDao = mockk(relaxed = true)
        transactionDao = mockk(relaxed = true)
        repository = BudgetRepository(budgetDao, transactionDao)
    }

    // ==================== 创建预算测试 ====================

    @Test
    fun `createBudget calls dao insert and returns id`() = runTest {
        // Given
        val expectedId = 1L
        coEvery { budgetDao.insertBudget(any()) } returns expectedId

        // When
        val result = repository.createBudget(
            title = "Food Budget",
            amountCents = 50000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis()
        )

        // Then
        assertEquals(expectedId, result)
        coVerify {
            budgetDao.insertBudget(match {
                it.title == "Food Budget" &&
                it.amountCents == 50000L &&
                it.periodType == BudgetPeriodType.MONTHLY.name &&
                it.isActive == true
            })
        }
    }

    @Test
    fun `createBudget with custom notification settings stores them`() = runTest {
        // Given
        coEvery { budgetDao.insertBudget(any()) } returns 1L

        // When
        repository.createBudget(
            title = "Custom Notifications",
            amountCents = 100000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis(),
            notifyAt80 = false,
            notifyAt90 = true,
            notifyAt100 = false
        )

        // Then
        coVerify {
            budgetDao.insertBudget(match {
                it.notifyAt80 == false &&
                it.notifyAt90 == true &&
                it.notifyAt100 == false
            })
        }
    }

    @Test
    fun `createBudget with endDate stores it`() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val oneMonthLater = now + (30 * 24 * 60 * 60 * 1000L)
        coEvery { budgetDao.insertBudget(any()) } returns 1L

        // When
        repository.createBudget(
            title = "Fixed Period Budget",
            amountCents = 100000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = now,
            endDate = oneMonthLater
        )

        // Then
        coVerify {
            budgetDao.insertBudget(match {
                it.endDate == oneMonthLater
            })
        }
    }

    // ==================== 更新预算测试 ====================

    @Test
    fun `updateBudget calls dao with updated timestamp`() = runTest {
        // Given
        val budget = BudgetEntity(
            id = 1,
            title = "Original",
            amountCents = 50000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis(),
            updatedAt = 1000L
        )
        coEvery { budgetDao.updateBudget(any()) } returns Unit

        // When
        repository.updateBudget(budget)

        // Then
        coVerify {
            budgetDao.updateBudget(match {
                it.updatedAt > 1000L
            })
        }
    }

    @Test
    fun `updateBudget modifies existing data`() = runTest {
        // Given
        val budget = BudgetEntity(
            id = 1,
            title = "Test",
            amountCents = 50000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis()
        )
        coEvery { budgetDao.updateBudget(any()) } returns Unit

        // When
        repository.updateBudget(budget.copy(title = "Updated", amountCents = 100000L))

        // Then
        coVerify {
            budgetDao.updateBudget(match {
                it.title == "Updated" &&
                it.amountCents == 100000L
            })
        }
    }

    // ==================== 删除预算测试 ====================

    @Test
    fun `deleteBudget calls dao delete`() = runTest {
        // Given
        val budget = BudgetEntity(
            id = 1,
            title = "To Delete",
            amountCents = 50000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis()
        )

        // When
        repository.deleteBudget(budget)

        // Then
        coVerify { budgetDao.deleteBudget(budget) }
    }

    @Test
    fun `deleteBudgetById calls dao with correct id`() = runTest {
        // When
        repository.deleteBudgetById(1L)

        // Then
        coVerify { budgetDao.deleteBudgetById(1L) }
    }

    // ==================== 查询预算测试 ====================

    @Test
    fun `getAllBudgets returns flow from dao`() = runTest {
        // Given
        val expectedBudgets = listOf(
            BudgetEntity(
                id = 1,
                title = "Food",
                amountCents = 50000L,
                periodType = BudgetPeriodType.MONTHLY.name,
                startDate = System.currentTimeMillis()
            ),
            BudgetEntity(
                id = 2,
                title = "Transport",
                amountCents = 30000L,
                periodType = BudgetPeriodType.WEEKLY.name,
                startDate = System.currentTimeMillis()
            )
        )
        every { budgetDao.getAllBudgets() } returns flowOf(expectedBudgets)

        // When
        val result = repository.getAllBudgets().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Food", result[0].title)
        assertEquals("Transport", result[1].title)
    }

    @Test
    fun `getActiveBudgets returns only active budgets`() = runTest {
        // Given
        val activeBudgets = listOf(
            BudgetEntity(
                id = 1,
                title = "Active Budget",
                amountCents = 50000L,
                periodType = BudgetPeriodType.MONTHLY.name,
                startDate = System.currentTimeMillis(),
                isActive = true
            )
        )
        every { budgetDao.getActiveBudgets() } returns flowOf(activeBudgets)

        // When
        val result = repository.getActiveBudgets().first()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0].isActive)
    }

    @Test
    fun `getBudgetById returns from dao`() = runTest {
        // Given
        val expectedBudget = BudgetEntity(
            id = 1,
            title = "Specific Budget",
            amountCents = 50000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis()
        )
        coEvery { budgetDao.getBudgetById(1L) } returns expectedBudget

        // When
        val result = repository.getBudgetById(1L)

        // Then
        assertNotNull(result)
        assertEquals("Specific Budget", result?.title)
        assertEquals(50000L, result?.amountCents)
    }

    @Test
    fun `getBudgetById returns null for non-existent id`() = runTest {
        // Given
        coEvery { budgetDao.getBudgetById(999L) } returns null

        // When
        val result = repository.getBudgetById(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `getBudgetByIdFlow returns flow from dao`() = runTest {
        // Given
        val expectedBudget = BudgetEntity(
            id = 1,
            title = "Flow Budget",
            amountCents = 50000L,
            periodType = BudgetPeriodType.MONTHLY.name,
            startDate = System.currentTimeMillis()
        )
        every { budgetDao.getBudgetByIdFlow(1L) } returns flowOf(expectedBudget)

        // When
        val result = repository.getBudgetByIdFlow(1L).first()

        // Then
        assertNotNull(result)
        assertEquals("Flow Budget", result?.title)
    }

    // ==================== 支出金额统计测试 ====================

    @Test
    fun `getSpentAmountForPeriodFlow calculates correct amount`() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val startDate = now - (24 * 60 * 60 * 1000L) // 1 day ago
        val endDate = now

        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.EXPENSE.name,
                amountCents = 5000L, // $50.00
                category = "Food",
                dateMillis = startDate + 3600000L // 1 hour after start
            ),
            TransactionEntity(
                id = 2,
                type = TransactionType.EXPENSE.name,
                amountCents = 3000L, // $30.00
                category = "Transport",
                dateMillis = startDate + 7200000L // 2 hours after start
            ),
            TransactionEntity(
                id = 3,
                type = TransactionType.INCOME.name, // Should be ignored
                amountCents = 100000L, // $1000.00
                category = "Salary",
                dateMillis = startDate + 10800000L // 3 hours after start
            )
        )

        every {
            transactionDao.getTransactionsByDateRange(startDate, endDate)
        } returns flowOf(transactions)

        // When
        val result = repository.getSpentAmountForPeriodFlow(startDate, endDate).first()

        // Then
        assertEquals(8000L, result) // $80.00 (50 + 30)
    }

    @Test
    fun `getSpentAmountForPeriodFlow returns zero for no expenses`() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val startDate = now - (24 * 60 * 60 * 1000L)
        val endDate = now

        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.INCOME.name, // Only income, no expense
                amountCents = 100000L,
                category = "Salary",
                dateMillis = startDate + 3600000L
            )
        )

        every {
            transactionDao.getTransactionsByDateRange(startDate, endDate)
        } returns flowOf(transactions)

        // When
        val result = repository.getSpentAmountForPeriodFlow(startDate, endDate).first()

        // Then
        assertEquals(0L, result)
    }

    @Test
    fun `getSpentAmountForPeriodFlow filters by date range`() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val startDate = now - (24 * 60 * 60 * 1000L) // 1 day ago
        val endDate = now

        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.EXPENSE.name,
                amountCents = 5000L,
                category = "Food",
                dateMillis = startDate - 3600000L // Before start - should be ignored
            ),
            TransactionEntity(
                id = 2,
                type = TransactionType.EXPENSE.name,
                amountCents = 3000L,
                category = "Transport",
                dateMillis = startDate + 3600000L // In range - should be counted
            ),
            TransactionEntity(
                id = 3,
                type = TransactionType.EXPENSE.name,
                amountCents = 2000L,
                category = "Entertainment",
                dateMillis = endDate + 3600000L // After end - should be ignored
            )
        )

        every {
            transactionDao.getTransactionsByDateRange(startDate, endDate)
        } returns flowOf(transactions)

        // When
        val result = repository.getSpentAmountForPeriodFlow(startDate, endDate).first()

        // Then
        assertEquals(3000L, result) // Only the $30.00 expense in range
    }

    // ==================== 统计操作测试 ====================

    @Test
    fun `getBudgetCount returns from dao`() = runTest {
        // Given
        coEvery { budgetDao.getBudgetCount() } returns 5

        // When
        val result = repository.getBudgetCount()

        // Then
        assertEquals(5, result)
    }

    @Test
    fun `getActiveBudgetCount returns from dao`() = runTest {
        // Given
        coEvery { budgetDao.getActiveBudgetCount() } returns 3

        // When
        val result = repository.getActiveBudgetCount()

        // Then
        assertEquals(3, result)
    }
}
