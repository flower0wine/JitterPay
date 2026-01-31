package com.example.jitterpay.ui.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.data.local.PendingUpdate
import com.example.jitterpay.util.UpdateManager
import kotlinx.coroutines.delay

/**
 * 更新检查屏幕
 *
 * 功能:
 * - 应用启动时自动检查更新（由 ViewModel init 触发）
 * - 有新版本时静默下载（后台进行，不展示 UI）
 * - 下载完成后显示安装对话框
 *
 * 注意：checkForUpdates() 由 ViewModel init 块自动调用，无需外部触发
 */
@Composable
fun UpdateScreen(
    viewModel: UpdateViewModel = hiltViewModel(),
    updateManager: UpdateManager,
    checkOnLaunch: Boolean = true,
    onCheckComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // 启动完成回调（仅在 checkOnLaunch 为 true 时调用）
    LaunchedEffect(Unit) {
        if (checkOnLaunch) {
            delay(500)
        }
        onCheckComplete()
    }

    // 安装对话框（只有安装时才显示）
    if (uiState.showInstallDialog && uiState.pendingUpdate != null) {
        InstallUpdateDialog(
            pendingUpdate = uiState.pendingUpdate!!,
            updateManager = updateManager,
            onInstall = { viewModel.installUpdate() },
            onDismiss = { viewModel.dismissInstallDialog() },
            onDelete = { viewModel.deleteCachedUpdate() }
        )
    }
}

/**
 * 安装更新对话框
 */
@Composable
fun InstallUpdateDialog(
    pendingUpdate: PendingUpdate,
    updateManager: UpdateManager,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("更新已就绪 🎉") },
        text = {
            Text(
                "JitterPay ${pendingUpdate.versionName} 已下载完成\n\n" +
                "• APK 大小: ${updateManager.formatFileSize(pendingUpdate.apkSize)}\n" +
                "• 发布日期: ${updateManager.formatDate(pendingUpdate.releaseDate)}\n\n" +
                "是否立即安装？"
            )
        },
        confirmButton = {
            Button(onClick = onInstall) {
                Text("立即安装")
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("稍后")
            }
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
}
