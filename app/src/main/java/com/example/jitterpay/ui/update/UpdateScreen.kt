package com.example.jitterpay.ui.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
 * - 应用启动时自动检查更新
 * - 有新版本时静默下载
 * - 下载完成后显示安装对话框
 */
@Composable
fun UpdateScreen(
    viewModel: UpdateViewModel = hiltViewModel(),
    updateManager: UpdateManager,
    checkOnLaunch: Boolean = true,
    onCheckComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // 启动时检查更新
    LaunchedEffect(Unit) {
        if (checkOnLaunch) {
            delay(500)  // 稍微延迟，让 UI 先完成加载
            viewModel.checkForUpdates()
        }
        onCheckComplete()
    }

    // 安装对话框
    if (uiState.showInstallDialog && uiState.pendingUpdate != null) {
        InstallUpdateDialog(
            pendingUpdate = uiState.pendingUpdate!!,
            updateManager = updateManager,
            onInstall = { viewModel.installUpdate() },
            onDismiss = { viewModel.dismissInstallDialog() },
            onDelete = { viewModel.deleteCachedUpdate() }
        )
    }

    // 错误对话框
    uiState.error?.let { error ->
        ErrorDialog(
            message = error,
            onDismiss = { viewModel.dismissError() }
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

/**
 * 错误对话框
 */
@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("更新检查失败 😔") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    )
}
