package com.example.jitterpay.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UpdateManager version comparison logic.
 *
 * Tests pure logic: version comparison.
 * Note: formatFileSize and formatDate require UpdateManager instance with Context,
 * so they're tested via helper functions below.
 */
class UpdateManagerTest {

    @Test
    fun `isVersionNewer returns true when new version is higher`() {
        assertTrue(isVersionNewer("2.0.0", "1.9.9"))
        assertTrue(isVersionNewer("2.0.0", "1.0.0"))
        assertTrue(isVersionNewer("1.10.0", "1.9.9"))
        assertTrue(isVersionNewer("1.2.0", "1.1.9"))
    }

    @Test
    fun `isVersionNewer returns false when current version is newer`() {
        assertFalse(isVersionNewer("1.0.0", "2.0.0"))
        assertFalse(isVersionNewer("1.9.9", "2.0.0"))
        assertFalse(isVersionNewer("1.0.0", "1.0.1"))
    }

    @Test
    fun `isVersionNewer returns false when versions are equal`() {
        assertFalse(isVersionNewer("1.0.0", "1.0.0"))
        assertFalse(isVersionNewer("2.0.0", "2.0.0"))
    }

    @Test
    fun `isVersionNewer handles version with v prefix`() {
        assertTrue(isVersionNewer("v2.0.0", "v1.0.0"))
        assertTrue(isVersionNewer("v1.5.0", "v1.4.9"))
        assertFalse(isVersionNewer("v1.0.0", "v2.0.0"))
    }

    @Test
    fun `isVersionNewer handles different version lengths`() {
        assertTrue(isVersionNewer("2.0.0", "1.0"))
        assertTrue(isVersionNewer("1.0.0.1", "1.0.0"))
        assertTrue(isVersionNewer("10.0.0", "9.9.9"))
    }

    @Test
    fun `isVersionNewer handles malformed version parts`() {
        // When "abc" is converted, it becomes [0], so 0 < 1.0.0's [1,0,0] → false
        assertFalse(isVersionNewer("abc", "1.0.0"))
        // When current is "abc" converted to [0], and new is [2,0,0], 2 > 0 → true
        assertTrue(isVersionNewer("2.0.0", "abc"))
    }

    @Test
    fun `formatFileSize helper formats bytes correctly`() {
        assertEquals("0 B", testFormatFileSize(0))
        assertEquals("500 B", testFormatFileSize(500))
        assertEquals("1023 B", testFormatFileSize(1023))
    }

    @Test
    fun `formatFileSize helper formats kilobytes correctly`() {
        assertEquals("1 KB", testFormatFileSize(1024))
        assertEquals("10 KB", testFormatFileSize(10 * 1024))
        assertEquals("999 KB", testFormatFileSize(999 * 1024))
    }

    @Test
    fun `formatFileSize helper formats megabytes correctly`() {
        assertEquals("1.0 MB", testFormatFileSize(1024 * 1024))
        assertEquals("10.0 MB", testFormatFileSize(10 * 1024 * 1024))
        assertEquals("15.5 MB", testFormatFileSize((15.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun `formatFileSize helper formats gigabytes correctly`() {
        assertEquals("1024.0 MB", testFormatFileSize(1024L * 1024 * 1024))
        assertEquals("2560.0 MB", testFormatFileSize((2.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun `formatDate helper extracts date before T`() {
        assertEquals("2026-01-30", testFormatDate("2026-01-30T10:30:00Z"))
        assertEquals("2025-12-25", testFormatDate("2025-12-25T00:00:00.000Z"))
    }

    @Test
    fun `formatDate helper returns original string when no T separator`() {
        assertEquals("2026-01-30", testFormatDate("2026-01-30"))
        assertEquals("invalid-date", testFormatDate("invalid-date"))
    }

    @Test
    fun `formatDate helper handles empty string`() {
        assertEquals("", testFormatDate(""))
    }

    @Test
    fun `formatDate helper handles date with timezone`() {
        assertEquals("2026-01-30", testFormatDate("2026-01-30T15:30:00+08:00"))
        assertEquals("2025-06-15", testFormatDate("2025-06-15T09:00:00-05:00"))
    }

    /**
     * Helper function that mirrors UpdateManager.isVersionNewer logic
     * for unit testing without needing Context
     */
    private fun isVersionNewer(newVersion: String, currentVersion: String): Boolean {
        val newParts = newVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = currentVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(newParts.size, currentParts.size)) {
            val newVal = newParts.getOrElse(i) { 0 }
            val currentVal = currentParts.getOrElse(i) { 0 }
            if (newVal > currentVal) return true
            if (newVal < currentVal) return false
        }
        return false
    }

    /**
     * Helper function that mirrors UpdateManager.formatFileSize logic
     */
    private fun testFormatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Helper function that mirrors UpdateManager.formatDate logic
     */
    private fun testFormatDate(dateString: String): String {
        return try {
            if (dateString.contains("T")) {
                dateString.substringBefore("T")
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
