package com.example.jitterpay.ui.update

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.BuildConfig
import com.example.jitterpay.data.local.PendingUpdate
import com.example.jitterpay.data.local.UpdatePreferences
import com.example.jitterpay.scheduler.UpdateScheduler
import com.example.jitterpay.util.UpdateInfo
import com.example.jitterpay.util.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 更新功能 ViewModel - 管理静默更新状态
 *
 * 核心流程:
 * 1. 打开应用时自动检查更新（静默进行，不展示 UI）
 * 2. 有新版本时自动后台下载
 * 3. 下载完成后显示安装对话框（考虑跳过版本）
 * 4. 安装后清理缓存
 *
 * 设计说明:
 * - 使用 UpdateChecker 统一处理更新决策逻辑
 * - 跳过版本检查与对话框显示逻辑在同一处判断，避免竞态条件
 * - 更新检查和下载过程完全在后台进行，不展示任何 UI
 * - 只有下载完成且用户未跳过时才显示安装对话框
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateManager: UpdateManager,
    private val updatePreferences: UpdatePreferences,
    private val updateScheduler: UpdateScheduler,
    private val updateChecker: UpdateChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "UpdateViewModel"
    }

    init {
        // 监听状态变化 - 使用 combine 确保跳过版本检查与对话框显示同步判断
        viewModelScope.launch {
            combine(
                updatePreferences.pendingUpdate,
                updatePreferences.skippedVersion
            ) { pendingUpdate, _ ->
                // 统一的对话框显示判断逻辑
                val shouldShow = updateChecker.shouldShowInstallDialog(pendingUpdate)
                shouldShow to pendingUpdate
            }.collect { (shouldShow, pendingUpdate) ->
                _uiState.value = _uiState.value.copy(
                    pendingUpdate = pendingUpdate,
                    showInstallDialog = shouldShow
                )
                Log.d(TAG, "State updated: showDialog=$shouldShow, pendingUpdate=${pendingUpdate?.version}")
            }
        }

        // 启动时自动检查更新（静默进行）
        viewModelScope.launch {
            performUpdateCheck()
        }
    }

    /**
     * 执行更新检查（静默进行，不展示 UI）
     */
    private suspend fun performUpdateCheck() {
        Log.d(TAG, "=== Starting update check ===")

        try {
            val result = updateManager.checkForUpdates()
            result.fold(
                onSuccess = { updateInfo ->
                    when (val checkResult = updateChecker.handleUpdateCheck(updateInfo)) {
                        is UpdateCheckResult.ShowInstallDialog -> {
                            // 缓存存在，显示安装对话框
                            // 注意：showInstallDialog 会由上面的 collect 自动更新
                            Log.d(TAG, "Cached APK exists, pending update available")
                        }
                        is UpdateCheckResult.StartDownload -> {
                            // 开始后台下载
                            startSilentDownload(checkResult.updateInfo)
                        }
                        is UpdateCheckResult.SkippedVersion -> {
                            // 用户跳过了此版本，清理缓存
                            Log.d(TAG, "User skipped version ${checkResult.version}")
                        }
                        UpdateCheckResult.AlreadyUpToDate -> {
                            Log.d(TAG, "Already up to date")
                        }
                    }
                },
                onFailure = { e ->
                    // 错误只记录日志，不展示给用户
                    Log.e(TAG, "Update check failed: ${e.message}")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during update check: ${e.message}")
        }
    }

    /**
     * 开始静默下载（后台进行，不展示 UI）
     */
    private fun startSilentDownload(updateInfo: UpdateInfo) {
        viewModelScope.launch {
            updateScheduler.scheduleDownload(
                version = updateInfo.latestVersion.replace("v", ""),
                versionName = updateInfo.latestVersion,
                downloadUrl = updateInfo.downloadUrl,
                apkSize = updateInfo.apkSize,
                releaseDate = updateInfo.releaseDate
            )
        }
    }

    /**
     * 安装更新
     */
    fun installUpdate() {
        viewModelScope.launch {
            val pendingUpdate = _uiState.value.pendingUpdate
            if (pendingUpdate != null && pendingUpdate.isDownloaded) {
                Log.d(TAG, "Starting installation: ${pendingUpdate.versionName}")
                updateManager.installApk(pendingUpdate.apkFile)

                // 安装启动后清理状态
                cleanupAfterInstall()
            }
        }
    }

    /**
     * 关闭对话框（不删除缓存）
     */
    fun dismissInstallDialog() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showInstallDialog = false)
        }
    }

    /**
     * 删除缓存并清除状态
     * - dev 环境：删除缓存，下次启动仍提示
     * - prod 环境：记住跳过的版本，下次启动跳过
     */
    fun deleteCachedUpdate() {
        viewModelScope.launch {
            val currentVersion = _uiState.value.pendingUpdate?.version

            // 生产环境记住跳过的版本
            if (currentVersion != null && BuildConfig.REMEMBER_SKIPPED_VERSION) {
                Log.d(TAG, "Saving skipped version: $currentVersion")
                updatePreferences.skipCurrentVersion(currentVersion)
            }

            // 清理缓存
            Log.d(TAG, "Cleaning up cache")
            updatePreferences.cleanupCache()

            _uiState.value = _uiState.value.copy(
                showInstallDialog = false,
                pendingUpdate = null
            )
        }
    }

    /**
     * 安装完成后清理
     * 延迟删除确保系统安装器已读取 APK
     */
    private fun cleanupAfterInstall() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)

            Log.d(TAG, "Cleaning up after installation")
            updatePreferences.clearPendingUpdate()
            updatePreferences.cleanupCache()

            _uiState.value = _uiState.value.copy(
                showInstallDialog = false,
                pendingUpdate = null
            )
        }
    }

    /**
     * 关闭所有对话框
     */
    fun closeAllDialogs() {
        _uiState.value = _uiState.value.copy(showInstallDialog = false)
    }
}

/**
 * 更新 UI 状态
 */
data class UpdateUiState(
    val pendingUpdate: PendingUpdate? = null,
    val showInstallDialog: Boolean = false
) {
    val hasPendingUpdate: Boolean
        get() = pendingUpdate != null && pendingUpdate.isDownloaded
}
