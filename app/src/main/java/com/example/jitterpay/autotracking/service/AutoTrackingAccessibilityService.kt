package com.example.jitterpay.autotracking.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.jitterpay.autotracking.model.DefaultWhitelists
import com.example.jitterpay.autotracking.model.TransactionInfo
import com.example.jitterpay.autotracking.parser.TransactionTextParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Accessibility Service for auto-tracking expenses
 *
 * Monitors window state changes and extracts transaction information from
 * whitelisted payment confirmation screens.
 *
 * Key responsibilities:
 * - Monitor window content changes
 * - Filter events to only process whitelisted apps
 * - Extract transaction information using text parser
 * - Show overlay for category selection when payment is detected
 */
@SuppressLint("AccessibilityPolicy")
class AutoTrackingAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var processingJob: Job? = null
    private val textParser = TransactionTextParser()

    // Flag to avoid processing the same screen multiple times
    private var lastProcessedEventTime: Long = 0
    private val MIN_EVENT_INTERVAL_MS = 500L // Minimum time between processing events

    // Timestamp tracking
    private var detectionTime: Long = 0

    companion object {
        const val ACTION_SHOW_CATEGORY_DRAWER =
            "com.example.jitterpay.autotracking.SHOW_CATEGORY_DRAWER"

        const val EXTRA_TRANSACTION_AMOUNT = "transaction_amount"
        const val EXTRA_TRANSACTION_METHOD = "transaction_method"
        const val EXTRA_TRANSACTION_DESCRIPTION = "transaction_description"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        // Filter to only process window state changes and content changes
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        // Get current package name
        val packageName = event.packageName?.toString() ?: return

        // Skip if it's our own app
        if (packageName == this.packageName) {
            return
        }

        // Check if package is in whitelist
        val whitelist = DefaultWhitelists.findByPackageName(packageName) ?: run {
            return
        }


        // Debounce: skip if processing too many events
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessedEventTime < MIN_EVENT_INTERVAL_MS) {
            return
        }

        // Only process when window state changes to avoid duplicate processing
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            processPaymentDetection(event, whitelist)
            lastProcessedEventTime = currentTime
        }
    }

    /**
     * Process payment detection from the event
     *
     * @param event The accessibility event
     * @param whitelist The app whitelist configuration
     */
    private fun processPaymentDetection(
        event: AccessibilityEvent,
        whitelist: com.example.jitterpay.autotracking.model.AppWhitelist
    ) {
        // Check if current activity matches any screen config
        val activityName = event.className?.toString() ?: return
        val screenConfig = whitelist.screenConfigs.find {
            activityName.contains(it.activityName)
        } ?: run {
            return
        }


        // Cancel any existing processing job
        processingJob?.cancel()

        // Start new processing job with polling to handle dynamic page content
        processingJob = serviceScope.launch {
            val pollIntervalMs = 100L
            val timeoutMs = 120_000L // 2 minutes
            val startTime = System.currentTimeMillis()
            var pollCount = 0

            try {
                while (true) {
                    pollCount++
                    // Check timeout
                    val elapsedMs = System.currentTimeMillis() - startTime
                    if (elapsedMs >= timeoutMs) {
                        Log.w("AutoTracking", "[POLLING] Timeout after ${elapsedMs}ms, ${pollCount} polls")
                        break
                    }

                    // Get root accessibility node
                    val rootNode = rootInActiveWindow
                    if (rootNode == null) {
                        delay(pollIntervalMs)
                        continue
                    }

                    // Use node-based parsing first (more accurate), fallback to pattern matching
                    val transactionInfo = textParser.parseTransactionFromNodes(rootNode, screenConfig)

                    // Check if this is a successful payment
                    val isPaymentSuccessful = textParser.isPaymentSuccessful(transactionInfo)


                    if (transactionInfo.isValid() && isPaymentSuccessful) {
                        detectionTime = System.currentTimeMillis()
                        // Show category drawer overlay
                        showCategoryDrawer(
                            transactionInfo,
                            whitelist.appName
                        )
                        break // Stop polling after successful detection
                    }

                    // Wait for next poll
                    delay(pollIntervalMs)
                }

            } catch (e: Exception) {
                // Log error but don't crash the service
                Log.e("AutoTracking", "[POLLING] Exception during polling: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }

    /**
     * Show category drawer overlay for user to select transaction category
     *
     * @param transactionInfo The extracted transaction information
     * @param sourceApp The name of the app where transaction occurred
     */
    private fun showCategoryDrawer(
        transactionInfo: TransactionInfo,
        sourceApp: String
    ) {

        val intent = Intent(ACTION_SHOW_CATEGORY_DRAWER).apply {
            putExtra(EXTRA_TRANSACTION_AMOUNT, transactionInfo.amount)
            putExtra(EXTRA_TRANSACTION_METHOD, transactionInfo.paymentMethod)
            putExtra(
                EXTRA_TRANSACTION_DESCRIPTION,
                "${sourceApp}: ${transactionInfo.description ?: sourceApp}"
            )
            // Ensure this works from service context
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Start the overlay service to show category drawer
        try {
            val serviceIntent = Intent(applicationContext, CategoryDrawerOverlayService::class.java).apply {
                putExtras(intent.extras ?: android.os.Bundle())
                action = CategoryDrawerOverlayService.ACTION_SHOW_DRAWER
            }
            val result = applicationContext.startService(serviceIntent)

            if (result == null) {
                Log.e("AutoTracking", "[DRAWER] startService returned null - service may not have started!")
            }
        } catch (e: SecurityException) {
            Log.e("AutoTracking", "[DRAWER] SecurityException: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("AutoTracking", "[DRAWER] Exception starting service: ${e.message}", e)
            e.printStackTrace()
        }

    }

    override fun onInterrupt() {
        // Called when service is interrupted
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Service is connected and ready to receive events
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any ongoing processing
        processingJob?.cancel()
    }
}
