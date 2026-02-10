package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.UserPreferencesDataSource
import com.example.jitterpay.data.model.UserAvatar
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户偏好设置仓库
 *
 * 封装用户偏好设置的数据访问逻辑
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) {
    /**
     * 获取用户选择的头像
     *
     * 统一返回 UserAvatar，隐藏内部实现细节
     */
    fun getUserAvatar(): Flow<UserAvatar> = userPreferencesDataSource.userAvatar

    /**
     * 获取用户选择的头像ID
     */
    fun getAvatarId(): Flow<Int> = userPreferencesDataSource.avatarId

    /**
     * 保存用户选择的头像ID
     *
     * @param avatarId 头像资源ID
     */
    suspend fun saveAvatarId(avatarId: Int): Unit {
        userPreferencesDataSource.saveAvatarId(avatarId)
    }

    /**
     * 获取用户选择的自定义头像路径
     */
    fun getCustomAvatarPath(): Flow<String> = userPreferencesDataSource.customAvatarPath

    /**
     * 保存用户选择的自定义头像路径
     *
     * @param path 自定义头像的文件路径，传入空字符串表示清除自定义头像
     */
    suspend fun saveCustomAvatarPath(path: String): Unit {
        userPreferencesDataSource.saveCustomAvatarPath(path)
    }

    /**
     * 获取快速添加金额
     */
    fun getQuickAddAmount(): Flow<Int> = userPreferencesDataSource.quickAddAmount

    /**
     * 保存快速添加金额
     *
     * @param amount 快速添加金额
     */
    suspend fun saveQuickAddAmount(amount: Int): Unit {
        userPreferencesDataSource.saveQuickAddAmount(amount)
    }
}
