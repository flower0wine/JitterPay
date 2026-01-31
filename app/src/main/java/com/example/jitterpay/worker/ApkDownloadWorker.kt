package com.example.jitterpay.worker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.jitterpay.data.local.UpdatePreferences
import com.example.jitterpay.util.UpdateManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * 后台 APK 下载 Worker
 *
 * 使用 WorkManager 在后台静默下载更新 APK。
 * 支持网络连接时下载，下载失败时自动重试。
 *
 * 注意：使用 EntryPoint 进行手动依赖注入，绕过 Hilt WorkerFactory 问题
 */
class ApkDownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun updateManager(): UpdateManager
        fun updatePreferences(): UpdatePreferences
    }

    companion object {
        private const val TAG = "ApkDownloadWorker"
        private const val WORK_TIMEOUT_MINUTES = 15L
        const val KEY_VERSION = "version"
        const val KEY_VERSION_NAME = "version_name"
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_APK_SIZE = "apk_size"
        const val KEY_RELEASE_DATE = "release_date"
        const val KEY_RESULT_PATH = "result_path"
        const val WORK_NAME = "apk_download"

        /**
         * 创建输入数据
         */
        fun createInputData(
            version: String,
            versionName: String,
            downloadUrl: String,
            apkSize: Long,
            releaseDate: String
        ): Data {
            return workDataOf(
                KEY_VERSION to version,
                KEY_VERSION_NAME to versionName,
                KEY_DOWNLOAD_URL to downloadUrl,
                KEY_APK_SIZE to apkSize,
                KEY_RELEASE_DATE to releaseDate
            )
        }
    }

    private val updateManager: UpdateManager by lazy {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            WorkerEntryPoint::class.java
        )
        entryPoint.updateManager()
    }

    private val updatePreferences: UpdatePreferences by lazy {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            WorkerEntryPoint::class.java
        )
        entryPoint.updatePreferences()
    }

    override suspend fun doWork(): Result {
        val version = inputData.getString(KEY_VERSION) ?: run {
            Log.e(TAG, "Missing version in input data")
            return Result.failure()
        }
        val versionName = inputData.getString(KEY_VERSION_NAME) ?: run {
            Log.e(TAG, "Missing versionName in input data")
            return Result.failure()
        }
        val downloadUrl = inputData.getString(KEY_DOWNLOAD_URL) ?: run {
            Log.e(TAG, "Missing downloadUrl in input data")
            return Result.failure()
        }
        val apkSize = inputData.getLong(KEY_APK_SIZE, 0L)
        val releaseDate = inputData.getString(KEY_RELEASE_DATE) ?: ""

        Log.i(TAG, "========================================")
        Log.i(TAG, "Starting APK download")
        Log.i(TAG, "Version: $version ($versionName)")
        Log.i(TAG, "APK size: ${formatSize(apkSize)}")
        Log.i(TAG, "========================================")

        // 1. 在 Worker 内部检查网络状态（不依赖 WorkManager 约束）
        if (!isNetworkAvailable()) {
            Log.w(TAG, "No network available, retrying later...")
            return Result.retry()
        }

        // 2. 清理旧缓存
        cleanupOldCache(version)

        // 3. 执行下载（带超时保护）
        var downloadError: Throwable? = null
        var downloadedFile: File? = null

        val downloadJob = withTimeoutOrNull(WORK_TIMEOUT_MINUTES * 60 * 1000L) {
            try {
                val result = updateManager.downloadApkToCache(
                    version = version,
                    downloadUrl = downloadUrl,
                    onProgress = { progress ->
                        // 检查协程是否被取消
                        if (!isActive && progress < 100) {
                            throw CancellationException("Download cancelled")
                        }
                    }
                )

                if (result.isSuccess) {
                    downloadedFile = result.getOrNull()
                } else {
                    downloadError = result.exceptionOrNull()
                }
            } catch (e: CancellationException) {
                Log.w(TAG, "Download cancelled")
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Download error: ${e.message}")
                downloadError = e
            }
        }

        // 4. 处理结果
        return when {
            downloadJob == null -> {
                // 超时
                Log.e(TAG, "Download timeout after $WORK_TIMEOUT_MINUTES minutes")
                cleanupFailedDownload(version)
                Result.retry()
            }
            downloadError != null -> {
                // 下载失败
                Log.e(TAG, "Download failed: ${downloadError?.message}")

                // 根据错误类型决定是否重试
                val shouldRetry = isTemporaryError(downloadError)
                cleanupFailedDownload(version)
                if (shouldRetry) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
            downloadedFile != null -> {
                // 下载成功
                val apkFile = downloadedFile!!
                Log.i(TAG, "Download complete: ${apkFile.name} (${formatSize(apkFile.length())})")

                // 保存待安装状态
                updatePreferences.setPendingUpdate(
                    version = version,
                    versionName = versionName,
                    downloadUrl = downloadUrl,
                    apkPath = apkFile.absolutePath,
                    apkSize = apkSize,
                    releaseDate = releaseDate
                )

                Result.success(workDataOf(KEY_RESULT_PATH to apkFile.absolutePath))
            }
            else -> {
                cleanupFailedDownload(version)
                Result.failure()
            }
        }
    }

    /**
     * 检查网络是否可用（不依赖 WorkManager 约束）
     * 在测试环境或无法获取网络状态时默认返回 true
     */
    private fun isNetworkAvailable(): Boolean {
        try {
            val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm == null) {
                Log.w(TAG, "ConnectivityManager not available, assuming network is ok")
                return true
            }

            val network = cm.activeNetwork ?: run {
                Log.w(TAG, "No active network, assuming network is ok")
                return true
            }

            val capabilities = cm.getNetworkCapabilities(network) ?: run {
                Log.w(TAG, "No network capabilities, assuming network is ok")
                return true
            }

            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            return hasInternet
        } catch (e: Exception) {
            Log.w(TAG, "Error checking network availability: ${e.message}")
            return true // 出错时默认允许尝试
        }
    }

    /**
     * 判断是否为临时性错误（可重试）
     */
    private fun isTemporaryError(exception: Throwable?): Boolean {
        if (exception == null) return true

        return exception is SocketTimeoutException ||
                exception is UnknownHostException ||
                exception is java.net.ConnectException ||
                (exception.message?.contains("timeout", ignoreCase = true) == true) ||
                (exception.message?.contains("connection", ignoreCase = true) == true) ||
                (exception.message?.contains("network", ignoreCase = true) == true)
    }

    /**
     * 清理旧的缓存文件
     * 确保每次下载前都清理旧 APK，避免使用缓存的旧版本
     */
    private suspend fun cleanupOldCache(currentVersion: String) {
        try {
            // 清理所有旧的 jitterpay-*.apk 文件
            applicationContext.cacheDir.listFiles()?.filter { file ->
                file.name.startsWith("jitterpay-") && file.name.endsWith(".apk")
            }?.forEach { oldFile ->
                oldFile.delete()
            }

            // 同时清理 DataStore 中的旧状态
            val pending = updatePreferences.pendingVersion.firstOrNull()
            if (pending != null && pending != currentVersion) {
                updatePreferences.cleanupCache()
            }

            // 删除之前 Worker 保存的 APK 文件
            val currentPath = inputData.getString(KEY_RESULT_PATH)
            if (!currentPath.isNullOrEmpty()) {
                val oldFile = File(currentPath)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                Log.w(TAG, "Error cleaning cache: ${e.message}")
            }
        }
    }

    /**
     * 清理失败的下载文件
     */
    private fun cleanupFailedDownload(version: String) {
        try {
            val failedFile = File(applicationContext.cacheDir, "jitterpay-$version.apk")
            if (failedFile.exists()) {
                failedFile.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning failed download: ${e.message}")
        }
    }

    /**
     * 格式化文件大小
     */
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
