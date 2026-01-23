package com.example.jitterpay.autotracking.model

/**
 * Data class representing a whitelisted app configuration
 * Defines which apps and which screens within those apps to monitor for auto-tracking
 */
data class AppWhitelist(
    val packageName: String,
    val appName: String,
    val screenConfigs: List<ScreenConfig>
)

/**
 * Configuration for a specific screen within an app
 * Defines how to extract transaction information from a specific screen
 */
data class ScreenConfig(
    val activityName: String,
    val screenName: String,
    val textPatterns: List<TextPattern>,
    val paymentMethod: String = "Unknown",

    // Node-specific targeting for more accurate detection
    val paymentSuccessIndicator: PaymentSuccessIndicator? = null,
    val amountExtraction: AmountExtraction? = null,
    val descriptionExtraction: DescriptionExtraction? = null
)

/**
 * Configuration for detecting payment success using node properties
 */
data class PaymentSuccessIndicator(
    val nodeType: NodeType,
    val textProperty: TextProperty,
    val textValue: String, // Exact match for contentDescription or text
    val fallbackPatterns: List<String> = emptyList() // Regex patterns as fallback
)

/**
 * Configuration for extracting amount from specific node
 */
data class AmountExtraction(
    val nodeType: NodeType,
    val textProperty: TextProperty,
    val currencyPattern: String = "[￥¥$€£₹]\\s*([\\d,]+\\.\\d{2})" // Include full-width ￥
)

/**
 * Configuration for extracting description from specific node
 */
data class DescriptionExtraction(
    val nodeType: NodeType,
    val textProperty: TextProperty,
    val contextKeywords: List<String> = emptyList() // Keywords to identify merchant/description
)

/**
 * Type of Android node to search for
 */
enum class NodeType {
    TEXT_VIEW,          // android.widget.TextView
    BUTTON,             // android.widget.Button
    IMAGE_VIEW,         // android.widget.ImageView
    VIEW_GROUP,         // android.view.ViewGroup
    ANY                 // Any node type
}

/**
 * Which property of the node to use for text extraction
 */
enum class TextProperty {
    TEXT,               // node.text
    CONTENT_DESCRIPTION // node.contentDescription
}

/**
 * Pattern for extracting information from screen text
 * Uses regex to match specific content patterns
 */
data class TextPattern(
    val pattern: String,  // Regex pattern
    val type: PatternType,
    val priority: Int = 0  // Higher priority patterns are checked first
)

/**
 * Types of information that can be extracted from text patterns
 */
enum class PatternType {
    AMOUNT,          // Extract monetary amount (e.g., "$123.45", "123.45")
    PAYMENT_STATUS,  // Extract payment status (e.g., "Payment Successful", "Completed")
    DESCRIPTION,     // Extract transaction description
    PAYMENT_METHOD   // Extract payment method (e.g., "Credit Card", "WeChat Pay")
}

/**
 * Result of parsing transaction information from screen text
 */
data class TransactionInfo(
    val amount: String? = null,
    val status: String? = null,
    val description: String? = null,
    val paymentMethod: String? = null
) {
    fun isValid(): Boolean {
        return !amount.isNullOrBlank() && !status.isNullOrBlank()
    }
}

/**
 * Predefined whitelists for common payment apps
 */
object DefaultWhitelists {
    val wechatPay = AppWhitelist(
        packageName = "com.tencent.mm",
        appName = "WeChat",
        screenConfigs = listOf(
            ScreenConfig(
                activityName = "com.tencent.mm.framework.app.UIPageFragmentActivity",
                screenName = "Payment Result",
                textPatterns = listOf(
                    TextPattern(
                        pattern = "[￥¥]\\s*([\\d,]+\\.\\d{2})", // Include both full-width and half-width
                        type = PatternType.AMOUNT,
                        priority = 1
                    ),
                    TextPattern(
                        pattern = "([\\d,]+\\.\\d{2})",
                        type = PatternType.AMOUNT,
                        priority = 2
                    ),
                    TextPattern(
                        pattern = "支付成功|Payment Successful",
                        type = PatternType.PAYMENT_STATUS
                    ),
                    TextPattern(
                        pattern = "零钱|余额|银行卡",
                        type = PatternType.PAYMENT_METHOD
                    ),
                    TextPattern(
                        pattern = "平台商户|商家",  // Match merchant patterns
                        type = PatternType.DESCRIPTION
                    )
                ),
                paymentMethod = "WeChat Pay",

                // Node-specific targeting for more accurate WeChat payment detection
                paymentSuccessIndicator = PaymentSuccessIndicator(
                    nodeType = NodeType.VIEW_GROUP,
                    textProperty = TextProperty.CONTENT_DESCRIPTION,
                    textValue = "支付成功",
                    fallbackPatterns = listOf("支付成功", "Payment Successful")
                ),
                amountExtraction = AmountExtraction(
                    nodeType = NodeType.TEXT_VIEW,
                    textProperty = TextProperty.TEXT,
                    currencyPattern = "[￥¥]\\s*([\\d,]+\\.\\d{2})"
                ),
                descriptionExtraction = DescriptionExtraction(
                    nodeType = NodeType.TEXT_VIEW,
                    textProperty = TextProperty.TEXT,
                    contextKeywords = listOf("商户", "商家", "收银台")
                )
            )
        )
    )

    val alipay = AppWhitelist(
        packageName = "com.eg.android.AlipayGphone",
        appName = "Alipay",
        screenConfigs = listOf(
            ScreenConfig(
                activityName = "com.alipay.mobile.payee.ui.PaymentResultActivity",
                screenName = "Payment Result",
                textPatterns = listOf(
                    TextPattern(
                        pattern = "支付成功|Payment Successful|[￥¥]\\s*([\\d,]+\\.\\d{2})",
                        type = PatternType.AMOUNT,
                        priority = 1
                    ),
                    TextPattern(
                        pattern = "([\\d,]+\\.\\d{2})",
                        type = PatternType.AMOUNT,
                        priority = 2
                    ),
                    TextPattern(
                        pattern = "支付成功|Payment Successful",
                        type = PatternType.PAYMENT_STATUS
                    ),
                    TextPattern(
                        pattern = "余额|花呗|银行卡",
                        type = PatternType.PAYMENT_METHOD
                    )
                ),
                paymentMethod = "Alipay"
            )
        )
    )

    // Example for a banking app
    val bankApp = AppWhitelist(
        packageName = "com.example.bank",
        appName = "Bank App",
        screenConfigs = listOf(
            ScreenConfig(
                activityName = "com.example.bank.ui.TransactionResultActivity",
                screenName = "Transaction Result",
                textPatterns = listOf(
                    TextPattern(
                        pattern = "成功|Success|\\$\\s*([\\d,]+\\.\\d{2})",
                        type = PatternType.AMOUNT,
                        priority = 1
                    ),
                    TextPattern(
                        pattern = "([\\d,]+\\.\\d{2})",
                        type = PatternType.AMOUNT,
                        priority = 2
                    ),
                    TextPattern(
                        pattern = "交易成功|Transfer Successful",
                        type = PatternType.PAYMENT_STATUS
                    ),
                    TextPattern(
                        pattern = "信用卡|储蓄卡|Checking|Savings",
                        type = PatternType.PAYMENT_METHOD
                    )
                ),
                paymentMethod = "Bank Card"
            )
        )
    )

    /**
     * Get all default whitelists
     */
    fun getAll(): List<AppWhitelist> = listOf(wechatPay, alipay, bankApp)

    /**
     * Find whitelist by package name
     */
    fun findByPackageName(packageName: String): AppWhitelist? {
        return getAll().find { it.packageName == packageName }
    }
}
