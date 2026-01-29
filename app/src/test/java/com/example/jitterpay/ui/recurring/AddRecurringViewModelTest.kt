package com.example.jitterpay.ui.recurring

import com.example.jitterpay.data.repository.RecurringRepository
import com.example.jitterpay.scheduler.RecurringReminderScheduler
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
 * AddRecurringViewModel 单元测试
 *
 * 测试ViewModel的核心功能：
 * - 表单状态更新
 * - 保存验证
 * - 保存成功处理
 * - 错误处理
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddRecurringViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RecurringRepository
    private lateinit var reminderScheduler: RecurringReminderScheduler
    private lateinit var viewModel: AddRecurringViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        reminderScheduler = mockk(relaxed = true)
        viewModel = AddRecurringViewModel(repository, reminderScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== 状态更新测试 ====================

    @Test
    fun `initial state has correct defaults`() {
        // Then
        assertEquals("", viewModel.uiState.value.title)
        assertEquals("0.00", viewModel.uiState.value.amount)
        assertEquals("Transport", viewModel.uiState.value.category)
        assertEquals(RecurringFrequency.DAILY, viewModel.uiState.value.frequency)
        assertEquals("EXPENSE", viewModel.uiState.value.type)
        assertFalse(viewModel.uiState.value.saveSuccess)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `setTitle updates state correctly`() {
        // When
        viewModel.setTitle("Test Title")

        // Then
        assertEquals("Test Title", viewModel.uiState.value.title)
    }

    @Test
    fun `setAmount updates state correctly`() {
        // When
        viewModel.setAmount("15.99")

        // Then
        assertEquals("15.99", viewModel.uiState.value.amount)
    }

    @Test
    fun `setCategory updates state correctly`() {
        // When
        viewModel.setCategory("Entertainment")

        // Then
        assertEquals("Entertainment", viewModel.uiState.value.category)
    }

    @Test
    fun `setFrequency updates state correctly`() {
        // When
        viewModel.setFrequency(RecurringFrequency.MONTHLY)

        // Then
        assertEquals(RecurringFrequency.MONTHLY, viewModel.uiState.value.frequency)
    }

    @Test
    fun `setStartDate updates state correctly`() {
        // Given
        val testDate = 1704067200000L

        // When
        viewModel.setStartDate(testDate)

        // Then
        assertEquals(testDate, viewModel.uiState.value.startDate)
    }

    @Test
    fun `setType updates state correctly`() {
        // When
        viewModel.setType("INCOME")

        // Then
        assertEquals("INCOME", viewModel.uiState.value.type)
    }

    // ==================== 保存验证测试 ====================

    @Test
    fun `saveRecurring shows error when title is blank`() = runTest {
        // Given
        viewModel.setTitle("")
        viewModel.setAmount("10.00")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Please enter a title", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.saveSuccess)
        coVerify(exactly = 0) { repository.addRecurring(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `saveRecurring shows error when amount is invalid`() = runTest {
        // Given
        viewModel.setTitle("Test")
        viewModel.setAmount("invalid")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Please enter a valid amount", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `saveRecurring shows error when amount is zero`() = runTest {
        // Given
        viewModel.setTitle("Test")
        viewModel.setAmount("0.00")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Please enter a valid amount", viewModel.uiState.value.error)
    }

    @Test
    fun `saveRecurring shows error when amount is negative`() = runTest {
        // Given
        viewModel.setTitle("Test")
        viewModel.setAmount("-10.00")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Please enter a valid amount", viewModel.uiState.value.error)
    }

    @Test
    fun `saveRecurring shows error for empty amount string`() = runTest {
        // Given
        viewModel.setTitle("Test")
        viewModel.setAmount("")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Please enter a valid amount", viewModel.uiState.value.error)
    }

    // ==================== 保存成功测试 ====================

    @Test
    fun `saveRecurring calls repository with correct parameters`() = runTest {
        // Given
        coEvery {
            repository.addRecurring(
                title = "Morning Commute",
                amountCents = 350L,
                type = "EXPENSE",
                category = "Transport",
                frequency = RecurringFrequency.DAILY.name,
                startDateMillis = any()
            )
        } returns 1L

        viewModel.setTitle("Morning Commute")
        viewModel.setAmount("3.50")
        viewModel.setCategory("Transport")
        viewModel.setFrequency(RecurringFrequency.DAILY)
        viewModel.setType("EXPENSE")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.addRecurring(
                title = "Morning Commute",
                amountCents = 350L,
                type = "EXPENSE",
                category = "Transport",
                frequency = RecurringFrequency.DAILY.name,
                startDateMillis = any()
            )
        }
    }

    @Test
    fun `saveRecurring sets saveSuccess on successful save`() = runTest {
        // Given
        coEvery { repository.addRecurring(any(), any(), any(), any(), any(), any()) } returns 1L

        viewModel.setTitle("Test")
        viewModel.setAmount("10.00")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.saveSuccess)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `saveRecurring handles repository exception`() = runTest {
        // Given
        coEvery { repository.addRecurring(any(), any(), any(), any(), any(), any()) } throws Exception("Database error")

        viewModel.setTitle("Test")
        viewModel.setAmount("10.00")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.saveSuccess)
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Database error"))
    }

    @Test
    fun `saveRecurring clears previous error on success`() = runTest {
        // Given - First save with invalid data to set error
        viewModel.setTitle("")
        viewModel.setAmount("10.00")
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // Given - Now save with valid data
        coEvery { repository.addRecurring(any(), any(), any(), any(), any(), any()) } returns 1L
        viewModel.setTitle("Valid Title")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    // ==================== 金额转换测试 ====================

    @Test
    fun `saveRecurring converts amount correctly`() = runTest {
        // Given
        coEvery { repository.addRecurring(any(), any(), any(), any(), any(), any()) } returns 1L

        viewModel.setTitle("Test")
        viewModel.setAmount("15.99")

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.addRecurring(
                title = "Test",
                amountCents = 1599L, // 15.99 * 100
                type = "EXPENSE",
                category = "Transport",
                frequency = RecurringFrequency.DAILY.name,
                startDateMillis = any()
            )
        }
    }

    @Test
    fun `saveRecurring handles large amounts`() = runTest {
        // Given
        coEvery { repository.addRecurring(any(), any(), any(), any(), any(), any()) } returns 1L

        viewModel.setTitle("Salary")
        viewModel.setAmount("5000.00")
        viewModel.setType("INCOME")
        viewModel.setFrequency(RecurringFrequency.MONTHLY)

        // When
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.addRecurring(
                title = "Salary",
                amountCents = 500000L,
                type = "INCOME",
                category = "Transport",
                frequency = RecurringFrequency.MONTHLY.name,
                startDateMillis = any()
            )
        }
    }

    // ==================== 错误处理测试 ====================

    @Test
    fun `clearError clears error state`() = runTest {
        // Given - Set an error
        viewModel.setTitle("")
        viewModel.setAmount("10.00")
        viewModel.saveRecurring()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    // ==================== 多字段状态测试 ====================

    @Test
    fun `multiple state updates work together`() {
        // When - Set multiple fields
        viewModel.setTitle("Netflix")
        viewModel.setAmount("15.99")
        viewModel.setCategory("Entertainment")
        viewModel.setFrequency(RecurringFrequency.MONTHLY)
        viewModel.setType("EXPENSE")

        // Then - All fields are updated
        val state = viewModel.uiState.value
        assertEquals("Netflix", state.title)
        assertEquals("15.99", state.amount)
        assertEquals("Entertainment", state.category)
        assertEquals(RecurringFrequency.MONTHLY, state.frequency)
        assertEquals("EXPENSE", state.type)
    }

    @Test
    fun `default startDate is set to current time`() {
        // Then - startDate should be approximately current time
        val now = System.currentTimeMillis()
        assertTrue(viewModel.uiState.value.startDate <= now)
        assertTrue(viewModel.uiState.value.startDate > now - 60000) // Within last minute
    }
}
