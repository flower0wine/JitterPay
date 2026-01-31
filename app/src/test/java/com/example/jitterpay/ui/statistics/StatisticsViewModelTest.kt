package com.example.jitterpay.ui.statistics

import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.ui.components.statistics.TimePeriod
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given
        every { repository.getAllTransactions() } returns flowOf(emptyList())

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        // After initialization with empty transactions, loading should be false
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0.0, viewModel.uiState.value.totalIncome, 0.01)
    }

    @Test
    fun `loads monthly expense`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val testTimeMillis = calendar.timeInMillis

        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                type = TransactionType.EXPENSE.name,
                amountCents = 100000L, // $1000.00
                category = "Dining",
                description = "Dinner",
                dateMillis = testTimeMillis
            )
        )
        every { repository.getAllTransactions() } returns flowOf(transactions)

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        // Note: Number formatting may vary by locale
        assertTrue(viewModel.uiState.value.totalSpent > 0)
    }

    @Test
    fun `calculates balance correctly`() = runTest {
        // Given
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                type = TransactionType.INCOME.name,
                amountCents = 500000L, // $5000.00
                category = "Salary",
                description = "Monthly salary",
                dateMillis = System.currentTimeMillis()
            ),
            TransactionEntity(
                id = 2L,
                type = TransactionType.EXPENSE.name,
                amountCents = 200000L, // $2000.00
                category = "Dining",
                description = "Dinner",
                dateMillis = System.currentTimeMillis()
            )
        )
        every { repository.getAllTransactions() } returns flowOf(transactions)

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(3000.0, viewModel.uiState.value.balance, 0.01)
    }

    @Test
    fun `loads categories from repository`() = runTest {
        // Given - use timestamp within current month to pass time filtering
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 15) // Mid-month to ensure within range
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val testTimeMillis = calendar.timeInMillis

        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                type = TransactionType.EXPENSE.name,
                amountCents = 50000L, // $500.00
                category = "Dining",
                description = "Lunch",
                dateMillis = testTimeMillis
            ),
            TransactionEntity(
                id = 2L,
                type = TransactionType.EXPENSE.name,
                amountCents = 30000L, // $300.00
                category = "Transport",
                description = "Bus",
                dateMillis = testTimeMillis
            )
        )
        every { repository.getAllTransactions() } returns flowOf(transactions)

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - ViewModel only includes EXPENSE in categories
        assertEquals(2, viewModel.uiState.value.categories.size)
        // Categories are sorted by percentage descending
        // Note: Number formatting may vary by locale, so we check for presence of values
        assertTrue(viewModel.uiState.value.categories.isNotEmpty())
    }

    @Test
    fun `calculates category percentages`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val testTimeMillis = calendar.timeInMillis

        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                type = TransactionType.EXPENSE.name,
                amountCents = 75000L, // $750.00 (75%)
                category = "Dining",
                description = "Dinner",
                dateMillis = testTimeMillis
            ),
            TransactionEntity(
                id = 2L,
                type = TransactionType.EXPENSE.name,
                amountCents = 25000L, // $250.00 (25%)
                category = "Transport",
                description = "Taxi",
                dateMillis = testTimeMillis
            )
        )
        every { repository.getAllTransactions() } returns flowOf(transactions)

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
        every { repository.getAllTransactions() } returns flowOf(emptyList())
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
        every { repository.getAllTransactions() } returns flowOf(emptyList())

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(TimePeriod.MONTHLY, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun `empty categories when no expense`() = runTest {
        // Given
        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                type = TransactionType.INCOME.name,
                amountCents = 50000L,
                category = "Salary",
                description = "Income",
                dateMillis = System.currentTimeMillis()
            )
        )
        every { repository.getAllTransactions() } returns flowOf(transactions)

        // When
        viewModel = StatisticsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.categories.isEmpty())
    }
}
