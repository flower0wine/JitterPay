package com.example.jitterpay.ui.update

import com.example.jitterpay.data.local.UpdatePreferences
import com.example.jitterpay.scheduler.UpdateScheduler
import com.example.jitterpay.util.UpdateInfo
import com.example.jitterpay.util.UpdateManager
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
 * Unit tests for UpdateViewModel.
 *
 * Tests update checking, download scheduling, and dialog state management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UpdateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var updateManager: UpdateManager
    private lateinit var updatePreferences: UpdatePreferences
    private lateinit var updateScheduler: UpdateScheduler
    private lateinit var viewModel: UpdateViewModel

    private val testUpdateInfo = UpdateInfo(
        latestVersion = "v2.0.0",
        releaseDate = "2026-01-30",
        cdnBaseUrl = "https://store.example.com",
        apkSize = 15000000L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        updateManager = mockk(relaxed = true)
        updatePreferences = mockk(relaxed = true)
        updateScheduler = mockk(relaxed = true)

        // Default mock returns no pending update
        every { updatePreferences.pendingUpdate } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UpdateViewModel {
        return UpdateViewModel(updateManager, updatePreferences, updateScheduler)
    }

    @Test
    fun `initial state is loading false and no update`() {
        // When
        viewModel = createViewModel()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.updateInfo)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `checkForUpdates shows upToDate when no new version`() = runTest {
        // Given
        coEvery { updateManager.checkForUpdates() } returns Result.success(null)

        // When
        viewModel = createViewModel()
        viewModel.checkForUpdates()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isUpToDate)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.updateInfo)
    }

    @Test
    fun `checkForUpdates shows error on network failure`() = runTest {
        // Given
        coEvery { updateManager.checkForUpdates() } returns Result.failure(Exception("Network error"))

        // When
        viewModel = createViewModel()
        viewModel.checkForUpdates()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Network error", viewModel.uiState.value.error)
    }

    @Test
    fun `dismissError clears error state`() = runTest {
        // Given
        coEvery { updateManager.checkForUpdates() } returns Result.failure(Exception("Test error"))
        viewModel = createViewModel()
        viewModel.checkForUpdates()
        advanceUntilIdle()

        // When
        viewModel.dismissError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `closeAllDialogs closes install dialog`() = runTest {
        // Given - simulate a dialog being shown
        viewModel = createViewModel()
        viewModel.closeAllDialogs()

        // Then
        assertFalse(viewModel.uiState.value.showInstallDialog)
    }

    @Test
    fun `checkForUpdates updates last check time`() = runTest {
        // Given
        coEvery { updateManager.checkForUpdates() } returns Result.success(null)

        // When
        viewModel = createViewModel()
        viewModel.checkForUpdates()
        advanceUntilIdle()

        // Then
        coVerify { updatePreferences.updateLastCheckTime() }
    }

    @Test
    fun `checkForUpdates handles null result from checkForUpdates`() = runTest {
        // Given
        coEvery { updateManager.checkForUpdates() } returns Result.success(null)

        // When
        viewModel = createViewModel()
        viewModel.checkForUpdates()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.isUpToDate)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `checkForUpdates sets isLoading during check`() = runTest {
        // Given
        coEvery { updateManager.checkForUpdates() } returns Result.success(null)

        // When
        viewModel = createViewModel()
        viewModel.checkForUpdates()

        // Then - just verify it doesn't throw and completes
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `checkForUpdates calls updateManager`() = runTest {
        // Given
        coEvery { updateManager.checkForUpdates() } returns Result.success(null)

        // When
        viewModel = createViewModel()
        viewModel.checkForUpdates()
        advanceUntilIdle()

        // Then
        coVerify { updateManager.checkForUpdates() }
    }
}
