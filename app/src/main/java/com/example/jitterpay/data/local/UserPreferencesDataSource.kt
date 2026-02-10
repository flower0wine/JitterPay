package com.example.jitterpay.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.jitterpay.R
import com.example.jitterpay.data.model.UserAvatar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * 用户偏好设置数据源
 *
 * 使用 DataStore 存储用户的偏好设置，如头像选择等
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val AVATAR_ID_KEY = intPreferencesKey("avatar_id")
        private val CUSTOM_AVATAR_PATH_KEY = stringPreferencesKey("custom_avatar_path")
        private val QUICK_ADD_AMOUNT_KEY = intPreferencesKey("quick_add_amount")
        const val DEFAULT_AVATAR_ID = -1
        const val DEFAULT_QUICK_ADD_AMOUNT = 100
        const val NO_CUSTOM_AVATAR = ""
        /**
         * 用于标记自定义头像的特殊值
         */
        const val CUSTOM_AVATAR_MARKER = 0
    }

    /**
     * 获取用户选择的头像ID
     */
    val avatarId: Flow<Int> = dataStore.data.map { preferences ->
        preferences[AVATAR_ID_KEY] ?: DEFAULT_AVATAR_ID
    }

    /**
     * 获取用户选择的自定义头像路径
     */
    val customAvatarPath: Flow<String> = dataStore.data.map { preferences ->
        preferences[CUSTOM_AVATAR_PATH_KEY] ?: NO_CUSTOM_AVATAR
    }

    /**
     * 获取用户选择的头像
     *
     * 统一返回 UserAvatar，隐藏内部实现细节
     */
    val userAvatar: Flow<UserAvatar> = combine(
        avatarId,
        customAvatarPath
    ) { avatarId, customPath ->
        when {
            // 有自定义头像
            customPath.isNotEmpty() -> UserAvatar.Custom(customPath)
            // 有选中的默认头像
            avatarId != DEFAULT_AVATAR_ID && avatarId != CUSTOM_AVATAR_MARKER -> {
                UserAvatar.Default(avatarId)
            }
            // 默认使用第一个头像
            else -> UserAvatar.Default(R.drawable.avatar_1)
        }
    }

    /**
     * 保存用户选择的头像ID
     *
     * @param avatarId 头像资源ID
     */
    suspend fun saveAvatarId(avatarId: Int): Unit {
        dataStore.edit { preferences ->
            preferences[AVATAR_ID_KEY] = avatarId
        }
    }

    /**
     * 保存用户选择的自定义头像路径
     *
     * @param path 自定义头像的文件路径，传入空字符串表示清除自定义头像
     */
    suspend fun saveCustomAvatarPath(path: String): Unit {
        dataStore.edit { preferences ->
            if (path.isEmpty()) {
                preferences.remove(CUSTOM_AVATAR_PATH_KEY)
            } else {
                preferences[CUSTOM_AVATAR_PATH_KEY] = path
            }
        }
    }

    /**
     * 获取快速添加金额
     */
    val quickAddAmount: Flow<Int> = dataStore.data.map { preferences ->
        preferences[QUICK_ADD_AMOUNT_KEY] ?: DEFAULT_QUICK_ADD_AMOUNT
    }

    /**
     * 保存快速添加金额
     *
     * @param amount 快速添加金额
     */
    suspend fun saveQuickAddAmount(amount: Int): Unit {
        dataStore.edit { preferences ->
            preferences[QUICK_ADD_AMOUNT_KEY] = amount
        }
    }
}
