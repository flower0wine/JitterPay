package com.example.jitterpay.ui.update

import android.util.Log
import com.example.jitterpay.BuildConfig
import com.example.jitterpay.data.local.PendingUpdate
import com.example.jitterpay.data.local.UpdatePreferences
import com.example.jitterpay.util.UpdateInfo
import com.example.jitterpay.util.UpdateManager
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 更新检查核心逻辑
 *
 * 职责：
 * - 决定是否需要检查更新
 * - 决定是否显示安装对话框
 * - 统一处理跳过版本逻辑
 *
 * 设计原则：
 * - 单一职责：只负责更新决策，不涉及 UI
 * - 可测试：纯逻辑可通过单元测试验证
 * - 可复用：核心逻辑可被其他模块调用
 */
@Singleton
class UpdateChecker @Inject constructor(
    private val updateManager: UpdateManager,
    private val updatePreferences: UpdatePreferences
) {
    companion object {
        private const val TAG = "UpdateChecker"
    }

    /**
     * 检查是否应该显示安装对话框
     *
     * 统一的对话框显示判断逻辑：
     * 1. 检查是否有待安装更新且已下载
     * 2. 检查是否跳过此版本
     *
     * @param pendingUpdate 待安装的更新
     * @return 是否显示对话框
     */
    suspend fun shouldShowInstallDialog(pendingUpdate: PendingUpdate?): Boolean {
        // 1. 检查是否有待安装更新且已下载
        if (pendingUpdate == null || !pendingUpdate.isDownloaded) {
            return false
        }

        // 2. 检查是否跳过此版本
        if (BuildConfig.REMEMBER_SKIPPED_VERSION) {
            val isSkipped = updatePreferences.isVersionSkipped(pendingUpdate.version)
            if (isSkipped) {
                return false
            }
        }

        return true
    }

    /**
     * 处理更新检查结果
     *
     * 核心流程：
     * 1. 检查是否有新版本
     * 2. 立即检查是否跳过此版本（不依赖缓存）
     * 3. 如果有缓存且 APK 存在，显示安装对话框
     * 4. 否则准备下载
     *
     * @param updateInfo 最新版本信息
     * @return 更新检查结果
     */
    suspend fun handleUpdateCheck(updateInfo: UpdateInfo?): UpdateCheckResult {
        updatePreferences.updateLastCheckTime()

        // 无新版本
        if (updateInfo == null) {
            return UpdateCheckResult.AlreadyUpToDate
        }

        val latestVersion = updateInfo.latestVersion.removePrefix("v")

        // 【关键修改】先检查是否跳过此版本（不依赖缓存）
        if (BuildConfig.REMEMBER_SKIPPED_VERSION) {
            val isSkipped = updatePreferences.isVersionSkipped(latestVersion)
            if (isSkipped) {
                // 清理可能存在的旧缓存
                cleanupStaleCache(latestVersion)
                return UpdateCheckResult.SkippedVersion(latestVersion)
            }
        }

        // 获取缓存信息
        val cached = updatePreferences.pendingUpdate.first()
        val cachedApkExists = cached?.apkPath?.isNotEmpty() == true && File(cached.apkPath).exists()

        // 有缓存且 APK 存在，显示安装对话框
        if (cached != null && cached.version == latestVersion && cachedApkExists) {
            return UpdateCheckResult.ShowInstallDialog(cached)
        }

        // 清理无效缓存并准备下载
        if (!cachedApkExists && cached != null) {
            updatePreferences.clearPendingUpdate()
        }

        return UpdateCheckResult.StartDownload(updateInfo)
    }

    /**
     * 清理指定版本之外的旧缓存
     *
     * @param currentVersion 当前版本号（不应被清理）
     */
    private suspend fun cleanupStaleCache(currentVersion: String) {
        try {
            val pending = updatePreferences.pendingUpdate.first()
            if (pending != null && pending.version != currentVersion) {
                updatePreferences.cleanupCache()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning stale cache", e)
        }
    }

    /**
     * 获取待安装的更新信息
     *
     * @return PendingUpdate 或 null
     */
    suspend fun getPendingUpdate(): PendingUpdate? {
        return updatePreferences.pendingUpdate.first()
    }
}

/**
 * 更新检查结果
 */
sealed class UpdateCheckResult {
    /**
     * 已是最新版本
     */
    data object AlreadyUpToDate : UpdateCheckResult()

    /**
     * 显示安装对话框
     */
    data class ShowInstallDialog(val pendingUpdate: PendingUpdate) : UpdateCheckResult()

    /**
     * 开始下载
     */
    data class StartDownload(val updateInfo: UpdateInfo) : UpdateCheckResult()

    /**
     * 跳过此版本
     */
    data class SkippedVersion(val version: String) : UpdateCheckResult()
}
