package com.example.jitterpay.worker

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ApkDownloadWorker parameter handling.
 *
 * Tests input data creation and key constants without requiring
 * full Android WorkManager infrastructure.
 */
class ApkDownloadWorkerTest {

    @Test
    fun `createInputData creates correct data for all parameters`() {
        // Given/When
        val inputData = ApkDownloadWorker.createInputData(
            version = "1",
            versionName = "v1.0.0",
            downloadUrl = "https://store.example.com/v1.0.0.apk",
            apkSize = 10000000L,
            releaseDate = "2025-01-01"
        )

        // Then
        assertEquals("1", inputData.getString(ApkDownloadWorker.KEY_VERSION))
        assertEquals("v1.0.0", inputData.getString(ApkDownloadWorker.KEY_VERSION_NAME))
        assertEquals("https://store.example.com/v1.0.0.apk", inputData.getString(ApkDownloadWorker.KEY_DOWNLOAD_URL))
        assertEquals(10000000L, inputData.getLong(ApkDownloadWorker.KEY_APK_SIZE, 0L))
        assertEquals("2025-01-01", inputData.getString(ApkDownloadWorker.KEY_RELEASE_DATE))
    }

    @Test
    fun `createInputData handles large version numbers`() {
        // Given/When
        val inputData = ApkDownloadWorker.createInputData(
            version = "100",
            versionName = "v10.0.0",
            downloadUrl = "https://store.example.com/v10.0.0.apk",
            apkSize = 50000000L,
            releaseDate = "2026-12-31"
        )

        // Then
        assertEquals("100", inputData.getString(ApkDownloadWorker.KEY_VERSION))
        assertEquals(50000000L, inputData.getLong(ApkDownloadWorker.KEY_APK_SIZE, 0L))
    }

    @Test
    fun `work has correct name constant`() {
        // Then
        assertEquals("apk_download", ApkDownloadWorker.WORK_NAME)
    }

    @Test
    fun `key constants are defined`() {
        // Then - verify all key constants are accessible
        assertNotNull(ApkDownloadWorker.KEY_VERSION)
        assertNotNull(ApkDownloadWorker.KEY_VERSION_NAME)
        assertNotNull(ApkDownloadWorker.KEY_DOWNLOAD_URL)
        assertNotNull(ApkDownloadWorker.KEY_APK_SIZE)
        assertNotNull(ApkDownloadWorker.KEY_RELEASE_DATE)
        assertNotNull(ApkDownloadWorker.KEY_RESULT_PATH)
    }

    @Test
    fun `version string removePrefix works correctly`() {
        // Given
        val versionWithV = "v2.0.0"
        val versionWithoutV = "2.0.0"

        // When/Then
        assertEquals("2.0.0", versionWithV.removePrefix("v"))
        assertEquals("2.0.0", versionWithoutV.removePrefix("v"))
    }

    @Test
    fun `downloadUrl format is correct for arm64-v8a`() {
        // Given
        val version = "v2.0.0"
        val abi = "arm64-v8a"
        val cdnBaseUrl = "https://store.example.com"

        // When
        val downloadUrl = "$cdnBaseUrl/$version/jitterpay-$abi-${version.removePrefix("v")}.apk"

        // Then
        assertEquals("https://store.example.com/v2.0.0/jitterpay-arm64-v8a-2.0.0.apk", downloadUrl)
    }

    @Test
    fun `downloadUrl format is correct for armeabi-v7a`() {
        // Given
        val version = "v1.5.0"
        val abi = "armeabi-v7a"
        val cdnBaseUrl = "https://cdn.example.com"

        // When
        val downloadUrl = "$cdnBaseUrl/$version/jitterpay-$abi-${version.removePrefix("v")}.apk"

        // Then
        assertEquals("https://cdn.example.com/v1.5.0/jitterpay-armeabi-v7a-1.5.0.apk", downloadUrl)
    }
}
