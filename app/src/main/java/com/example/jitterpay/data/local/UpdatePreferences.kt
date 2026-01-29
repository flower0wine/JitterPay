package com.example.jitterpay.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.updateDataStore: DataStore<Preferences> by preferencesDataStore(name = "update_preferences")

/**
 * 更新相关的用户偏好设置存储
 *
 * 使用 DataStore 存储更新检查和下载状态
 */
@Singleton
class UpdatePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.updateDataStore

    companion object {
        private const val TAG = "UpdatePreferences"
        private val PENDING_VERSION = stringPreferencesKey("pending_version")
        private val PENDING_VERSION_NAME = stringPreferencesKey("pending_version_name")
        private val PENDING_DOWNLOAD_URL = stringPreferencesKey("pending_download_url")
        private val PENDING_APK_PATH = stringPreferencesKey("pending_apk_path")
        private val PENDING_APK_SIZE = longPreferencesKey("pending_apk_size")
        private val PENDING_RELEASE_DATE = stringPreferencesKey("pending_release_date")
        private val LAST_CHECK_TIME = longPreferencesKey("last_check_time")
    }

    /**
     * 获取待安装版本号
     */
    val pendingVersion: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PENDING_VERSION]
    }

    /**
     * 获取待安装版本信息
     */
    val pendingUpdate: Flow<PendingUpdate?> = dataStore.data.map { preferences ->
        val version = preferences[PENDING_VERSION]
        if (version != null) {
            PendingUpdate(
                version = version,
                versionName = preferences[PENDING_VERSION_NAME] ?: version,
                downloadUrl = preferences[PENDING_DOWNLOAD_URL] ?: "",
                apkPath = preferences[PENDING_APK_PATH] ?: "",
                apkSize = preferences[PENDING_APK_SIZE] ?: 0L,
                releaseDate = preferences[PENDING_RELEASE_DATE] ?: ""
            )
        } else {
            null
        }
    }

    /**
     * 获取最后检查时间
     */
    val lastCheckTime: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_CHECK_TIME] ?: 0L
    }

    /**
     * 设置待安装的更新信息（下载完成后调用）
     */
    suspend fun setPendingUpdate(
        version: String,
        versionName: String,
        downloadUrl: String,
        apkPath: String,
        apkSize: Long,
        releaseDate: String
    ) {
        dataStore.edit { preferences ->
            preferences[PENDING_VERSION] = version
            preferences[PENDING_VERSION_NAME] = versionName
            preferences[PENDING_DOWNLOAD_URL] = downloadUrl
            preferences[PENDING_APK_PATH] = apkPath
            preferences[PENDING_APK_SIZE] = apkSize
            preferences[PENDING_RELEASE_DATE] = releaseDate
        }
    }

    /**
     * 清除待安装状态（安装完成后或用户取消时调用）
     */
    suspend fun clearPendingUpdate() {
        dataStore.edit { preferences ->
            preferences.remove(PENDING_VERSION)
            preferences.remove(PENDING_VERSION_NAME)
            preferences.remove(PENDING_DOWNLOAD_URL)
            preferences.remove(PENDING_APK_PATH)
            preferences.remove(PENDING_APK_SIZE)
            preferences.remove(PENDING_RELEASE_DATE)
        }
    }

    /**
     * 更新最后检查时间
     */
    suspend fun updateLastCheckTime() {
        dataStore.edit { preferences ->
            preferences[LAST_CHECK_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * 检查是否有待安装的更新
     */
    suspend fun hasPendingUpdate(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[PENDING_VERSION] != null
    }

    /**
     * 获取待安装的 APK 文件
     */
    suspend fun getPendingApkFile(): File? {
        val path = getApkPathSync()
        return if (path.isNotEmpty()) File(path) else null
    }

    /**
     * 同步获取 APK 路径（用于 Worker）
     */
    suspend fun getApkPathSync(): String {
        return try {
            val preferences = dataStore.data.first()
            preferences[PENDING_APK_PATH] ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting APK path", e)
            ""
        }
    }

    /**
     * 清理缓存文件
     */
    suspend fun cleanupCache() {
        cleanupCacheFiles()
        clearPendingUpdate()
    }

    /**
     * 清理旧的 APK 缓存文件
     */
    private suspend fun cleanupCacheFiles() {
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
}

/**
 * 待安装更新信息数据类
 */
data class PendingUpdate(
    val version: String,
    val versionName: String,
    val downloadUrl: String,
    val apkPath: String,
    val apkSize: Long,
    val releaseDate: String
) {
    val apkFile: File
        get() = File(apkPath)

    val isDownloaded: Boolean
        get() = apkPath.isNotEmpty() && apkFile.exists()
}
