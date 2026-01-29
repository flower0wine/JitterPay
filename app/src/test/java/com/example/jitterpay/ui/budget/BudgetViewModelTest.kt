package com.example.jitterpay.ui.budget

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.jitterpay.data.local.entity.BudgetEntity
import com.example.jitterpay.data.local.entity.BudgetPeriodType
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.BudgetRepository
import com.example.jitterpay.data.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * BudgetViewModel 单元测试
 *
 * 测试ViewModel的状态管理和用户操作：
 * - 加载预算列表
 * - 创建预算
 * - 更新预算
 * - 删除预算
 * - 错误处理
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var budgetRepository: BudgetRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: BudgetViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        budgetRepository = mockk(relaxed = true)
        transactionRepository = mockk(relaxed = true)

        viewModel = BudgetViewModel(budgetRepository, transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== 加载预算测试 ====================

    @Test
    fun `loadBudgets updates uiState with budgets`() = runTest {
        // Given
        val budgets = listOf(
            BudgetEntity(
                id = 1,
                title = "Food Budget",
                amountCents = 50000L,
                periodType = BudgetPeriodType.MONTHLY.name,
                startDate = System.currentTimeMillis()
            ),
            BudgetEntity(
                id = 2,
                title = "Transport Budget",
                amountCents = 30000L,
                periodType = BudgetPeriodType.WEEKLY.name,
                startDate = System.currentTimeMillis()
            )
        )
        val transactions = emptyList<TransactionEntity>()

        every { budgetRepository.getAllBudgets() } returns flowOf(budgets)
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)

        // When
        viewModel.reloadBudgets()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertEquals(2, uiState.budgets.size)
        assertEquals("Food Budget", uiState.budgets[0].title)
        assertEquals("Transport Budget", uiState.budgets[1].title)
    }

    @Test
    fun `loadBudgets calculates spent amount correctly`() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val budgets = listOf(
            BudgetEntity(
                id = 1,
                title = "Food Budget",
                amountCents = 50000L, // $500.00
                periodType = BudgetPeriodType.MONTHLY.name,
                startDate = now
            )
        )
        val (start, end) = budgets[0].getCurrentPeriodRange()
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.EXPENSE.name,
                amountCents = 5000L, // $50.00
                category = "Food",
                dateMillis = (start + end) / 2
            ),
            TransactionEntity(
                id = 2,
                type = TransactionType.EXPENSE.name,
                amountCents = 3000L, // $30.00
                category = "Food",
                dateMillis = (start + end) / 2 + 1000
            )
        )

        every { budgetRepository.getAllBudgets() } returns flowOf(budgets)
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)

        // When
        viewModel.reloadBudgets()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.budgets.size)
        assertEquals(80.0, uiState.budgets[0].spentAmount, 0.01) // $80.00 = $50 + $30
    }

    @Test
    fun `loadBudgets only counts expenses in date range`() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val budgets = listOf(
            BudgetEntity(
                id = 1,
                title = "Food Budget",
                amountCents = 50000L,
                periodType = BudgetPeriodType.MONTHLY.name,
                startDate = now
            )
        )
        val (start, end) = budgets[0].getCurrentPeriodRange()
        val transactions = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.EXPENSE.name,
                amountCents = 5000L, // $50.00 - In range
                category = "Food",
                dateMillis = (start + end) / 2
            ),
            TransactionEntity(
                id = 2,
                type = TransactionType.EXPENSE.name,
                amountCents = 3000L, // $30.00 - Before start
                category = "Food",
                dateMillis = start - 1000
            ),
            TransactionEntity(
                id = 3,
                type = TransactionType.EXPENSE.name,
                amountCents = 2000L, // $20.00 - After end
                category = "Food",
                dateMillis = end + 1000
            )
        )

        every { budgetRepository.getAllBudgets() } returns flowOf(budgets)
        every { transactionRepository.getAllTransactions() } returns flowOf(transactions)

        // When
        viewModel.reloadBudgets()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(50.0, uiState.budgets[0].spentAmount, 0.01) // Only $50.00
    }

    @Test
    fun `loadBudgets handles empty list`() = runTest {
        // Given
        every { budgetRepository.getAllBudgets() } returns flowOf(emptyList())
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())

        // When
        viewModel.reloadBudgets()
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertTrue(uiState.budgets.isEmpty())
    }

    @Test
    fun `loadBudgets sets loading state initially`() = runTest {
        // Given
        every { budgetRepository.getAllBudgets() } returns flowOf(emptyList())
        every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())

        // When
        viewModel.reloadBudgets()

        // Then - check loading state is set immediately
        assertTrue(viewModel.uiState.value.isLoading)

        // Wait for completion
        advanceUntilIdle()

        // Then - check loading state is cleared after completion
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ==================== 创建预算测试 ====================

    @Test
    fun `createBudget calls repository with correct data`() = runTest {
        // Given
        val budgetData = BudgetData(
            title = "New Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis()
        )
        coEvery { budgetRepository.createBudget(any(), any(), any(), any(), any(), any(), any(), any()) } returns 1L

        // When
        viewModel.createBudget(budgetData)
        advanceUntilIdle()

        // Then
        coVerify {
            budgetRepository.createBudget(
                title = "New Budget",
                amountCents = 50000L, // 500.00 * 100
                periodType = BudgetPeriodType.MONTHLY.name,
                startDate = budgetData.startDate,
                endDate = budgetData.endDate,
                notifyAt80 = true,
                notifyAt90 = true,
                notifyAt100 = true
            )
        }
    }

    @Test
    fun `createBudget with custom notifications stores them`() = runTest {
        // Given
        val budgetData = BudgetData(
            title = "Custom Notifications",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            notifyAt80 = false,
            notifyAt90 = true,
            notifyAt100 = false
        )
        coEvery { budgetRepository.createBudget(any(), any(), any(), any(), any(), any(), any(), any()) } returns 1L

        // When
        viewModel.createBudget(budgetData)
        advanceUntilIdle()

        // Then
        coVerify {
            budgetRepository.createBudget(
                any(),
                any(),
                any(),
                any(),
                any(),
                notifyAt80 = false,
                notifyAt90 = true,
                notifyAt100 = false
            )
        }
    }

    @Test
    fun `createBudget on error sets error in uiState`() = runTest {
        // Given
        val budgetData = BudgetData(
            title = "Error Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis()
        )
        coEvery { budgetRepository.createBudget(any(), any(), any(), any(), any(), any(), any(), any()) } throws Exception("Database error")

        // When
        viewModel.createBudget(budgetData)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertNotNull(uiState.error)
        assertEquals("Database error", uiState.error)
    }

    // ==================== 更新预算测试 ====================

    @Test
    fun `updateBudget calls repository with correct data`() = runTest {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Updated Budget",
            amount = 1000.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            isActive = true
        )
        coEvery { budgetRepository.updateBudget(any()) } returns Unit

        // When
        viewModel.updateBudget(budgetData)
        advanceUntilIdle()

        // Then
        coVerify {
            budgetRepository.updateBudget(match {
                it.id == 1L &&
                it.title == "Updated Budget" &&
                it.amountCents == 100000L && // 1000.00 * 100
                it.periodType == BudgetPeriodType.MONTHLY.name &&
                it.isActive == true
            })
        }
    }

    @Test
    fun `updateBudget on error sets error in uiState`() = runTest {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Update Error",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis()
        )
        coEvery { budgetRepository.updateBudget(any()) } throws Exception("Update failed")

        // When
        viewModel.updateBudget(budgetData)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertNotNull(uiState.error)
        assertEquals("Update failed", uiState.error)
    }

    // ==================== 删除预算测试 ====================

    @Test
    fun `deleteBudget calls repository with correct id`() = runTest {
        // Given
        val budgetId = 1L
        coEvery { budgetRepository.deleteBudgetById(any()) } returns Unit

        // When
        viewModel.deleteBudget(budgetId)
        advanceUntilIdle()

        // Then
        coVerify { budgetRepository.deleteBudgetById(1L) }
    }

    @Test
    fun `deleteBudget on error sets error in uiState`() = runTest {
        // Given
        val budgetId = 1L
        coEvery { budgetRepository.deleteBudgetById(any()) } throws Exception("Delete failed")

        // When
        viewModel.deleteBudget(budgetId)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertNotNull(uiState.error)
        assertEquals("Delete failed", uiState.error)
    }

    // ==================== 错误处理测试 ====================

    @Test
    fun `clearError removes error from uiState`() = runTest {
        // Given
        val budgetData = BudgetData(
            title = "Error Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis()
        )
        coEvery { budgetRepository.createBudget(any(), any(), any(), any(), any(), any(), any(), any()) } throws Exception("Test error")

        // When
        viewModel.createBudget(budgetData)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    // ==================== BudgetData 转换测试 ====================

    @Test
    fun `BudgetData calculates progress correctly`() {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Test Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            spentAmount = 250.0
        )

        // Then
        assertEquals(0.5f, budgetData.progress, 0.01f)
    }

    @Test
    fun `BudgetData progress is clamped to 1_0`() {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Over Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            spentAmount = 750.0 // 150% of budget
        )

        // Then
        assertEquals(1.0f, budgetData.progress, 0.01f)
    }

    @Test
    fun `BudgetData calculates remaining amount correctly`() {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Test Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            spentAmount = 250.0
        )

        // Then
        assertEquals(250.0, budgetData.remainingAmount, 0.01)
    }

    @Test
    fun `BudgetData remaining amount is clamped to 0_0`() {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Over Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            spentAmount = 750.0
        )

        // Then
        assertEquals(0.0, budgetData.remainingAmount, 0.01)
    }

    @Test
    fun `BudgetData detects over budget correctly`() {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Over Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            spentAmount = 550.0
        )

        // Then
        assertTrue(budgetData.isOverBudget)
    }

    @Test
    fun `BudgetData status is HEALTHY when under 80%`() {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Healthy Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            spentAmount = 250.0 // 50%
        )

        // Then
        assertEquals(BudgetStatus.HEALTHY, budgetData.status)
    }

    @Test
    fun `BudgetData status is WARNING when between 80% and 90%`() {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Warning Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            spentAmount = 425.0 // 85%
        )

        // Then
        assertEquals(BudgetStatus.WARNING, budgetData.status)
    }

    @Test
    fun `BudgetData status is CRITICAL when between 90% and 100%`() {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Critical Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            spentAmount = 475.0 // 95%
        )

        // Then
        assertEquals(BudgetStatus.CRITICAL, budgetData.status)
    }

    @Test
    fun `BudgetData status is OVER_BUDGET when over 100%`() {
        // Given
        val budgetData = BudgetData(
            id = 1,
            title = "Over Budget",
            amount = 500.0,
            periodType = BudgetPeriodType.MONTHLY,
            startDate = System.currentTimeMillis(),
            spentAmount = 550.0 // 110%
        )

        // Then
        assertEquals(BudgetStatus.OVER_BUDGET, budgetData.status)
    }
}
