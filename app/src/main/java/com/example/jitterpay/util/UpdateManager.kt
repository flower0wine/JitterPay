package com.example.jitterpay.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.jitterpay.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class UpdateInfo(
    val latestVersion: String,
    val releaseDate: String,
    val cdnBaseUrl: String,
    val apkSize: Long
) {
    val downloadUrl: String
        get() = "$cdnBaseUrl/$latestVersion/jitterpay-${getAbi()}-${latestVersion.removePrefix("v")}.apk"

    private fun getAbi(): String {
        return when {
            Build.SUPPORTED_ABIS.contains("arm64-v8a") -> "arm64-v8a"
            Build.SUPPORTED_ABIS.contains("armeabi-v7a") -> "armeabi-v7a"
            Build.SUPPORTED_ABIS.contains("x86_64") -> "x86_64"
            Build.SUPPORTED_ABIS.contains("x86") -> "x86"
            else -> "universal"
        }
    }
}

class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val currentVersion: String
        get() = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"

    private lateinit var httpClient: OkHttpClient

    constructor(
        context: Context,
        okHttpClient: OkHttpClient
    ) : this(context) {
        this.httpClient = okHttpClient
    }

    init {
        if (!::httpClient.isInitialized) {
            httpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)      // 连接超时 60秒
                .readTimeout(10 * 60, TimeUnit.SECONDS)    // 读取超时 10分钟（APK下载需要）
                .writeTimeout(10 * 60, TimeUnit.SECONDS)   // 写入超时 10分钟
                // 禁用缓存，确保每次都获取最新版本
                .cache(null)
                .build()
        }
    }

    companion object {
        private const val TAG = "UpdateManager"
    }

    /**
     * Check if a new version is available
     */
    suspend fun checkForUpdates(): Result<UpdateInfo?> =
        checkForUpdates(BuildConfig.CDN_BASE_URL)

    /**
     * Check if a new version is available against a custom server URL.
     * This method is primarily used for testing with MockWebServer.
     *
     * @param serverBaseUrl The base URL of the update server (without version.json)
     */
    suspend fun checkForUpdates(serverBaseUrl: String): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$serverBaseUrl/version.json")
                .get()
                // 添加缓存控制头，确保不缓存 version.json
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "Failed to check for updates: HTTP ${response.code}")
                return@withContext Result.failure(Exception("Network error: ${response.code}"))
            }

            val body = response.body?.string()
            if (body.isNullOrEmpty()) {
                Log.w(TAG, "Empty response from version check")
                return@withContext Result.failure(Exception("Empty response"))
            }

            val json = JSONObject(body)
            val latestVersion = json.optString("latest_version", "")

            if (latestVersion.isEmpty()) {
                return@withContext Result.success(null)
            }

            // Compare versions
            if (isVersionNewer(latestVersion, currentVersion)) {
                val info = UpdateInfo(
                    latestVersion = latestVersion,
                    releaseDate = json.optString("release_date", ""),
                    cdnBaseUrl = json.optString("cdn_base_url", BuildConfig.CDN_BASE_URL),
                    apkSize = json.optLong("apk_size", 0)
                )
                Result.success(info)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            Result.failure(e)
        }
    }

    /**
     * Download APK file with progress callback
     */
    suspend fun downloadApk(updateInfo: UpdateInfo, onProgress: (Int) -> Unit): Result<File> =
        downloadApkToCache(
            version = updateInfo.latestVersion,
            downloadUrl = updateInfo.downloadUrl,
            onProgress = onProgress
        )

    /**
     * Download APK to cache directory
     *
     * @param version Version code (e.g., "1")
     * @param downloadUrl Full download URL
     * @param onProgress Progress callback (0-100)
     * @return Result containing the downloaded File
     */
    suspend fun downloadApkToCache(
        version: String,
        downloadUrl: String,
        onProgress: (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // 清理旧 APK 文件（确保每次下载都是最新的）
            cleanupCacheFiles()

            val request = Request.Builder()
                .url(downloadUrl)
                .get()
                // 添加缓存控制头，确保下载最新 APK
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "Download failed: HTTP ${response.code}")
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
            val contentLength = body.contentLength()

            // 使用包含 ABI 的文件名，与服务器保持一致
            val abi = getAbi()
            val apkFile = File(context.cacheDir, "jitterpay-$abi-$version.apk")

            FileOutputStream(apkFile).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (contentLength > 0) {
                            val progress = ((totalBytesRead * 100) / contentLength).toInt()
                            onProgress(progress.coerceIn(0, 100))
                        }
                    }
                }
            }

            Result.success(apkFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading APK", e)
            Result.failure(e)
        }
    }

    /**
     * Clean up old APK cache files
     */
    private fun cleanupCacheFiles() {
        try {
            // 列出所有缓存的 APK 文件
            val apkFiles = context.cacheDir.listFiles()?.filter { file ->
                file.name.startsWith("jitterpay-") && file.name.endsWith(".apk")
            } ?: emptyList()

            Log.d(TAG, "=== Cache files before cleanup ===")
            if (apkFiles.isEmpty()) {
                Log.d(TAG, "No APK cache files found")
            } else {
                apkFiles.forEach { file ->
                    Log.d(TAG, "  ${file.name} (${file.length()} bytes, modified: ${file.lastModified()})")
                }
            }
            Log.d(TAG, "==================================")

            // 删除旧的缓存文件
            apkFiles.forEach { oldFile ->
                oldFile.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning cache files", e)
        }
    }

    /**
     * 获取当前设备的 ABI
     */
    private fun getAbi(): String {
        return when {
            Build.SUPPORTED_ABIS.contains("arm64-v8a") -> "arm64-v8a"
            Build.SUPPORTED_ABIS.contains("armeabi-v7a") -> "armeabi-v7a"
            Build.SUPPORTED_ABIS.contains("x86_64") -> "x86_64"
            Build.SUPPORTED_ABIS.contains("x86") -> "x86"
            else -> "universal"
        }
    }

    /**
     * Install APK using FileProvider (Android 7.0+ compatible)
     */
    fun installApk(apkFile: File) {
        try {
            // 确保文件存在
            if (!apkFile.exists()) {
                throw Exception("APK file does not exist: ${apkFile.absolutePath}")
            }

            // Get URI from FileProvider (for Android 7.0+)
            val apkUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            Log.i(TAG, "URI from FileProvider: $apkUri")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    apkUri,
                    "application/vnd.android.package-archive"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                addCategory(Intent.CATEGORY_DEFAULT)
            }

            // Check if there's an app to handle the intent
            val resolver = intent.resolveActivity(context.packageManager)
            Log.i(TAG, "resolveActivity result: $resolver")

            if (resolver != null) {
                // 使用 createChooser 确保有 UI 反馈
                val chooserIntent = Intent.createChooser(intent, "Install APK")
                chooserIntent.addCategory(Intent.CATEGORY_DEFAULT)
                chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

                Log.i(TAG, "Starting installation activity...")
                context.startActivity(chooserIntent)
                Log.i(TAG, "startActivity() called successfully")
            } else {
                Log.e(TAG, "No app found to install APK")
                throw Exception("No app found to install APK")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK", e)
            throw e
        }
    }

    /**
     * Version comparison
     */
    private fun isVersionNewer(newVersion: String, currentVersion: String): Boolean {
        // 移除 v 前缀和 -dev 后缀进行版本比较
        val cleanNewVersion = newVersion.removePrefix("v").substringBefore("-dev")
        val cleanCurrentVersion = currentVersion.removePrefix("v").substringBefore("-dev")

        val newParts = cleanNewVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = cleanCurrentVersion.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(newParts.size, currentParts.size)) {
            val newVal = newParts.getOrElse(i) { 0 }
            val currentVal = currentParts.getOrElse(i) { 0 }
            if (newVal > currentVal) return true
            if (newVal < currentVal) return false
        }
        return false
    }

    /**
     * Format file size for display
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Format date for display
     */
    fun formatDate(dateString: String): String {
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
