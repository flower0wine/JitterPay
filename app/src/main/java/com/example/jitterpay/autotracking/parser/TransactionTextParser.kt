package com.example.jitterpay.autotracking.parser

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jitterpay.autotracking.model.AmountExtraction
import com.example.jitterpay.autotracking.model.AppWhitelist
import com.example.jitterpay.autotracking.model.DescriptionExtraction
import com.example.jitterpay.autotracking.model.NodeType
import com.example.jitterpay.autotracking.model.PatternType
import com.example.jitterpay.autotracking.model.PaymentSuccessIndicator
import com.example.jitterpay.autotracking.model.ScreenConfig
import com.example.jitterpay.autotracking.model.TextPattern
import com.example.jitterpay.autotracking.model.TextProperty
import com.example.jitterpay.autotracking.model.TransactionInfo

/**
 * Enhanced adapter for extracting transaction information from screen text
 * Uses both node-specific targeting and pattern matching for accurate extraction
 */
class TransactionTextParser {

    /**
     * Parse transaction information from screen using node-specific targeting first,
     * then fallback to pattern matching if needed
     *
     * @param rootNode The root accessibility node
     * @param screenConfig The screen configuration containing text patterns
     * @return Parsed transaction information
     */
    fun parseTransactionFromNodes(
        rootNode: AccessibilityNodeInfo?,
        screenConfig: ScreenConfig
    ): TransactionInfo {
        if (rootNode == null) {
            return TransactionInfo()
        }

        // Try node-specific targeting first
        val result = tryNodeSpecificExtraction(rootNode, screenConfig)

        // If node-specific extraction didn't find everything, fallback to pattern matching
        if (result.amount == null || result.status == null) {
            val fallbackResult = parseTransaction(
                extractAllTextFromNodes(rootNode),
                screenConfig
            )
            return TransactionInfo(
                amount = result.amount ?: fallbackResult.amount,
                status = result.status ?: fallbackResult.status,
                description = result.description ?: fallbackResult.description,
                paymentMethod = result.paymentMethod ?: fallbackResult.paymentMethod
            )
        }

        return result
    }

    /**
     * Parse transaction information from screen text using the given screen configuration
     *
     * @param screenText The full text content from the screen
     * @param screenConfig The screen configuration containing text patterns
     * @return Parsed transaction information
     */
    fun parseTransaction(
        screenText: String,
        screenConfig: ScreenConfig
    ): TransactionInfo {
        var amount: String? = null
        var status: String? = null
        var description: String? = null
        var paymentMethod: String? = null

        // Sort patterns by priority (higher priority first)
        val sortedPatterns = screenConfig.textPatterns.sortedByDescending { it.priority }

        for (pattern in sortedPatterns) {
            when (pattern.type) {
                PatternType.AMOUNT -> {
                    if (amount == null) {
                        amount = extractAmount(screenText, pattern.pattern)
                    }
                }
                PatternType.PAYMENT_STATUS -> {
                    if (status == null) {
                        status = extractMatch(screenText, pattern.pattern)
                    }
                }
                PatternType.DESCRIPTION -> {
                    if (description == null) {
                        description = extractMatch(screenText, pattern.pattern)
                    }
                }
                PatternType.PAYMENT_METHOD -> {
                    if (paymentMethod == null) {
                        paymentMethod = extractMatch(screenText, pattern.pattern)
                    }
                }
            }
        }

        // Use default payment method from screen config if not found
        if (paymentMethod == null && screenConfig.paymentMethod.isNotBlank()) {
            paymentMethod = screenConfig.paymentMethod
        }

        return TransactionInfo(
            amount = amount,
            status = status,
            description = description,
            paymentMethod = paymentMethod
        )
    }

    /**
     * Try to extract information using node-specific targeting
     *
     * @param rootNode The root accessibility node
     * @param screenConfig The screen configuration
     * @return Parsed transaction information
     */
    private fun tryNodeSpecificExtraction(
        rootNode: AccessibilityNodeInfo,
        screenConfig: ScreenConfig
    ): TransactionInfo {
        var amount: String? = null
        var status: String? = null
        var description: String? = null

        // Check for payment success indicator first
        val isPaymentSuccess = screenConfig.paymentSuccessIndicator?.let { indicator ->
            isPaymentSuccessNode(rootNode, indicator)
        } ?: false

        if (!isPaymentSuccess) {
            // Not a payment success page, return empty result
            return TransactionInfo()
        }

        // Extract amount using node-specific targeting
        amount = screenConfig.amountExtraction?.let { extraction ->
            extractAmountFromNode(rootNode, extraction)
        }

        // Extract description using node-specific targeting
        description = screenConfig.descriptionExtraction?.let { extraction ->
            extractDescriptionFromNode(rootNode, extraction)
        }

        // Set status from indicator
        status = screenConfig.paymentSuccessIndicator?.textValue

        val paymentMethod = screenConfig.paymentMethod.takeIf { it.isNotBlank() }

        return TransactionInfo(
            amount = amount,
            status = status,
            description = description,
            paymentMethod = paymentMethod
        )
    }

    /**
     * Check if the screen shows a payment success message using node targeting
     *
     * @param rootNode The root accessibility node
     * @param indicator The payment success indicator configuration
     * @return True if payment success is detected
     */
    private fun isPaymentSuccessNode(
        rootNode: AccessibilityNodeInfo,
        indicator: PaymentSuccessIndicator
    ): Boolean {
        // Search for node with matching content description or text
        val successNode = findNodeByProperty(
            rootNode,
            indicator.nodeType,
            indicator.textProperty,
            indicator.textValue
        )

        if (successNode != null) {
            return true
        }

        // Fallback to regex patterns on full text
        val fullText = extractAllTextFromNodes(rootNode)
        return indicator.fallbackPatterns.any { pattern ->
            Regex(pattern).containsMatchIn(fullText)
        }
    }

    /**
     * Extract amount from specific node
     *
     * @param rootNode The root accessibility node
     * @param extraction The amount extraction configuration
     * @return The extracted amount or null
     */
    private fun extractAmountFromNode(
        rootNode: AccessibilityNodeInfo,
        extraction: AmountExtraction
    ): String? {
        // Find TextView with amount
        val amountNodes = findNodesByType(
            rootNode,
            extraction.nodeType
        )

        for (node in amountNodes) {
            val text = when (extraction.textProperty) {
                TextProperty.TEXT -> node.text?.toString()
                TextProperty.CONTENT_DESCRIPTION -> node.contentDescription?.toString()
            } ?: continue

            // Try to extract amount from this text
            val amount = extractAmount(text, extraction.currencyPattern)
            if (amount != null) {
                return amount
            }
        }

        return null
    }

    /**
     * Extract description (merchant name) from specific node
     *
     * @param rootNode The root accessibility node
     * @param extraction The description extraction configuration
     * @return The extracted description or null
     */
    private fun extractDescriptionFromNode(
        rootNode: AccessibilityNodeInfo,
        extraction: DescriptionExtraction
    ): String? {
        // Find TextViews that might contain merchant/description
        val textNodes = findNodesByType(rootNode, extraction.nodeType)

        for (node in textNodes) {
            val text = when (extraction.textProperty) {
                TextProperty.TEXT -> node.text?.toString()
                TextProperty.CONTENT_DESCRIPTION -> node.contentDescription?.toString()
            } ?: continue

            // Check if this text contains any of the context keywords
            if (extraction.contextKeywords.any { keyword ->
                    text.contains(keyword, ignoreCase = true)
                }
            ) {
                return text.trim()
            }
        }

        return null
    }

    /**
     * Find node by type and property value
     */
    private fun findNodeByProperty(
        rootNode: AccessibilityNodeInfo,
        nodeType: NodeType,
        textProperty: TextProperty,
        textValue: String
    ): AccessibilityNodeInfo? {
        val className = when (nodeType) {
            NodeType.TEXT_VIEW -> "android.widget.TextView"
            NodeType.BUTTON -> "android.widget.Button"
            NodeType.IMAGE_VIEW -> "android.widget.ImageView"
            NodeType.VIEW_GROUP -> "android.view.ViewGroup"
            NodeType.ANY -> null
        }

        val nodes = findNodesByType(rootNode, nodeType)

        for (node in nodes) {
            val nodeValue = when (textProperty) {
                TextProperty.TEXT -> node.text?.toString()
                TextProperty.CONTENT_DESCRIPTION -> node.contentDescription?.toString()
            }

            if (nodeValue == textValue) {
                return node
            }
        }

        return null
    }

    /**
     * Find all nodes of a specific type
     */
    private fun findNodesByType(
        rootNode: AccessibilityNodeInfo,
        nodeType: NodeType
    ): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()

        when (nodeType) {
            NodeType.ANY -> {
                // Collect all nodes
                collectAllNodes(rootNode, result)
            }
            else -> {
                val className = when (nodeType) {
                    NodeType.TEXT_VIEW -> "android.widget.TextView"
                    NodeType.BUTTON -> "android.widget.Button"
                    NodeType.IMAGE_VIEW -> "android.widget.ImageView"
                    NodeType.VIEW_GROUP -> "android.view.ViewGroup"
                    NodeType.ANY -> null
                }

                // Collect nodes with matching class name
                collectNodesByClassName(rootNode, className, result)
            }
        }

        return result
    }

    /**
     * Collect all nodes from the tree
     */
    private fun collectAllNodes(
        node: AccessibilityNodeInfo,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        result.add(node)
        for (i in 0 until node.childCount) {
            collectAllNodes(node.getChild(i) ?: continue, result)
        }
    }

    /**
     * Collect nodes with matching class name
     */
    private fun collectNodesByClassName(
        node: AccessibilityNodeInfo,
        className: String?,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        if (className == null || node.className?.toString()?.endsWith(className) == true) {
            result.add(node)
        }

        for (i in 0 until node.childCount) {
            collectNodesByClassName(node.getChild(i) ?: continue, className, result)
        }
    }

    /**
     * Extract monetary amount from text using regex pattern
     * Handles various currency formats: $123.45, ¥123.45, ￥123.45, 123.45, etc.
     *
     * @param text The text to search in
     * @param pattern The regex pattern to match
     * @return The extracted amount string (e.g., "123.45") or null if not found
     */
    private fun extractAmount(text: String, pattern: String): String? {
        try {
            val regex = Regex(pattern)
            val match = regex.find(text) ?: return null

            // Check if pattern has a capture group
            val capturedValue = match.groups[1]?.value

            // If no capture group, try to extract the amount from the full match
            val amountText = capturedValue ?: match.value

            // Remove any currency symbols, commas, and whitespace
            val cleanedAmount = amountText
                .replace(Regex("[￥¥$€£₹]"), "")  // Include full-width ￥
                .replace(",", "")
                .trim()

            // Validate that it's a valid amount format
            val amountPattern = Regex("\\d+\\.\\d{2}|\\d+")
            if (cleanedAmount.matches(amountPattern)) {
                // Ensure exactly 2 decimal places
                return if (cleanedAmount.contains(".")) {
                    val parts = cleanedAmount.split(".")
                    "${parts[0]}.${parts[1].take(2).padEnd(2, '0')}"
                } else {
                    "$cleanedAmount.00"
                }
            }

            return null
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Extract text using regex pattern
     *
     * @param text The text to search in
     * @param pattern The regex pattern to match
     * @return The matched text or null if not found
     */
    private fun extractMatch(text: String, pattern: String): String? {
        return try {
            val regex = Regex(pattern)
            regex.find(text)?.value?.trim()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if the screen text indicates a successful transaction
     *
     * @param transactionInfo The parsed transaction information
     * @return True if transaction is successful
     */
    fun isPaymentSuccessful(transactionInfo: TransactionInfo): Boolean {
        val status = transactionInfo.status ?: return false

        // Check for success indicators in various languages
        val successPatterns = listOf(
            "success", "successful", "成功", "完成", "completed",
            "confirmed", "确认", "done"
        )

        val lowerStatus = status.lowercase()
        return successPatterns.any { lowerStatus.contains(it) }
    }

    /**
     * Get all text content from accessibility node hierarchy
     * Traverses the node tree recursively to collect all visible text
     *
     * @param rootNode The root accessibility node
     * @return Concatenated text content from all nodes
     */
    fun extractAllTextFromNodes(
        rootNode: AccessibilityNodeInfo?
    ): String {
        if (rootNode == null) return ""

        val textBuilder = StringBuilder()
        collectTextFromNode(rootNode, textBuilder)
        return textBuilder.toString()
    }

    /**
     * Recursively collect text from a node and its children
     *
     * @param node The current node
     * @param textBuilder StringBuilder to append text to
     */
    private fun collectTextFromNode(
        node: AccessibilityNodeInfo,
        textBuilder: StringBuilder
    ) {
        // Add text from this node
        node.text?.let {
            if (it.isNotBlank()) {
                textBuilder.append(it).append(" ")
            }
        }

        node.contentDescription?.let {
            if (it.isNotBlank()) {
                textBuilder.append(it).append(" ")
            }
        }

        // Recursively collect from children
        for (i in 0 until node.childCount) {
            collectTextFromNode(node.getChild(i) ?: continue, textBuilder)
        }
    }
}
