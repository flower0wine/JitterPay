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

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "UpdateManager"
    }

    /**
     * Check if a new version is available
     */
    suspend fun checkForUpdates(): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${BuildConfig.CDN_BASE_URL}/version.json")
                .get()
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
                Log.i(TAG, "New version available: $latestVersion")
                Result.success(info)
            } else {
                Log.d(TAG, "Current version is up to date: $currentVersion")
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
            Log.i(TAG, "Starting download: $downloadUrl")

            val request = Request.Builder()
                .url(downloadUrl)
                .get()
                .build()

            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "Download failed: HTTP ${response.code}")
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
            val contentLength = body.contentLength()

            // Clean old APK files in cache
            cleanupCacheFiles()

            val apkFile = File(context.cacheDir, "jitterpay-$version.apk")

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

            Log.i(TAG, "Download complete: ${apkFile.absolutePath}")
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
            context.cacheDir.listFiles()?.filter { file ->
                file.name.startsWith("jitterpay-") && file.name.endsWith(".apk")
            }?.forEach { oldFile ->
                Log.d(TAG, "Cleaning up old cache file: ${oldFile.name}")
                oldFile.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning cache files", e)
        }
    }

    /**
     * Install APK
     */
    fun installApk(apkFile: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK", e)
            throw e
        }
    }

    /**
     * Version comparison
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
