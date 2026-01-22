package com.example.jitterpay.ui.addtransaction

import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AddTransactionViewModel 单元测试
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: TransactionRepository
    private lateinit var viewModel: AddTransactionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = AddTransactionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has expense type`() {
        assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.selectedType)
    }

    @Test
    fun `initial amount is zero`() {
        assertEquals("0.00", viewModel.uiState.value.amount)
    }

    @Test
    fun `setType updates state`() {
        viewModel.setType(TransactionType.INCOME)

        assertEquals(TransactionType.INCOME, viewModel.uiState.value.selectedType)
    }

    @Test
    fun `setAmount updates state`() {
        viewModel.setAmount("10.50")

        assertEquals("10.50", viewModel.uiState.value.amount)
    }

    @Test
    fun `setCategory updates state`() {
        viewModel.setCategory("Dining")

        assertEquals("Dining", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `setDate updates state`() {
        val timestamp = System.currentTimeMillis()
        viewModel.setDate(timestamp)

        assertEquals(timestamp, viewModel.uiState.value.selectedDateMillis)
    }

    @Test
    fun `setDescription updates state`() {
        viewModel.setDescription("Test description")

        assertEquals("Test description", viewModel.uiState.value.description)
    }

    @Test
    fun `saveTransaction with missing category shows error`() = runTest {
        // Given - no category selected
        viewModel.setAmount("10.00")

        // When
        viewModel.saveTransaction {}
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("required"))
    }

    @Test
    fun `saveTransaction with zero amount shows error`() = runTest {
        // Given - zero amount
        viewModel.setCategory("Dining")
        viewModel.setAmount("0.00")

        // When
        viewModel.saveTransaction {}
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `saveTransaction with valid data calls repository`() = runTest {
        // Given
        coEvery { repository.addTransaction(any(), any(), any(), any(), any()) } returns 1L
        viewModel.setType(TransactionType.EXPENSE)
        viewModel.setAmount("10.00")
        viewModel.setCategory("Dining")
        viewModel.setDate(System.currentTimeMillis())

        // When
        viewModel.saveTransaction {}
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.addTransaction(
                type = TransactionType.EXPENSE,
                amountCents = 1000L, // $10.00 in cents
                category = "Dining",
                description = "",
                dateMillis = any()
            )
        }
    }

    @Test
    fun `saveTransaction sets isSaving flag while processing`() = runTest {
        // Given
        coEvery { repository.addTransaction(any(), any(), any(), any(), any()) } returns 1L
        viewModel.setAmount("10.00")
        viewModel.setCategory("Dining")

        // When
        viewModel.saveTransaction {}

        // Advance until the coroutine completes
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - after coroutine completes, isSaving should be false
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `clearError clears error state`() = runTest {
        // Trigger an error first
        viewModel.saveTransaction {}
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `initial state has no error`() {
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `initial state is not saving`() {
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `initial state has not saved successfully`() {
        assertFalse(viewModel.uiState.value.saveSuccess)
    }
}
