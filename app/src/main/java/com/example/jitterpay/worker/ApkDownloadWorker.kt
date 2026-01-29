package com.example.jitterpay.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.jitterpay.data.local.UpdatePreferences
import com.example.jitterpay.util.UpdateManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

/**
 * 后台 APK 下载 Worker
 *
 * 使用 WorkManager 在后台静默下载更新 APK。
 * 支持网络连接时下载，下载失败时自动重试。
 */
@HiltWorker
class ApkDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateManager: UpdateManager,
    private val updatePreferences: UpdatePreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ApkDownloadWorker"
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

    override suspend fun doWork(): Result {
        val version = inputData.getString(KEY_VERSION) ?: return Result.failure()
        val versionName = inputData.getString(KEY_VERSION_NAME) ?: return Result.failure()
        val downloadUrl = inputData.getString(KEY_DOWNLOAD_URL) ?: return Result.failure()
        val apkSize = inputData.getLong(KEY_APK_SIZE, 0L)
        val releaseDate = inputData.getString(KEY_RELEASE_DATE) ?: ""

        Log.i(TAG, "Starting download for version: $version")

        return try {
            // 清理之前的缓存文件
            cleanupOldCache()

            // 下载 APK
            val result = updateManager.downloadApkToCache(
                version = version,
                downloadUrl = downloadUrl,
                onProgress = { progress ->
                    // 可选：更新通知进度（当前版本不显示通知）
                    Log.d(TAG, "Download progress: $progress%")
                }
            )

            result.fold(
                onSuccess = { apkFile ->
                    Log.i(TAG, "Download complete: ${apkFile.absolutePath}")

                    // 保存待安装状态
                    updatePreferences.setPendingUpdate(
                        version = version,
                        versionName = versionName,
                        downloadUrl = downloadUrl,
                        apkPath = apkFile.absolutePath,
                        apkSize = apkSize,
                        releaseDate = releaseDate
                    )

                    Result.success(
                        workDataOf(KEY_RESULT_PATH to apkFile.absolutePath)
                    )
                },
                onFailure = { e ->
                    Log.e(TAG, "Download failed: ${e.message}")
                    // 指数退避重试
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during download", e)
            Result.retry()
        }
    }

    /**
     * 清理旧的缓存文件
     */
    private suspend fun cleanupOldCache() {
        try {
            // 删除之前的 APK 文件（如果有）
            val currentPath = inputData.getString(KEY_RESULT_PATH)
            if (!currentPath.isNullOrEmpty()) {
                File(currentPath).delete()
            }

            // 清理 DataStore 中的旧状态（不同版本）
            val currentVersion = inputData.getString(KEY_VERSION)
            val pendingVersion = updatePreferences.pendingVersion
            pendingVersion.collect { pending ->
                if (pending != null && pending != currentVersion) {
                    updatePreferences.cleanupCache()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning old cache", e)
        }
    }
}
