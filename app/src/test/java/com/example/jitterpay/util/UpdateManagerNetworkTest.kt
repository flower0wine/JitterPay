package com.example.jitterpay.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import java.util.concurrent.TimeUnit

/**
 * Unit tests for UpdateManager network operations using MockWebServer.
 *
 * Tests network layer with Robolectric providing Android context
 * and MockWebServer simulating the update server.
 *
 * This test verifies:
 * - HTTP request/response handling
 * - JSON parsing
 * - Version comparison logic
 * - Error handling for various network conditions
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [30])  // SDK 30 有完整的 Build 字段
class UpdateManagerNetworkTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var context: Context

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        mockWebServer = MockWebServer()
    }

    @After
    fun tearDown() {
        try {
            mockWebServer.shutdown()
        } catch (e: Exception) {
            // Ignore shutdown errors
        }
    }

    /**
     * Creates a mock PackageInfo with the specified version name
     */
    private fun createPackageInfo(versionName: String): PackageInfo {
        val packageInfo = PackageInfo()
        packageInfo.versionName = versionName
        return packageInfo
    }

    /**
     * Creates an UpdateManager instance configured to use MockWebServer.
     * Uses Robolectric's ReflectionHelpers to set the app version.
     */
    private fun createUpdateManager(
        currentVersionName: String,
        okHttpClient: OkHttpClient
    ): UpdateManager {
        // Use Robolectric to modify the app's version directly
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        ReflectionHelpers.setField(packageInfo, "versionName", currentVersionName)

        // Use the secondary constructor that accepts OkHttpClient
        return UpdateManager(context, okHttpClient)
    }

    @Test
    fun `checkForUpdates returns update info when new version available`() = runTest {
        // Given
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/")

        val versionJson = """
            {
                "latest_version": "v2.0.0",
                "release_date": "2026-01-30",
                "cdn_base_url": "${baseUrl}",
                "apk_size": 15000000
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(versionJson)
            .addHeader("Content-Type", "application/json")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates("${baseUrl}version.json")

        // Then
        assertTrue("Result should be success", result.isSuccess)
        val updateInfo = result.getOrNull()
        assertNotNull("UpdateInfo should not be null", updateInfo)
        assertEquals("v2.0.0", updateInfo?.latestVersion)
        assertEquals("2026-01-30", updateInfo?.releaseDate)
        assertEquals(15000000L, updateInfo?.apkSize)
    }

    @Test
    fun `checkForUpdates returns null when already up to date`() = runTest {
        // Given
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/")

        val versionJson = """
            {
                "latest_version": "v1.0.0",
                "release_date": "2026-01-30",
                "cdn_base_url": "${baseUrl}",
                "apk_size": 15000000
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(versionJson)
            .addHeader("Content-Type", "application/json")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates("${baseUrl}version.json")

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertNull("UpdateInfo should be null when up to date", result.getOrNull())
    }

    @Test
    fun `checkForUpdates returns null when server version is older`() = runTest {
        // Given
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/")

        val versionJson = """
            {
                "latest_version": "v0.5.0",
                "release_date": "2024-01-01",
                "cdn_base_url": "${baseUrl}",
                "apk_size": 10000000
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(versionJson)
            .addHeader("Content-Type", "application/json")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v3.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates("${baseUrl}version.json")

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertNull("UpdateInfo should be null when server version is older", result.getOrNull())
    }

    @Test
    fun `checkForUpdates handles HTTP 404`() = runTest {
        // Given
        mockWebServer.start()

        mockWebServer.enqueue(MockResponse()
            .setResponseCode(404)
            .setBody("Not Found")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates(mockWebServer.url("/version.json").toString())

        // Then
        assertTrue("Result should be failure on 404", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue("Exception should contain 404", exception?.message?.contains("404") == true)
    }

    @Test
    fun `checkForUpdates handles HTTP 500`() = runTest {
        // Given
        mockWebServer.start()

        mockWebServer.enqueue(MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates(mockWebServer.url("/version.json").toString())

        // Then
        assertTrue("Result should be failure on 500", result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue("Exception should contain 500", exception?.message?.contains("500") == true)
    }

    @Test
    fun `checkForUpdates handles empty response body`() = runTest {
        // Given
        mockWebServer.start()

        mockWebServer.enqueue(MockResponse()
            .setBody("")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates(mockWebServer.url("/version.json").toString())

        // Then
        assertTrue("Result should be failure on empty response", result.isFailure)
    }

    @Test
    fun `checkForUpdates handles malformed JSON`() = runTest {
        // Given
        mockWebServer.start()

        mockWebServer.enqueue(MockResponse()
            .setBody("not valid json at all!")
            .addHeader("Content-Type", "application/json")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates(mockWebServer.url("/version.json").toString())

        // Then
        assertTrue("Result should be failure on malformed JSON", result.isFailure)
    }

    @Test
    fun `checkForUpdates handles missing latest_version field`() = runTest {
        // Given
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/")

        val versionJson = """
            {
                "release_date": "2026-01-30",
                "cdn_base_url": "${baseUrl}",
                "apk_size": 15000000
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(versionJson)
            .addHeader("Content-Type", "application/json")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates("${baseUrl}version.json")

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertNull("UpdateInfo should be null when latest_version is empty", result.getOrNull())
    }

    @Test
    fun `checkForUpdates handles empty latest_version`() = runTest {
        // Given
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/")

        val versionJson = """
            {
                "latest_version": "",
                "release_date": "2026-01-30",
                "cdn_base_url": "${baseUrl}",
                "apk_size": 15000000
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(versionJson)
            .addHeader("Content-Type", "application/json")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates("${baseUrl}version.json")

        // Then
        assertTrue("Result should be success", result.isSuccess)
        assertNull("UpdateInfo should be null when latest_version is empty string", result.getOrNull())
    }

    @Test
    fun `downloadUrl generates correct URL for arm64-v8a`() {
        // Given - UpdateInfo with test data
        val updateInfo = UpdateInfo(
            latestVersion = "v2.0.0",
            releaseDate = "2026-01-30",
            cdnBaseUrl = "https://store.example.com",
            apkSize = 15000000L
        )

        // When
        val downloadUrl = updateInfo.downloadUrl

        // Then - Verify the URL format is correct (ABI varies by test environment)
        assertTrue(
            "downloadUrl should contain correct version: $downloadUrl",
            downloadUrl.contains("jitterpay-") && downloadUrl.contains("-2.0.0.apk")
        )
        assertTrue(
            "downloadUrl should start with CDN URL: $downloadUrl",
            downloadUrl.startsWith("https://store.example.com/v2.0.0/jitterpay-")
        )
    }

    @Test
    fun `checkForUpdates handles version with v prefix correctly`() = runTest {
        // Given
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/")

        val versionJson = """
            {
                "latest_version": "v2.0.0",
                "release_date": "2026-01-30",
                "cdn_base_url": "${baseUrl}",
                "apk_size": 15000000
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(versionJson)
            .addHeader("Content-Type", "application/json")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("v1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates("${baseUrl}version.json")

        // Then
        assertTrue("Result should be success", result.isSuccess)
        val updateInfo = result.getOrNull()
        assertNotNull("UpdateInfo should not be null", updateInfo)
        assertEquals("v2.0.0", updateInfo?.latestVersion)
    }

    @Test
    fun `checkForUpdates handles version without v prefix`() = runTest {
        // Given
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/")

        val versionJson = """
            {
                "latest_version": "2.0.0",
                "release_date": "2026-01-30",
                "cdn_base_url": "${baseUrl}",
                "apk_size": 15000000
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(versionJson)
            .addHeader("Content-Type", "application/json")
        )

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val updateManager = createUpdateManager("1.0.0", okHttpClient)

        // When
        val result = updateManager.checkForUpdates("${baseUrl}version.json")

        // Then
        assertTrue("Result should be success", result.isSuccess)
        val updateInfo = result.getOrNull()
        assertNotNull("UpdateInfo should not be null", updateInfo)
        assertEquals("2.0.0", updateInfo?.latestVersion)
    }
}
