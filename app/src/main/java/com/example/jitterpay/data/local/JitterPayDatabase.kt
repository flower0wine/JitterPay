package com.example.jitterpay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.jitterpay.data.local.dao.TransactionDao
import com.example.jitterpay.data.local.entity.TransactionEntity

/**
 * JitterPay 记账应用数据库
 *
 * 数据库版本管理说明：
 * - version = 1: 初始版本，包含transactions表
 * - 未来版本升级时，使用Room的迁移策略
 *
 * 当前已考虑的扩展：
 * - categories表：用于存储用户自定义分类
 * - accounts表：用于多账户支持
 * - budgets表：用于预算管理
 */
@Database(
    entities = [
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JitterPayDatabase : RoomDatabase() {

    /**
     * 获取交易数据访问对象
     */
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "jitterpay_database"
    }
}
