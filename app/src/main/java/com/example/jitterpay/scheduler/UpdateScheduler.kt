package com.example.jitterpay.scheduler

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.jitterpay.worker.ApkDownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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
    private val executor = Executors.newSingleThreadExecutor()

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
        Log.i(TAG, "Scheduling download: $versionName ($version)")

        // 取消现有的下载任务（如果有新版本，需要重新下载）
        workManager.cancelUniqueWork(ApkDownloadWorker.WORK_NAME)

        // 创建输入数据
        val inputData = ApkDownloadWorker.createInputData(
            version = version,
            versionName = versionName,
            downloadUrl = downloadUrl,
            apkSize = apkSize,
            releaseDate = releaseDate
        )

        // 创建一次性工作请求（不设置网络约束，由 Worker 内部检查网络）
        val workRequest = OneTimeWorkRequestBuilder<ApkDownloadWorker>()
            .setInputData(inputData)
            .addTag(ApkDownloadWorker.WORK_NAME)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30_000,
                TimeUnit.MILLISECONDS
            )
            .build()

        // 调度工作，替换已有的下载任务
        workManager.enqueueUniqueWork(
            ApkDownloadWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.i(TAG, "Download scheduled: workId=${workRequest.id}")
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
            val workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()
            workInfos.any { workInfo ->
                workInfo.state == WorkInfo.State.RUNNING ||
                workInfo.state == WorkInfo.State.ENQUEUED
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
            val workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()
            val workInfo = workInfos.firstOrNull()

            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> DownloadState.DOWNLOADING
                WorkInfo.State.SUCCEEDED -> DownloadState.SUCCEEDED
                WorkInfo.State.FAILED -> DownloadState.FAILED
                WorkInfo.State.CANCELLED -> DownloadState.CANCELLED
                WorkInfo.State.ENQUEUED -> DownloadState.ENQUEUED
                WorkInfo.State.BLOCKED -> DownloadState.BLOCKED
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
