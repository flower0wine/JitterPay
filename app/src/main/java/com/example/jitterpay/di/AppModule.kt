package com.example.jitterpay.di

import android.content.Context
import com.example.jitterpay.notification.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 应用模块 - 提供应用级别的依赖注入
 *
 * 提供 NotificationHelper 等需要在整个应用生命周期内保持单例的对象。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供 NotificationHelper 实例
     *
     * @param context 应用上下文
     * @return NotificationHelper 实例
     */
    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper {
        return NotificationHelper(context)
    }
}
