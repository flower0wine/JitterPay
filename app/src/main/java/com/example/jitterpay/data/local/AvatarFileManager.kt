package com.example.jitterpay.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 头像文件管理器
 *
 * 负责将用户选择的图片复制到应用内部存储，
 * 以符合 Android scoped storage 规范
 */
@Singleton
class AvatarFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val avatarDir: File by lazy {
        File(context.filesDir, AVATAR_DIRECTORY).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * 将用户选择的图片复制到内部存储
     *
     * @param sourceUri 用户选择的图片 URI
     * @return 复制后的文件路径
     */
    fun copyAvatarToInternalStorage(sourceUri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(Exception("无法打开图片文件"))

            val fileName = generateAvatarFileName()
            val destFile = File(avatarDir, fileName)

            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除内部存储中的头像文件
     *
     * @param filePath 头像文件路径
     * @return 是否删除成功
     */
    fun deleteAvatarFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 清理过期的头像文件（保留当前正在使用的文件）
     *
     * @param currentAvatarPath 当前使用的头像路径
     */
    fun cleanupOldAvatars(currentAvatarPath: String?) {
        avatarDir.listFiles()?.forEach { file ->
            if (currentAvatarPath != file.absolutePath) {
                file.delete()
            }
        }
    }

    /**
     * 清理所有头像文件
     */
    fun clearAllAvatars() {
        avatarDir.listFiles()?.forEach { it.delete() }
    }

    private fun generateAvatarFileName(): String {
        return "avatar_${UUID.randomUUID()}.jpg"
    }

    companion object {
        private const val AVATAR_DIRECTORY = "avatars"
    }
}
