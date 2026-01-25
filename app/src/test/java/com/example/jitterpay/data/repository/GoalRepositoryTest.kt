package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.entity.GoalEntity
import com.example.jitterpay.data.local.entity.GoalIconType
import com.example.jitterpay.data.local.entity.GoalTransactionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * GoalRepository 单元测试
 *
 * 测试仓库层的核心功能：
 * - 目标创建
 * - 资金存入和取出
 * - 目标查询
 * - 统计计算
 */
class GoalRepositoryTest {

    private lateinit var goalDao: GoalDao
    private lateinit var goalTransactionDao: GoalTransactionDao
    private lateinit var repository: GoalRepository

    @Before
    fun setup() {
        goalDao = mockk(relaxed = true)
        goalTransactionDao = mockk(relaxed = true)
        repository = GoalRepository(goalDao, goalTransactionDao)
    }

    @Test
    fun `createGoal inserts goal and returns id`() = runTest {
        // Given
        coEvery { goalDao.insertGoal(any()) } returns 1L

        // When
        val result = repository.createGoal(
            title = "Emergency Fund",
            targetAmountCents = 10000L,
            iconType = GoalIconType.SHIELD.name
        )

        // Then
        assertEquals(1L, result)
        coVerify {
            goalDao.insertGoal(match {
                it.title == "Emergency Fund" &&
                it.targetAmountCents == 10000L &&
                it.currentAmountCents == 0L &&
                it.iconType == GoalIconType.SHIELD.name &&
                !it.isCompleted
            })
        }
    }

    @Test
    fun `getAllGoals returns flow from dao`() = runTest {
        // Given
        val expectedGoals = listOf(
            GoalEntity(
                id = 1,
                title = "Goal 1",
                targetAmountCents = 10000L,
                currentAmountCents = 5000L,
                iconType = "SHIELD",
                isCompleted = false
            )
        )
        coEvery { goalDao.getAllGoals() } returns flowOf(expectedGoals)

        // When
        val result = repository.getAllGoals().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Goal 1", result[0].title)
    }

    @Test
    fun `getGoalById returns from dao`() = runTest {
        // Given
        val expectedGoal = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 5000L,
            currentAmountCents = 2500L,
            iconType = "FLIGHT",
            isCompleted = false
        )
        coEvery { goalDao.getGoalById(1L) } returns expectedGoal

        // When
        val result = repository.getGoalById(1L)

        // Then
        assertNotNull(result)
        assertEquals("Test Goal", result?.title)
        assertEquals(2500L, result?.currentAmountCents)
    }

    @Test
    fun `getGoalById returns null for non-existent id`() = runTest {
        // Given
        coEvery { goalDao.getGoalById(999L) } returns null

        // When
        val result = repository.getGoalById(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `addFundsToGoal updates goal and creates transaction`() = runTest {
        // Given
        val goal = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 5000L,
            iconType = "SHIELD",
            isCompleted = false
        )
        coEvery { goalDao.getGoalById(1L) } returns goal
        coEvery { goalDao.updateGoal(any()) } returns Unit
        coEvery { goalTransactionDao.insertGoalTransaction(any()) } returns 100L

        // When
        val newAmount = repository.addFundsToGoal(
            goalId = 1L,
            amountCents = 2500L,
            description = "Bonus"
        )

        // Then
        assertEquals(7500L, newAmount)
        coVerify {
            goalDao.updateGoal(match {
                it.currentAmountCents == 7500L &&
                !it.isCompleted
            })
            goalTransactionDao.insertGoalTransaction(match {
                it.goalId == 1L &&
                it.amountCents == 2500L &&
                it.description == "Bonus" &&
                it.type == "DEPOSIT"
            })
        }
    }

    @Test
    fun `addFundsToGoal marks goal as completed when target reached`() = runTest {
        // Given
        val goal = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 9500L,
            iconType = "SHIELD",
            isCompleted = false
        )
        coEvery { goalDao.getGoalById(1L) } returns goal
        coEvery { goalDao.updateGoal(any()) } returns Unit
        coEvery { goalTransactionDao.insertGoalTransaction(any()) } returns 100L

        // When
        val newAmount = repository.addFundsToGoal(
            goalId = 1L,
            amountCents = 1000L,
            description = "Final payment"
        )

        // Then
        assertEquals(10500L, newAmount)
        coVerify {
            goalDao.updateGoal(match {
                it.currentAmountCents == 10500L &&
                it.isCompleted == true
            })
        }
    }

    @Test
    fun `withdrawFromGoal updates goal and creates transaction`() = runTest {
        // Given
        val goal = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 7500L,
            iconType = "SHIELD",
            isCompleted = false
        )
        coEvery { goalDao.getGoalById(1L) } returns goal
        coEvery { goalDao.updateGoal(any()) } returns Unit
        coEvery { goalTransactionDao.insertGoalTransaction(any()) } returns 100L

        // When
        val newAmount = repository.withdrawFromGoal(
            goalId = 1L,
            amountCents = 1000L,
            description = "Emergency"
        )

        // Then
        assertEquals(6500L, newAmount)
        coVerify {
            goalDao.updateGoal(match {
                it.currentAmountCents == 6500L &&
                !it.isCompleted
            })
            goalTransactionDao.insertGoalTransaction(match {
                it.goalId == 1L &&
                it.amountCents == 1000L &&
                it.description == "Emergency" &&
                it.type == "WITHDRAWAL"
            })
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `withdrawFromGoal throws exception when insufficient funds`() = runTest {
        // Given
        val goal = GoalEntity(
            id = 1,
            title = "Test Goal",
            targetAmountCents = 10000L,
            currentAmountCents = 500L,
            iconType = "SHIELD",
            isCompleted = false
        )
        coEvery { goalDao.getGoalById(1L) } returns goal

        // When
        repository.withdrawFromGoal(
            goalId = 1L,
            amountCents = 1000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `withdrawFromGoal throws exception when goal not found`() = runTest {
        // Given
        coEvery { goalDao.getGoalById(1L) } returns null

        // When
        repository.withdrawFromGoal(
            goalId = 1L,
            amountCents = 1000L
        )
    }

    @Test
    fun `deleteGoalById calls dao`() = runTest {
        // When
        repository.deleteGoalById(1L)

        // Then
        coVerify { goalDao.deleteGoalById(1L) }
    }

    @Test
    fun `getTransactionsByGoalId returns from dao`() = runTest {
        // Given
        val expectedTransactions = listOf(
            GoalTransactionEntity(
                id = 1,
                goalId = 1L,
                type = "DEPOSIT",
                amountCents = 2500L,
                dateMillis = System.currentTimeMillis()
            )
        )
        coEvery { goalTransactionDao.getTransactionsByGoalId(1L) } returns flowOf(expectedTransactions)

        // When
        val result = repository.getTransactionsByGoalId(1L).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("DEPOSIT", result[0].type)
    }

    @Test
    fun `deleteGoalTransaction calls dao`() = runTest {
        // Given
        val transaction = GoalTransactionEntity(
            id = 1,
            goalId = 1L,
            type = "DEPOSIT",
            amountCents = 100L,
            dateMillis = System.currentTimeMillis()
        )

        // When
        repository.deleteGoalTransaction(transaction)

        // Then
        coVerify { goalTransactionDao.deleteGoalTransaction(transaction) }
    }

    @Test
    fun `getGoalCount returns from dao`() = runTest {
        // Given
        coEvery { goalDao.getGoalCount() } returns 5

        // When
        val result = repository.getGoalCount()

        // Then
        assertEquals(5, result)
    }

    @Test
    fun `getCompletedGoalCount returns from dao`() = runTest {
        // Given
        coEvery { goalDao.getCompletedGoalCount() } returns 3

        // When
        val result = repository.getCompletedGoalCount()

        // Then
        assertEquals(3, result)
    }

    @Test
    fun `getTotalTargetAmount returns from dao`() = runTest {
        // Given
        coEvery { goalDao.getTotalTargetAmount() } returns 25000L

        // When
        val result = repository.getTotalTargetAmount()

        // Then
        assertEquals(25000L, result)
    }

    @Test
    fun `getTotalCurrentAmount returns from dao`() = runTest {
        // Given
        coEvery { goalDao.getTotalCurrentAmount() } returns 15000L

        // When
        val result = repository.getTotalCurrentAmount()

        // Then
        assertEquals(15000L, result)
    }

    @Test
    fun `getTotalProgress calculates correctly`() = runTest {
        // Given
        val goals = listOf(
            GoalEntity(
                id = 1,
                title = "Goal 1",
                targetAmountCents = 10000L,
                currentAmountCents = 5000L,
                iconType = "SHIELD",
                isCompleted = false
            ),
            GoalEntity(
                id = 2,
                title = "Goal 2",
                targetAmountCents = 5000L,
                currentAmountCents = 5000L,
                iconType = "FLIGHT",
                isCompleted = true
            )
        )
        coEvery { goalDao.getAllGoals() } returns flowOf(goals)

        // When
        val result = repository.getTotalProgress().first()

        // Then
        // Total target: 15000, Total current: 10000
        // Progress: 10000 / 15000 = 0.6666...
        assertEquals(0.67f, result, 0.01f)
    }

    @Test
    fun `getTotalProgress returns 0 when no goals`() = runTest {
        // Given
        coEvery { goalDao.getAllGoals() } returns flowOf(emptyList())

        // When
        val result = repository.getTotalProgress().first()

        // Then
        assertEquals(0f, result, 0.0f)
    }
}
