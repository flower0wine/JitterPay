package com.example.jitterpay.ui.update

import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.PendingUpdate
import com.example.jitterpay.data.local.UpdatePreferences
import com.example.jitterpay.scheduler.DownloadState
import com.example.jitterpay.scheduler.UpdateScheduler
import com.example.jitterpay.util.UpdateInfo
import com.example.jitterpay.util.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * 更新功能 ViewModel - 管理静默更新状态
 *
 * 核心流程:
 * 1. 打开应用时检查更新
 * 2. 有新版本时自动后台下载
 * 3. 下载完成后提示安装
 * 4. 安装后清理缓存
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateManager: UpdateManager,
    private val updatePreferences: UpdatePreferences,
    private val updateScheduler: UpdateScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    init {
        // 监听状态变化
        viewModelScope.launch {
            // 监听待安装更新 - 下载完成后自动显示安装对话框
            updatePreferences.pendingUpdate.collect { pendingUpdate ->
                val isDownloaded = pendingUpdate?.isDownloaded == true
                _uiState.value = _uiState.value.copy(
                    pendingUpdate = pendingUpdate,
                    showInstallDialog = isDownloaded && !_uiState.value.showInstallDialog
                )
            }
        }

        viewModelScope.launch {
            // 监听下载状态
            val state = updateScheduler.getDownloadState()
            _uiState.value = _uiState.value.copy(
                downloadState = state
            )
        }
    }

    /**
     * 检查更新
     *
     * 流程:
     * 1. 获取远程 version.json
     * 2. 检查是否有已下载的更新
     * 3. 有新版本则静默下载
     */
    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 先检查是否有已下载的待安装更新（只检查一次，避免重复调用）
            val cached: PendingUpdate? = updatePreferences.pendingUpdate.first()
            if (cached != null) {
                // 有已下载的更新，显示安装对话框
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showInstallDialog = true,
                    pendingUpdate = cached
                )
            } else {
                // 没有缓存，检查远程更新
                updateManager.checkForUpdates().fold(
                    onSuccess = { updateInfo ->
                        updatePreferences.updateLastCheckTime()

                        if (updateInfo != null) {
                            // 有新版本，静默下载
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                updateInfo = updateInfo,
                                downloadState = DownloadState.ENQUEUED
                            )
                            startSilentDownload(updateInfo)
                        } else {
                            // 已是最新版本
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isUpToDate = true
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                )
            }
        }
    }

    /**
     * 开始静默下载
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
                updateManager.installApk(pendingUpdate.apkFile)

                // 安装开始后清理状态（安装流程已启动，APK 可被系统处理）
                cleanupAfterInstall()
            }
        }
    }

    /**
     * 稍后安装 - 删除缓存，等待下次启动
     */
    fun dismissInstallDialog() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showInstallDialog = false
            )
            // 不删除缓存，只关闭对话框
            // 用户下次打开时会再次显示安装对话框
        }
    }

    /**
     * 稍后 - 删除缓存并清除状态
     */
    fun deleteCachedUpdate() {
        viewModelScope.launch {
            updatePreferences.cleanupCache()
            _uiState.value = _uiState.value.copy(
                showInstallDialog = false,
                pendingUpdate = null
            )
        }
    }

    /**
     * 安装完成后清理
     * 注意：安装是异步的，需要延迟删除以确保系统安装器已读取 APK
     */
    private fun cleanupAfterInstall() {
        viewModelScope.launch {
            // 延迟 5 秒删除缓存（给系统安装器足够时间读取 APK）
            kotlinx.coroutines.delay(5000)

            // 清理 DataStore 中的待安装状态
            updatePreferences.clearPendingUpdate()
            // 清理缓存的 APK 文件
            updatePreferences.cleanupCache()

            _uiState.value = _uiState.value.copy(
                showInstallDialog = false,
                pendingUpdate = null,
                isUpToDate = true
            )
        }
    }

    /**
     * 清除错误状态
     */
    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 关闭对话框
     */
    fun closeAllDialogs() {
        _uiState.value = _uiState.value.copy(
            showInstallDialog = false
        )
    }
}

/**
 * 更新 UI 状态
 */
data class UpdateUiState(
    val isLoading: Boolean = false,
    val isUpToDate: Boolean = false,
    val updateInfo: UpdateInfo? = null,
    val pendingUpdate: PendingUpdate? = null,
    val showInstallDialog: Boolean = false,
    val downloadState: DownloadState = DownloadState.IDLE,
    val error: String? = null
) {
    val hasPendingUpdate: Boolean
        get() = pendingUpdate != null && pendingUpdate.isDownloaded

    val isDownloading: Boolean
        get() = downloadState == DownloadState.DOWNLOADING ||
                downloadState == DownloadState.ENQUEUED
}
