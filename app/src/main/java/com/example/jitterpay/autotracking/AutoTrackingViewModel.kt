package com.example.jitterpay.autotracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.autotracking.service.AutoTrackingAccessibilityService
import com.example.jitterpay.autotracking.util.AutoTrackingPermissions
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.domain.model.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing auto-tracking feature
 *
 * Responsibilities:
 * - Manage auto-tracking toggle state
 * - Check and handle permissions
 * - Save transactions received from overlay service
 */
@HiltViewModel
class AutoTrackingViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutoTrackingUiState())
    val uiState: StateFlow<AutoTrackingUiState> = _uiState.asStateFlow()

    // Broadcast receiver for saving transactions from overlay service
    private val transactionSaveReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if (intent.action == "com.example.jitterpay.action.SAVE_TRANSACTION") {
                saveTransactionFromBroadcast(intent)
            }
        }
    }

    init {
        // Register broadcast receiver for transaction saving
        val filter = IntentFilter("com.example.jitterpay.action.SAVE_TRANSACTION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(transactionSaveReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(transactionSaveReceiver, filter)
        }

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
     * Save transaction from overlay service
     */
    private fun saveTransactionFromBroadcast(intent: Intent) {
        val amountStr = intent.getStringExtra("amount") ?: return
        val paymentMethod = intent.getStringExtra("payment_method") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val category = intent.getStringExtra("category") ?: return
        val typeStr = intent.getStringExtra("type") ?: "EXPENSE"

        viewModelScope.launch {
            try {
                // Parse amount to cents
                val money = Money.parse(amountStr) ?: Money.ZERO
                val amountCents = money.toCents()

                // Determine transaction type
                val type = try {
                    TransactionType.valueOf(typeStr)
                } catch (e: IllegalArgumentException) {
                    TransactionType.EXPENSE
                }

                // Save transaction
                transactionRepository.addTransaction(
                    type = type,
                    amountCents = amountCents,
                    category = category,
                    description = "$paymentMethod - $description",
                    dateMillis = System.currentTimeMillis()
                )

                _uiState.value = _uiState.value.copy(
                    lastSavedTransaction = "Saved: $amountStr - $category",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save transaction: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear last saved transaction message
     */
    fun clearLastSavedTransaction() {
        _uiState.value = _uiState.value.copy(lastSavedTransaction = null)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(transactionSaveReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
}

/**
 * UI state for auto-tracking feature
 */
data class AutoTrackingUiState(
    val isAutoTrackingEnabled: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val lastSavedTransaction: String? = null,
    val error: String? = null
)
