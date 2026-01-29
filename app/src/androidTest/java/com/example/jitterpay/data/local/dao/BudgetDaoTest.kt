package com.example.jitterpay.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.jitterpay.data.local.JitterPayDatabase
import com.example.jitterpay.data.local.entity.BudgetEntity
import com.example.jitterpay.data.local.entity.BudgetPeriodType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * BudgetDao 测试
 *
 * 测试数据访问对象的数据库操作，使用内存数据库进行测试：
 * - 插入操作
 * - 更新操作
 * - 删除操作
 * - 查询操作
 * - Flow 响应式更新
 */
@RunWith(AndroidJUnit4::class)
class BudgetDaoTest {

    private lateinit var database: JitterPayDatabase
    private lateinit var budgetDao: BudgetDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            JitterPayDatabase::class.java
        ).build()
        budgetDao = database.budgetDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    // ==================== 插入操作测试 ====================

    @Test
    fun `insertBudget returns valid id`() = runTest {
        // Given
        val budget = createTestBudget(
            title = "Food Budget",
            amountCents = 50000L
        )

        // When
        val budgetId = budgetDao.insertBudget(budget)

        // Then
        assertTrue(budgetId > 0)
    }

    @Test
    fun `insertBudget stores data correctly`() = runTest {
        // Given
        val original = createTestBudget(
            title = "Test Budget",
            amountCents = 100000L
        )

        // When
        val budgetId = budgetDao.insertBudget(original)
        val retrieved = budgetDao.getBudgetById(budgetId)

        // Then
        assertNotNull(retrieved)
        assertEquals(original.title, retrieved?.title)
        assertEquals(original.amountCents, retrieved?.amountCents)
        assertEquals(original.periodType, retrieved?.periodType)
    }

    @Test
    fun `insertMultipleBudgets stores all correctly`() = runTest {
        // Given
        val budget1 = createTestBudget(id = 1, title = "Food", amountCents = 50000L)
        val budget2 = createTestBudget(id = 2, title = "Transport", amountCents = 30000L)
        val budget3 = createTestBudget(id = 3, title = "Entertainment", amountCents = 20000L)

        // When
        budgetDao.insertBudget(budget1)
        budgetDao.insertBudget(budget2)
        budgetDao.insertBudget(budget3)

        val budgets = budgetDao.getAllBudgets().first()

        // Then
        assertEquals(3, budgets.size)
        assertTrue(budgets.any { it.title == "Food" })
        assertTrue(budgets.any { it.title == "Transport" })
        assertTrue(budgets.any { it.title == "Entertainment" })
    }

    // ==================== 更新操作测试 ====================

    @Test
    fun `updateBudget modifies existing record`() = runTest {
        // Given
        val original = createTestBudget(title = "Original", amountCents = 50000L)
        val budgetId = budgetDao.insertBudget(original)
        val updated = original.copy(
            id = budgetId,
            title = "Updated",
            amountCents = 100000L
        )

        // When
        budgetDao.updateBudget(updated)
        val retrieved = budgetDao.getBudgetById(budgetId)

        // Then
        assertEquals("Updated", retrieved?.title)
        assertEquals(100000L, retrieved?.amountCents)
    }

    @Test
    fun `updateBudget updates timestamp`() = runTest {
        // Given
        val original = createTestBudget(
            updatedAt = 1000L
        )
        val budgetId = budgetDao.insertBudget(original)
        Thread.sleep(100) // 确保时间戳不同
        val updated = original.copy(
            id = budgetId,
            updatedAt = System.currentTimeMillis()
        )

        // When
        budgetDao.updateBudget(updated)
        val retrieved = budgetDao.getBudgetById(budgetId)

        // Then
        assertTrue(retrieved?.updatedAt!! > original.updatedAt)
    }

    // ==================== 删除操作测试 ====================

    @Test
    fun `deleteBudget removes record`() = runTest {
        // Given
        val budget = createTestBudget(title = "To Delete")
        val budgetId = budgetDao.insertBudget(budget)
        var retrieved = budgetDao.getBudgetById(budgetId)
        assertNotNull(retrieved)

        // When
        budgetDao.deleteBudget(budget)
        retrieved = budgetDao.getBudgetById(budgetId)

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `deleteBudgetById removes record by id`() = runTest {
        // Given
        val budget = createTestBudget(title = "To Delete By ID")
        val budgetId = budgetDao.insertBudget(budget)
        var retrieved = budgetDao.getBudgetById(budgetId)
        assertNotNull(retrieved)

        // When
        budgetDao.deleteBudgetById(budgetId)
        retrieved = budgetDao.getBudgetById(budgetId)

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `deleteBudget does not affect other records`() = runTest {
        // Given
        val budget1 = createTestBudget(id = 1, title = "Keep", amountCents = 10000L)
        val budget2 = createTestBudget(id = 2, title = "Delete", amountCents = 20000L)
        val budgetId1 = budgetDao.insertBudget(budget1)
        val budgetId2 = budgetDao.insertBudget(budget2)

        // When
        budgetDao.deleteBudgetById(budgetId2)

        // Then
        val remaining = budgetDao.getAllBudgets().first()
        assertEquals(1, remaining.size)
        assertEquals("Keep", remaining[0].title)
    }

    // ==================== 查询操作测试 ====================

    @Test
    fun `getAllBudgets returns all records`() = runTest {
        // Given
        val budget1 = createTestBudget(id = 1, title = "Food", amountCents = 50000L)
        val budget2 = createTestBudget(id = 2, title = "Transport", amountCents = 30000L)
        budgetDao.insertBudget(budget1)
        budgetDao.insertBudget(budget2)

        // When
        val budgets = budgetDao.getAllBudgets().first()

        // Then
        assertEquals(2, budgets.size)
    }

    @Test
    fun `getAllBudgets orders by createdAt desc`() = runTest {
        // Given
        val budget1 = createTestBudget(id = 1, title = "First", createdAt = 1000L)
        val budget2 = createTestBudget(id = 2, title = "Second", createdAt = 2000L)
        val budget3 = createTestBudget(id = 3, title = "Third", createdAt = 3000L)
        budgetDao.insertBudget(budget1)
        budgetDao.insertBudget(budget2)
        budgetDao.insertBudget(budget3)

        // When
        val budgets = budgetDao.getAllBudgets().first()

        // Then
        assertEquals("Third", budgets[0].title)
        assertEquals("Second", budgets[1].title)
        assertEquals("First", budgets[2].title)
    }

    @Test
    fun `getActiveBudgets returns only active budgets`() = runTest {
        // Given
        val active1 = createTestBudget(id = 1, title = "Active 1", isActive = true)
        val active2 = createTestBudget(id = 2, title = "Active 2", isActive = true)
        val inactive = createTestBudget(id = 3, title = "Inactive", isActive = false)
        budgetDao.insertBudget(active1)
        budgetDao.insertBudget(active2)
        budgetDao.insertBudget(inactive)

        // When
        val activeBudgets = budgetDao.getActiveBudgets().first()

        // Then
        assertEquals(2, activeBudgets.size)
        assertTrue(activeBudgets.all { it.isActive })
    }

    @Test
    fun `getBudgetById returns correct budget`() = runTest {
        // Given
        val budget = createTestBudget(id = 1, title = "Specific Budget")
        val budgetId = budgetDao.insertBudget(budget)

        // When
        val retrieved = budgetDao.getBudgetById(budgetId)

        // Then
        assertNotNull(retrieved)
        assertEquals("Specific Budget", retrieved?.title)
    }

    @Test
    fun `getBudgetById returns null for non-existent id`() = runTest {
        // When
        val retrieved = budgetDao.getBudgetById(999L)

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `getBudgetByIdFlow emits updates`() = runTest {
        // Given
        val original = createTestBudget(title = "Original", amountCents = 50000L)
        val budgetId = budgetDao.insertBudget(original)

        // When
        val flow = budgetDao.getBudgetByIdFlow(budgetId)
        var firstEmission = flow.first()
        val updated = original.copy(id = budgetId, title = "Updated")
        budgetDao.updateBudget(updated)
        var secondEmission = flow.first()

        // Then
        assertEquals("Original", firstEmission?.title)
        assertEquals("Updated", secondEmission?.title)
    }

    // ==================== 统计操作测试 ====================

    @Test
    fun `getBudgetCount returns correct count`() = runTest {
        // Given
        budgetDao.insertBudget(createTestBudget(id = 1, title = "Budget 1"))
        budgetDao.insertBudget(createTestBudget(id = 2, title = "Budget 2"))
        budgetDao.insertBudget(createTestBudget(id = 3, title = "Budget 3"))

        // When
        val count = budgetDao.getBudgetCount()

        // Then
        assertEquals(3, count)
    }

    @Test
    fun `getBudgetCount returns zero for empty table`() = runTest {
        // When
        val count = budgetDao.getBudgetCount()

        // Then
        assertEquals(0, count)
    }

    @Test
    fun `getActiveBudgetCount returns only active budgets count`() = runTest {
        // Given
        budgetDao.insertBudget(createTestBudget(id = 1, title = "Active 1", isActive = true))
        budgetDao.insertBudget(createTestBudget(id = 2, title = "Active 2", isActive = true))
        budgetDao.insertBudget(createTestBudget(id = 3, title = "Inactive", isActive = false))

        // When
        val count = budgetDao.getActiveBudgetCount()

        // Then
        assertEquals(2, count)
    }

    // ==================== Flow 响应式更新测试 ====================

    @Test
    fun `getAllBudgets Flow emits on insert`() = runTest {
        // Given
        val flow = budgetDao.getAllBudgets()
        val initial = flow.first()
        assertEquals(0, initial.size)

        // When
        budgetDao.insertBudget(createTestBudget(id = 1, title = "New Budget"))
        val updated = flow.first()

        // Then
        assertEquals(1, updated.size)
    }

    @Test
    fun `getAllBudgets Flow emits on update`() = runTest {
        // Given
        val budget = createTestBudget(title = "Original")
        val budgetId = budgetDao.insertBudget(budget)
        val flow = budgetDao.getAllBudgets()

        // When
        val updatedBudget = budget.copy(
            id = budgetId,
            title = "Updated"
        )
        budgetDao.updateBudget(updatedBudget)
        val result = flow.first()

        // Then
        assertEquals("Updated", result[0].title)
    }

    @Test
    fun `getAllBudgets Flow emits on delete`() = runTest {
        // Given
        val budget = createTestBudget(title = "To Delete")
        val budgetId = budgetDao.insertBudget(budget)
        val flow = budgetDao.getAllBudgets()
        var current = flow.first()
        assertEquals(1, current.size)

        // When
        budgetDao.deleteBudgetById(budgetId)
        current = flow.first()

        // Then
        assertEquals(0, current.size)
    }

    // ==================== 通知设置测试 ====================

    @Test
    fun `insertBudget stores notification settings`() = runTest {
        // Given
        val budget = createTestBudget(
            title = "Notification Test",
            notifyAt80 = true,
            notifyAt90 = false,
            notifyAt100 = true
        )

        // When
        val budgetId = budgetDao.insertBudget(budget)
        val retrieved = budgetDao.getBudgetById(budgetId)

        // Then
        assertTrue(retrieved?.notifyAt80 == true)
        assertTrue(retrieved?.notifyAt90 == false)
        assertTrue(retrieved?.notifyAt100 == true)
    }

    @Test
    fun `updateBudget modifies notification settings`() = runTest {
        // Given
        val budget = createTestBudget(
            notifyAt80 = true,
            notifyAt90 = true,
            notifyAt100 = true
        )
        val budgetId = budgetDao.insertBudget(budget)

        // When
        val updated = budget.copy(
            id = budgetId,
            notifyAt80 = false,
            notifyAt90 = false,
            notifyAt100 = false
        )
        budgetDao.updateBudget(updated)
        val retrieved = budgetDao.getBudgetById(budgetId)

        // Then
        assertFalse(retrieved?.notifyAt80 == true)
        assertFalse(retrieved?.notifyAt90 == true)
        assertFalse(retrieved?.notifyAt100 == true)
    }

    // ==================== 辅助方法 ====================

    private fun createTestBudget(
        id: Long = 0,
        title: String = "Test Budget",
        amountCents: Long = 100000L,
        periodType: String = BudgetPeriodType.MONTHLY.name,
        startDate: Long = System.currentTimeMillis(),
        endDate: Long? = null,
        notifyAt80: Boolean = true,
        notifyAt90: Boolean = true,
        notifyAt100: Boolean = true,
        isActive: Boolean = true,
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    ): BudgetEntity {
        return BudgetEntity(
            id = id,
            title = title,
            amountCents = amountCents,
            periodType = periodType,
            startDate = startDate,
            endDate = endDate,
            notifyAt80 = notifyAt80,
            notifyAt90 = notifyAt90,
            notifyAt100 = notifyAt100,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
