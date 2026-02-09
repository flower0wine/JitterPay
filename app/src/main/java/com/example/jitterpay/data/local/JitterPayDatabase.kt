package com.example.jitterpay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.example.jitterpay.data.local.dao.BudgetDao
import com.example.jitterpay.data.local.dao.GoalDao
import com.example.jitterpay.data.local.dao.GoalTransactionDao
import com.example.jitterpay.data.local.dao.RecurringDao
import com.example.jitterpay.data.local.dao.TransactionDao
import com.example.jitterpay.data.local.entity.BudgetEntity
import com.example.jitterpay.data.local.entity.GoalEntity
import com.example.jitterpay.data.local.entity.GoalTransactionEntity
import com.example.jitterpay.data.local.entity.RecurringEntity
import com.example.jitterpay.data.local.entity.TransactionEntity

/**
 * JitterPay 记账应用数据库
 */
@Database(
    entities = [
        TransactionEntity::class,
        GoalEntity::class,
        GoalTransactionEntity::class,
        RecurringEntity::class,
        BudgetEntity::class
    ],
    version = 2,
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

    /**
     * 获取定时记账数据访问对象
     */
    abstract fun recurringDao(): RecurringDao

    /**
     * 获取预算数据访问对象
     */
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "jitterpay_database"

        /**
         * 数据库迁移：版本1到版本2
         * 添加 budgetId 字段到 transactions 表
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN budgetId INTEGER DEFAULT NULL"
                )
            }
        }
    }
}
