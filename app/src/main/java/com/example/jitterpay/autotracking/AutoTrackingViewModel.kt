package com.example.jitterpay.autotracking

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.jitterpay.autotracking.service.AutoTrackingAccessibilityService
import com.example.jitterpay.autotracking.util.AutoTrackingPermissions
import com.example.jitterpay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for managing auto-tracking feature
 *
 * Responsibilities:
 * - Manage auto-tracking toggle state
 * - Check and handle permissions
 *
 * Note: Transaction saving is now handled directly by CategoryDrawerOverlayService
 * through Hilt-injected TransactionRepository. No broadcast receiver needed.
 */
@HiltViewModel
class AutoTrackingViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutoTrackingUiState())
    val uiState: StateFlow<AutoTrackingUiState> = _uiState.asStateFlow()

    init {
        // Check initial permission state
        checkPermissions()
    }

    /**
     * Check the current state of all required permissions
     */
    fun checkPermissions() {
        val accessibilityEnabled = AutoTrackingPermissions.isAccessibilityServiceEnabled(
            context,
            AutoTrackingAccessibilityService::class.java
        )
        val overlayGranted = AutoTrackingPermissions.canDrawOverlays(context)

        _uiState.value = _uiState.value.copy(
            isAccessibilityEnabled = accessibilityEnabled,
            isOverlayGranted = overlayGranted
        )
    }

    /**
     * Toggle auto-tracking feature
     */
    fun toggleAutoTracking(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            isAutoTrackingEnabled = enabled
        )
    }

    /**
     * Open accessibility service settings
     */
    fun openAccessibilitySettings(context: Context): Intent {
        return AutoTrackingPermissions.openAccessibilitySettings(
            context,
            AutoTrackingAccessibilityService::class.java
        )
    }

    /**
     * Open overlay permission settings
     */
    fun openOverlaySettings(context: Context): Intent {
        return AutoTrackingPermissions.openOverlayPermissionSettings(context)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for auto-tracking feature
 *
 * Note: lastSavedTransaction removed as it's no longer needed.
 * Transaction saving is handled directly by the service.
 */
data class AutoTrackingUiState(
    val isAutoTrackingEnabled: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val error: String? = null
)
