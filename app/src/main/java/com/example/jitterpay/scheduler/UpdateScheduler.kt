package com.example.jitterpay.scheduler

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jitterpay.worker.ApkDownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 更新下载调度器
 *
 * 使用 WorkManager 调度后台 APK 下载任务
 */
@Singleton
class UpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "UpdateScheduler"
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * 调度后台下载更新
     *
     * @param version 版本号 (如 "1")
     * @param versionName 版本显示名称 (如 "v1.2.0")
     * @param downloadUrl APK 下载 URL
     * @param apkSize APK 大小
     * @param releaseDate 发布日期
     */
    fun scheduleDownload(
        version: String,
        versionName: String,
        downloadUrl: String,
        apkSize: Long,
        releaseDate: String
    ) {
        Log.i(TAG, "Scheduling download for version: $versionName")

        // 创建约束条件：需要网络连接
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)  // 低电量也下载
            .build()

        // 创建输入数据
        val inputData = ApkDownloadWorker.createInputData(
            version = version,
            versionName = versionName,
            downloadUrl = downloadUrl,
            apkSize = apkSize,
            releaseDate = releaseDate
        )

        // 创建一次性工作请求
        val workRequest = OneTimeWorkRequestBuilder<ApkDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(ApkDownloadWorker.WORK_NAME)
            .build()

        // 调度工作，替换已有的下载任务
        workManager.enqueueUniqueWork(
            ApkDownloadWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.i(TAG, "Download scheduled successfully")
    }

    /**
     * 取消下载任务
     */
    fun cancelDownload() {
        Log.i(TAG, "Cancelling download")
        workManager.cancelUniqueWork(ApkDownloadWorker.WORK_NAME)
    }

    /**
     * 检查是否正在下载
     */
    suspend fun isDownloading(): Boolean {
        return try {
            val workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME)
            workInfos.get().any { workInfo ->
                workInfo.state == androidx.work.WorkInfo.State.RUNNING ||
                workInfo.state == androidx.work.WorkInfo.State.ENQUEUED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking download status", e)
            false
        }
    }

    /**
     * 获取下载状态
     */
    suspend fun getDownloadState(): DownloadState {
        return try {
            val workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME)
            val workInfo = workInfos.get().firstOrNull()

            when (workInfo?.state) {
                androidx.work.WorkInfo.State.RUNNING -> DownloadState.DOWNLOADING
                androidx.work.WorkInfo.State.SUCCEEDED -> DownloadState.SUCCEEDED
                androidx.work.WorkInfo.State.FAILED -> DownloadState.FAILED
                androidx.work.WorkInfo.State.CANCELLED -> DownloadState.CANCELLED
                androidx.work.WorkInfo.State.ENQUEUED -> DownloadState.ENQUEUED
                androidx.work.WorkInfo.State.BLOCKED -> DownloadState.BLOCKED
                null -> DownloadState.IDLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting download state", e)
            DownloadState.IDLE
        }
    }
}

/**
 * 下载状态枚举
 */
enum class DownloadState {
    IDLE,
    ENQUEUED,
    DOWNLOADING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    BLOCKED
}
