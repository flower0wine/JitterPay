package com.example.jitterpay.ui.home

import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
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
import org.junit.Test

/**
 * HomeViewModel 单元测试
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TransactionRepository
    private lateinit var viewModel: HomeViewModel

    private val testTransactions = listOf(
        TransactionEntity(
            id = 1,
            type = TransactionType.EXPENSE.name,
            amountCents = 550L,
            category = "Dining",
            dateMillis = System.currentTimeMillis()
        ),
        TransactionEntity(
            id = 2,
            type = TransactionType.INCOME.name,
            amountCents = 5000L,
            category = "Salary",
            dateMillis = System.currentTimeMillis()
        )
    )

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
        every { repository.getAllTransactions() } returns flowOf(emptyList())
        every { repository.getTotalBalance() } returns flowOf(0L)

        // When
        viewModel = HomeViewModel(repository)

        // Then - immediately after creation
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads transactions on initialization`() = runTest {
        // Given
        every { repository.getAllTransactions() } returns flowOf(testTransactions)
        every { repository.getTotalBalance() } returns flowOf(4450L)

        // When
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.uiState.value.transactions.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads total balance`() = runTest {
        // Given
        val expectedBalance = 4450L // 5000 - 550
        every { repository.getAllTransactions() } returns flowOf(testTransactions)
        every { repository.getTotalBalance() } returns flowOf(expectedBalance)

        // When
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedBalance, viewModel.uiState.value.totalBalance)
    }

    @Test
    fun `deleteTransaction calls repository`() = runTest {
        // Given
        every { repository.getAllTransactions() } returns flowOf(testTransactions)
        every { repository.getTotalBalance() } returns flowOf(0L)
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.deleteTransaction(testTransactions[0])
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.deleteTransaction(testTransactions[0]) }
    }

    @Test
    fun `refresh reloads data`() = runTest {
        // Given
        every { repository.getAllTransactions() } returns flowOf(testTransactions)
        every { repository.getTotalBalance() } returns flowOf(0L)
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should be called at least twice (initial + refresh)
        coVerify(atLeast = 2) { repository.getAllTransactions() }
    }

    @Test
    fun `empty transactions list when no data`() = runTest {
        // Given
        every { repository.getAllTransactions() } returns flowOf(emptyList())
        every { repository.getTotalBalance() } returns flowOf(0L)

        // When
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.transactions.isEmpty())
    }

    @Test
    fun `initial balance is zero`() = runTest {
        // Given
        every { repository.getAllTransactions() } returns flowOf(emptyList())
        every { repository.getTotalBalance() } returns flowOf(0L)

        // When
        viewModel = HomeViewModel(repository)

        // Then
        assertEquals(0L, viewModel.uiState.value.totalBalance)
    }
}
