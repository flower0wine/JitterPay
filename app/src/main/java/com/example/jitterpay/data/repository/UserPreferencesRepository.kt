package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.UserPreferencesDataSource
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
}
