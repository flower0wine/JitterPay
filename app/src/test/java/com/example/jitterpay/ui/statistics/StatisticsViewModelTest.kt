package com.example.jitterpay.ui.statistics

import com.example.jitterpay.data.local.dao.CategoryTotal
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.ui.components.statistics.TimePeriod
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * StatisticsViewModel 单元测试
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TransactionRepository
    private lateinit var viewModel: StatisticsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given
        every { repository.getMonthlyIncome(any(), any()) } returns flowOf(0L)
        every { repository.getMonthlyExpense(any(), any()) } returns flowOf(0L)
        every { repository.getExpenseByCategory(any(), any()) } returns flowOf(emptyList())

        // When
        viewModel = StatisticsViewModel(repository)

        // Then
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads monthly income`() = runTest {
        // Given
        val expectedIncome = 500000L // $5000.00
        every { repository.getMonthlyIncome(any(), any()) } returns flowOf(expectedIncome)
        every { repository.getMonthlyExpense(any(), any()) } returns flowOf(0L)
        every { repository.getExpenseByCategory(any(), any()) } returns flowOf(emptyList())

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(5000.0, viewModel.uiState.value.totalIncome, 0.01)
    }

    @Test
    fun `loads monthly expense`() = runTest {
        // Given
        val expectedExpense = 100000L // $1000.00
        every { repository.getMonthlyIncome(any(), any()) } returns flowOf(0L)
        every { repository.getMonthlyExpense(any(), any()) } returns flowOf(expectedExpense)
        every { repository.getExpenseByCategory(any(), any()) } returns flowOf(emptyList())

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1000.0, viewModel.uiState.value.totalSpent, 0.01)
    }

    @Test
    fun `calculates balance correctly`() = runTest {
        // Given
        val income = 500000L // $5000.00
        val expense = 200000L // $2000.00
        every { repository.getMonthlyIncome(any(), any()) } returns flowOf(income)
        every { repository.getMonthlyExpense(any(), any()) } returns flowOf(expense)
        every { repository.getExpenseByCategory(any(), any()) } returns flowOf(emptyList())

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(3000.0, viewModel.uiState.value.balance, 0.01)
    }

    @Test
    fun `loads categories from repository`() = runTest {
        // Given
        val categoryTotals = listOf(
            CategoryTotal("Dining", 50000L), // $500.00
            CategoryTotal("Transport", 30000L) // $300.00
        )
        every { repository.getMonthlyIncome(any(), any()) } returns flowOf(0L)
        every { repository.getMonthlyExpense(any(), any()) } returns flowOf(80000L)
        every { repository.getExpenseByCategory(any(), any()) } returns flowOf(categoryTotals)

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.uiState.value.categories.size)
        assertEquals("Dining", viewModel.uiState.value.categories[0].name)
        assertEquals(500.0, viewModel.uiState.value.categories[0].amount, 0.01)
    }

    @Test
    fun `calculates category percentages`() = runTest {
        // Given
        val categoryTotals = listOf(
            CategoryTotal("Dining", 75000L), // $750.00 (75%)
            CategoryTotal("Transport", 25000L) // $250.00 (25%)
        )
        every { repository.getMonthlyIncome(any(), any()) } returns flowOf(0L)
        every { repository.getMonthlyExpense(any(), any()) } returns flowOf(100000L)
        every { repository.getExpenseByCategory(any(), any()) } returns flowOf(categoryTotals)

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(75.0, viewModel.uiState.value.categories[0].percentage, 0.1)
        assertEquals(25.0, viewModel.uiState.value.categories[1].percentage, 0.1)
    }

    @Test
    fun `selectPeriod updates selected period`() = runTest {
        // Given
        every { repository.getMonthlyIncome(any(), any()) } returns flowOf(0L)
        every { repository.getMonthlyExpense(any(), any()) } returns flowOf(0L)
        every { repository.getExpenseByCategory(any(), any()) } returns flowOf(emptyList())
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectPeriod(TimePeriod.WEEKLY)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(TimePeriod.WEEKLY, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun `default period is monthly`() = runTest {
        // Given
        every { repository.getMonthlyIncome(any(), any()) } returns flowOf(0L)
        every { repository.getMonthlyExpense(any(), any()) } returns flowOf(0L)
        every { repository.getExpenseByCategory(any(), any()) } returns flowOf(emptyList())

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(TimePeriod.MONTHLY, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun `empty categories when no expense`() = runTest {
        // Given
        every { repository.getMonthlyIncome(any(), any()) } returns flowOf(0L)
        every { repository.getMonthlyExpense(any(), any()) } returns flowOf(0L)
        every { repository.getExpenseByCategory(any(), any()) } returns flowOf(emptyList())

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.categories.isEmpty())
    }
}
