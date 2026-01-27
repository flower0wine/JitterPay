package com.example.jitterpay.di

import android.content.Context
import androidx.room.Room
import com.example.jitterpay.data.local.JitterPayDatabase
import com.example.jitterpay.data.local.UserPreferencesDataSource
import com.example.jitterpay.data.local.dao.GoalDao
import com.example.jitterpay.data.local.dao.GoalTransactionDao
import com.example.jitterpay.data.local.dao.RecurringDao
import com.example.jitterpay.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库模块 - 提供Room数据库和DAO的依赖注入
 *
 * 使用Hilt的@Module和@InstallIn注解来定义依赖提供方式。
 * 安装在SingletonComponent中，确保数据库实例在应用生命周期内保持单例。
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供JitterPayDatabase实例
     *
     * @param context 应用上下文
     * @return JitterPayDatabase实例
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): JitterPayDatabase {
        return Room.databaseBuilder(
            context,
            JitterPayDatabase::class.java,
            JitterPayDatabase.DATABASE_NAME
        )
            // 不允许删除数据库
            .fallbackToDestructiveMigration(false)
            .build()
    }

    /**
     * 提供TransactionDao实例
     *
     * 从数据库实例中获取TransactionDao，用于数据访问操作。
     *
     * @param database JitterPayDatabase实例
     * @return TransactionDao实例
     */
    @Provides
    @Singleton
    fun provideTransactionDao(
        database: JitterPayDatabase
    ): TransactionDao {
        return database.transactionDao()
    }

    /**
     * 提供GoalDao实例
     *
     * 从数据库实例中获取GoalDao，用于目标数据访问操作。
     *
     * @param database JitterPayDatabase实例
     * @return GoalDao实例
     */
    @Provides
    @Singleton
    fun provideGoalDao(
        database: JitterPayDatabase
    ): GoalDao {
        return database.goalDao()
    }

/**
 * 提供GoalTransactionDao实例
 *
 * 从数据库实例中获取GoalTransactionDao，用于目标交易数据访问操作。
 *
 * @param database JitterPayDatabase实例
 * @return GoalTransactionDao实例
 */
@Provides
@Singleton
fun provideGoalTransactionDao(
    database: JitterPayDatabase
): GoalTransactionDao {
    return database.goalTransactionDao()
}

/**
 * 提供RecurringDao实例
 *
 * 从数据库实例中获取RecurringDao，用于定时记账数据访问操作。
 *
 * @param database JitterPayDatabase实例
 * @return RecurringDao实例
 */
@Provides
@Singleton
fun provideRecurringDao(
    database: JitterPayDatabase
): RecurringDao {
    return database.recurringDao()
}

/**
 * 提供UserPreferencesDataSource实例
 *
 * @param context 应用上下文
 * @return UserPreferencesDataSource实例
 */
@Provides
@Singleton
fun provideUserPreferencesDataSource(
    @ApplicationContext context: Context
): UserPreferencesDataSource {
    return UserPreferencesDataSource(context)
}
}
