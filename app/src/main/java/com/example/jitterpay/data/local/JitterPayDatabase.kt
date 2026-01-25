package com.example.jitterpay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.jitterpay.data.local.dao.GoalDao
import com.example.jitterpay.data.local.dao.GoalTransactionDao
import com.example.jitterpay.data.local.dao.TransactionDao
import com.example.jitterpay.data.local.entity.GoalEntity
import com.example.jitterpay.data.local.entity.GoalTransactionEntity
import com.example.jitterpay.data.local.entity.TransactionEntity

/**
 * JitterPay 记账应用数据库
 */
@Database(
    entities = [
        TransactionEntity::class,
        GoalEntity::class,
        GoalTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JitterPayDatabase : RoomDatabase() {

    /**
     * 获取交易数据访问对象
     */
    abstract fun transactionDao(): TransactionDao

    /**
     * 获取目标数据访问对象
     */
    abstract fun goalDao(): GoalDao

    /**
     * 获取目标交易数据访问对象
     */
    abstract fun goalTransactionDao(): GoalTransactionDao

    companion object {
        const val DATABASE_NAME = "jitterpay_database"
    }
}
