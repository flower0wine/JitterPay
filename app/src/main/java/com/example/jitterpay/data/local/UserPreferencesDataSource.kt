package com.example.jitterpay.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
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
        const val DEFAULT_AVATAR_ID = -1
    }

    /**
     * 获取用户选择的头像ID
     */
    val avatarId: Flow<Int> = dataStore.data.map { preferences ->
        preferences[AVATAR_ID_KEY] ?: DEFAULT_AVATAR_ID
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
}
