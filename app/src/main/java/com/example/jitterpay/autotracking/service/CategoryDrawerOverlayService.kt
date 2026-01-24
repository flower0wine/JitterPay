package com.example.jitterpay.autotracking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.example.jitterpay.R
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.domain.model.Money
import com.example.jitterpay.ui.autotracking.CategoryDrawerContent
import com.example.jitterpay.ui.theme.JitterPayTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Overlay service for displaying category drawer
 *
 * Shows a bottom drawer overlay when a payment is detected,
 * allowing users to select transaction category.
 */
@AndroidEntryPoint
class CategoryDrawerOverlayService : android.app.Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    // Service scope for coroutines
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Inject repository for direct data persistence
    @Inject
    lateinit var transactionRepository: TransactionRepository

    // Transaction data received from accessibility service
    private val _transactionData = MutableStateFlow<TransactionData?>(null)
    val transactionData: StateFlow<TransactionData?> = _transactionData

    // Timestamp tracking
    private var serviceStartTime: Long = 0
    private var showOverlayTime: Long = 0
    private var viewAddedTime: Long = 0

    data class TransactionData(
        val amount: String,
        val paymentMethod: String,
        val description: String
    )

    companion object {
        const val ACTION_SHOW_DRAWER = "com.example.jitterpay.autotracking.SHOW_CATEGORY_DRAWER"
        const val EXTRA_TRANSACTION_AMOUNT = "transaction_amount"
        const val EXTRA_TRANSACTION_METHOD = "transaction_method"
        const val EXTRA_TRANSACTION_DESCRIPTION = "transaction_description"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "category_drawer_overlay_channel"
        private const val CHANNEL_NAME = "Category Drawer Overlay"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // No binding needed
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceStartTime = System.currentTimeMillis()

        // Start foreground service to prevent immediate destruction
        val notification = createNotification()
        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e("OverlayService", "[START] Failed to start foreground: ${e.message}", e)
        }

        intent ?: run {
            Log.e("OverlayService", "[START] Intent is null!")
            return START_NOT_STICKY
        }


        when (intent.action) {
            ACTION_SHOW_DRAWER -> {
                val amount = intent.getStringExtra(EXTRA_TRANSACTION_AMOUNT) ?: run {
                    Log.e("OverlayService", "[START] Missing EXTRA_TRANSACTION_AMOUNT")
                    return START_NOT_STICKY
                }
                val paymentMethod =
                    intent.getStringExtra(EXTRA_TRANSACTION_METHOD) ?: run {
                        Log.e("OverlayService", "[START] Missing EXTRA_TRANSACTION_METHOD")
                        return START_NOT_STICKY
                    }
                val description =
                    intent.getStringExtra(EXTRA_TRANSACTION_DESCRIPTION) ?: run {
                        Log.e("OverlayService", "[START] Missing EXTRA_TRANSACTION_DESCRIPTION")
                        return START_NOT_STICKY
                    }

                showOverlay(amount, paymentMethod, description)
            }
            else -> {
                Log.w("OverlayService", "[START] Unknown action: ${intent.action}")
            }
        }

        return START_NOT_STICKY
    }

    /**
     * Show the category drawer overlay
     */
    private fun showOverlay(amount: String, paymentMethod: String, description: String) {
        showOverlayTime = System.currentTimeMillis()

        // Update transaction data
        _transactionData.value = TransactionData(amount, paymentMethod, description)

        // Remove existing overlay if any
        hideOverlay()

        // Create container for ComposeView
        val container = FrameLayout(this).apply {
            // Transparent background - Compose UI will handle the dimming
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        // Set up lifecycle owner
        container.setViewTreeLifecycleOwner(this)
        container.setViewTreeViewModelStoreOwner(this)
        container.setViewTreeSavedStateRegistryOwner(this)


        // Create themed context for ComposeView
        val themedContext = android.view.ContextThemeWrapper(
            this,
            R.style.Theme_JitterPay
        ).apply {
            setTheme(R.style.Theme_JitterPay)
        }

        // Create ComposeView
        overlayView = ComposeView(themedContext).apply {
            setContent {
                JitterPayTheme {
                    CategoryDrawerContent(
                        transactionData = transactionData,
                        onCategorySelected = { category ->
                            // Save transaction with selected category
                            saveTransaction(amount, paymentMethod, description, category)
                            hideOverlay()
                            stopSelf()
                        },
                        onCancel = {
                            hideOverlay()
                            stopSelf()
                        }
                    )
                }
            }
        }

        // Add ComposeView to container
        container.addView(overlayView, android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ))

        // Add container to window
        try {
            val layoutParams = getWindowLayoutParams()

            windowManager.addView(container, layoutParams)
            viewAddedTime = System.currentTimeMillis()

            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        } catch (e: SecurityException) {
            Log.e("OverlayService", "[OVERLAY] SecurityException: ${e.message}", e)
            Log.e("OverlayService", "[OVERLAY] This may be a permission issue - check SYSTEM_ALERT_WINDOW permission")
            overlayView = null
        } catch (e: Exception) {
            Log.e("OverlayService", "[OVERLAY] Exception adding view: ${e.message}", e)
            e.printStackTrace()
            overlayView = null
        }
    }

    /**
     * Hide and remove the overlay
     */
    private fun hideOverlay() {
        try {
            overlayView?.let { view ->
                val parent = view.parent as? View ?: run {
                    return@let
                }
                windowManager.removeView(parent)
            }
            // Don't set lifecycle to DESTROYED here - only in onDestroy()
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        } catch (e: Exception) {
            Log.e("OverlayService", "[HIDE] Exception hiding overlay: ${e.message}", e)
            e.printStackTrace()
        } finally {
            overlayView = null
        }
    }

    /**
     * Get window layout parameters for the overlay
     */
    private fun getWindowLayoutParams(): WindowManager.LayoutParams {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Removed FLAG_NOT_FOCUSABLE to ensure overlay can properly attach and display
        // FLAG_LAYOUT_IN_SCREEN allows overlay to extend into status/navigation bar areas
        val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS


        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER
        }
    }

    /**
     * Save the transaction to database
     *
     * Uses Hilt-injected TransactionRepository directly instead of broadcast.
     * This is more reliable as it doesn't depend on UI lifecycle.
     */
    private fun saveTransaction(
        amount: String,
        paymentMethod: String,
        description: String,
        category: String
    ) {
        serviceScope.launch {
            try {
                // Parse amount to cents
                val money = Money.parse(amount) ?: Money.ZERO
                val amountCents = money.toCents()

                // Save transaction directly to repository
                transactionRepository.addTransaction(
                    type = TransactionType.EXPENSE, // Default to expense
                    amountCents = amountCents,
                    category = category,
                    description = "$paymentMethod - $description",
                    dateMillis = System.currentTimeMillis()
                )

            } catch (e: Exception) {
                Log.e("OverlayService", "[SAVE] Failed to save transaction: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        hideOverlay()
        serviceScope.cancel()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    /**
     * Create notification channel for foreground service (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for category drawer overlay service"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Create notification for foreground service
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, com.example.jitterpay.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JitterPay Overlay")
            .setContentText("Select category for your transaction")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    // LifecycleOwner implementation
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    // ViewModelStoreOwner implementation
    override val viewModelStore: ViewModelStore
        get() = store

    // SavedStateRegistryOwner implementation
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}
