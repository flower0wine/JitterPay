package com.example.jitterpay.data.repository

import com.example.jitterpay.data.local.dao.TransactionDao
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * TransactionRepository 单元测试
 *
 * 测试仓库层的核心功能：
 * - 交易添加
 * - 交易查询
 * - 统计计算
 */
class TransactionRepositoryTest {

    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        transactionDao = mockk(relaxed = true)
        repository = TransactionRepository(transactionDao)
    }

    @Test
    fun `addTransaction calls dao insert and returns id`() = runTest {
        // Given
        coEvery { transactionDao.insertTransaction(any()) } returns 1L

        // When
        val result = repository.addTransaction(
            type = TransactionType.EXPENSE,
            amountCents = 550L,
            category = "Dining",
            dateMillis = System.currentTimeMillis()
        )

        // Then
        assertEquals(1L, result)
        coVerify {
            transactionDao.insertTransaction(match {
                it.type == TransactionType.EXPENSE.name &&
                it.amountCents == 550L &&
                it.category == "Dining"
            })
        }
    }

    @Test
    fun `addTransaction with description stores description`() = runTest {
        // Given
        coEvery { transactionDao.insertTransaction(any()) } returns 1L

        // When
        repository.addTransaction(
            type = TransactionType.EXPENSE,
            amountCents = 1000L,
            category = "Shopping",
            description = "Test purchase",
            dateMillis = System.currentTimeMillis()
        )

        // Then
        coVerify {
            transactionDao.insertTransaction(match {
                it.description == "Test purchase"
            })
        }
    }

    @Test
    fun `getAllTransactions returns flow from dao`() = runTest {
        // Given
        val expectedTransactions = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.EXPENSE.name,
                amountCents = 550L,
                category = "Dining",
                dateMillis = System.currentTimeMillis()
            )
        )
        every { transactionDao.getAllTransactions() } returns flowOf(expectedTransactions)

        // When
        val result = repository.getAllTransactions().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Dining", result[0].category)
    }

    @Test
    fun `getTransactionById returns from dao`() = runTest {
        // Given
        val expectedTransaction = TransactionEntity(
            id = 1,
            type = TransactionType.INCOME.name,
            amountCents = 5000L,
            category = "Salary",
            dateMillis = System.currentTimeMillis()
        )
        coEvery { transactionDao.getTransactionById(1L) } returns expectedTransaction

        // When
        val result = repository.getTransactionById(1L)

        // Then
        assertNotNull(result)
        assertEquals("Salary", result?.category)
        assertEquals(5000L, result?.amountCents)
    }

    @Test
    fun `getTransactionById returns null for non-existent id`() = runTest {
        // Given
        coEvery { transactionDao.getTransactionById(999L) } returns null

        // When
        val result = repository.getTransactionById(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `getTransactionsByType filters by type`() = runTest {
        // Given
        val expenseTransactions = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.EXPENSE.name,
                amountCents = 100L,
                category = "Food",
                dateMillis = System.currentTimeMillis()
            )
        )
        every { transactionDao.getTransactionsByType(TransactionType.EXPENSE.name) } returns flowOf(expenseTransactions)

        // When
        val result = repository.getTransactionsByType(TransactionType.EXPENSE).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(TransactionType.EXPENSE.name, result[0].type)
    }

    @Test
    fun `getTransactionsByCategory filters by category`() = runTest {
        // Given
        val diningTransactions = listOf(
            TransactionEntity(
                id = 1,
                type = TransactionType.EXPENSE.name,
                amountCents = 500L,
                category = "Dining",
                dateMillis = System.currentTimeMillis()
            )
        )
        every { transactionDao.getTransactionsByCategory("Dining") } returns flowOf(diningTransactions)

        // When
        val result = repository.getTransactionsByCategory("Dining").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Dining", result[0].category)
    }

    @Test
    fun `getTotalBalance returns flow from dao`() = runTest {
        // Given
        val expectedBalance = 1000L // $10.00
        every { transactionDao.getTotalBalance() } returns flowOf(expectedBalance)

        // When
        val result = repository.getTotalBalance().first()

        // Then
        assertEquals(1000L, result)
    }

    @Test
    fun `getMonthlyIncome returns flow from dao`() = runTest {
        // Given
        val expectedIncome = 500000L // $5000.00
        every { transactionDao.getMonthlyIncome(any(), any()) } returns flowOf(expectedIncome)

        // When
        val result = repository.getMonthlyIncome(0L, System.currentTimeMillis()).first()

        // Then
        assertEquals(500000L, result)
    }

    @Test
    fun `getMonthlyExpense returns flow from dao`() = runTest {
        // Given
        val expectedExpense = 10000L // $100.00
        every { transactionDao.getMonthlyExpense(any(), any()) } returns flowOf(expectedExpense)

        // When
        val result = repository.getMonthlyExpense(0L, System.currentTimeMillis()).first()

        // Then
        assertEquals(10000L, result)
    }

    @Test
    fun `deleteTransaction calls dao delete`() = runTest {
        // Given
        val transaction = TransactionEntity(
            id = 1,
            type = TransactionType.EXPENSE.name,
            amountCents = 100L,
            category = "Test",
            dateMillis = System.currentTimeMillis()
        )

        // When
        repository.deleteTransaction(transaction)

        // Then
        coVerify { transactionDao.deleteTransaction(transaction) }
    }

    @Test
    fun `deleteTransactionById calls dao with correct id`() = runTest {
        // When
        repository.deleteTransactionById(1L)

        // Then
        coVerify { transactionDao.deleteTransactionById(1L) }
    }

    @Test
    fun `updateTransaction updates with new timestamp`() = runTest {
        // Given
        val transaction = TransactionEntity(
            id = 1,
            type = TransactionType.EXPENSE.name,
            amountCents = 100L,
            category = "Test",
            dateMillis = System.currentTimeMillis(),
            updatedAt = 0L
        )

        // When
        repository.updateTransaction(transaction)

        // Then
        coVerify {
            transactionDao.updateTransaction(match {
                it.updatedAt > 0
            })
        }
    }

    @Test
    fun `getTransactionCount returns from dao`() = runTest {
        // Given
        coEvery { transactionDao.getTransactionCount() } returns 5

        // When
        val result = repository.getTransactionCount()

        // Then
        assertEquals(5, result)
    }

    @Test
    fun `deleteAllTransactions calls dao method`() = runTest {
        // When
        repository.deleteAllTransactions()

        // Then
        coVerify { transactionDao.deleteAllTransactions() }
    }
}
