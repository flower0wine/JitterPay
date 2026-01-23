package com.example.jitterpay.autotracking.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils

/**
 * Utility class for checking and requesting permissions for auto-tracking feature
 */
object AutoTrackingPermissions {

    /**
     * Check if accessibility service is enabled
     *
     * @param context Application context
     * @param serviceClass The accessibility service class
     * @return True if service is enabled
     */
    fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<*>
    ): Boolean {
        val expectedComponentName = "${context.packageName}/${serviceClass.name}"

        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        if (enabledServicesSetting.isEmpty()) {
            return false
        }

        val enabledServices = enabledServicesSetting.split(":".toRegex()).toTypedArray()

        for (enabledService in enabledServices) {
            if (enabledService.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }

        return false
    }

    /**
     * Check if overlay permission is granted
     *
     * @param context Application context
     * @return True if overlay permission is granted
     */
    fun canDrawOverlays(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        return Settings.canDrawOverlays(context)
    }

    /**
     * Check if all required permissions are granted for auto-tracking
     *
     * @param context Application context
     * @param serviceClass The accessibility service class
     * @return True if all permissions are granted
     */
    fun areAllPermissionsGranted(
        context: Context,
        serviceClass: Class<*>
    ): Boolean {
        return isAccessibilityServiceEnabled(context, serviceClass) &&
                canDrawOverlays(context)
    }

    /**
     * Create intent to open accessibility service settings
     *
     * @param context Application context
     * @param serviceClass The accessibility service class
     * @return Intent to open settings
     */
    fun openAccessibilitySettings(
        context: Context,
        serviceClass: Class<*>
    ): Intent {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }

    /**
     * Create intent to open overlay permission settings
     *
     * @param context Application context
     * @return Intent to open settings
     */
    fun openOverlayPermissionSettings(context: Context): Intent {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }

    /**
     * Get user-friendly message for missing permissions
     *
     * @param context Application context
     * @param serviceClass The accessibility service class
     * @return Message indicating which permissions are missing
     */
    fun getMissingPermissionMessage(
        context: Context,
        serviceClass: Class<*>
    ): String {
        val missingPermissions = mutableListOf<String>()

        if (!isAccessibilityServiceEnabled(context, serviceClass)) {
            missingPermissions.add("Accessibility Service")
        }

        if (!canDrawOverlays(context)) {
            missingPermissions.add("Display Over Other Apps")
        }

        return when (missingPermissions.size) {
            0 -> "All permissions granted"
            1 -> "Enable ${missingPermissions[0]}"
            else -> missingPermissions.joinToString(" and ")
        }
    }
}
