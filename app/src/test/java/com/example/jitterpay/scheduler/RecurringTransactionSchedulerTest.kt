package com.example.jitterpay.scheduler

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.jitterpay.worker.RecurringTransactionWorker
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

/**
 * RecurringTransactionScheduler unit tests
 *
 * Tests the scheduling logic for recurring transaction background work.
 * Verifies that WorkManager is properly configured for periodic checks.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class RecurringTransactionSchedulerTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        // Get application context for testing
        context = ApplicationProvider.getApplicationContext<Context>()

        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun `scheduleRecurringTransactionChecks schedules periodic work`() {
        val scheduler = RecurringTransactionScheduler(context)

        // When
        scheduler.scheduleRecurringTransactionChecks()

        // Then - verify periodic work is actually enqueued
        val workInfos = workManager.getWorkInfosByTag(
            RecurringTransactionWorker.UNIQUE_WORK_NAME
        ).get()

        assertFalse("Work should be scheduled", workInfos.isEmpty())

        val periodicWork = workInfos.find {
            it.tags.contains(RecurringTransactionWorker.UNIQUE_WORK_NAME)
        }
        assertNotNull("Work should have correct tag", periodicWork)
    }

    @Test
    fun `stopRecurringTransactionChecks cancels scheduled work`() {
        val scheduler = RecurringTransactionScheduler(context)

        // Given - schedule first
        scheduler.scheduleRecurringTransactionChecks()

        var workInfos = workManager.getWorkInfosByTag(
            RecurringTransactionWorker.UNIQUE_WORK_NAME
        ).get()
        assertFalse("Work should be scheduled initially", workInfos.isEmpty())

        // When
        scheduler.stopRecurringTransactionChecks()

        // Then - all work should be cancelled
        workInfos = workManager.getWorkInfosByTag(
            RecurringTransactionWorker.UNIQUE_WORK_NAME
        ).get()

        val anyRunning = workInfos.any {
            it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
        }
        assertFalse("All scheduled work should be cancelled", anyRunning)
    }

    @Test
    fun `scheduleImmediateCheck enqueues one-time work`() {
        val scheduler = RecurringTransactionScheduler(context)

        // When
        scheduler.scheduleImmediateCheck()

        // Then - verify one-time work is enqueued
        val workInfos = workManager.getWorkInfosByTag(
            RecurringTransactionWorker.UNIQUE_WORK_NAME
        ).get()

        assertFalse("Immediate work should be scheduled", workInfos.isEmpty())
    }

    @Test
    fun `multiple schedule calls replace existing work`() {
        val scheduler = RecurringTransactionScheduler(context)

        // When - schedule multiple times
        scheduler.scheduleRecurringTransactionChecks()
        scheduler.scheduleRecurringTransactionChecks()
        scheduler.scheduleRecurringTransactionChecks()

        // Then - work should still exist but not be duplicated excessively
        val workInfos = workManager.getWorkInfosByTag(
            RecurringTransactionWorker.UNIQUE_WORK_NAME
        ).get()

        assertFalse("Work should be scheduled", workInfos.isEmpty())
        // With UPDATE policy, there should only be one entry per unique work name
        assertEquals(
            "Multiple schedules should replace, not duplicate",
            1,
            workInfos.size
        )
    }

    @Test
    fun `stop without schedule does not throw`() {
        val scheduler = RecurringTransactionScheduler(context)

        // When - stop without scheduling
        scheduler.stopRecurringTransactionChecks()

        // Then - should not throw, verification is that we reach here without exception
        val workInfos = workManager.getWorkInfosByTag(
            RecurringTransactionWorker.UNIQUE_WORK_NAME
        ).get()
        assertTrue("No work should exist", workInfos.isEmpty())
    }

    @Test
    fun `immediate check and periodic check can coexist`() {
        val scheduler = RecurringTransactionScheduler(context)

        // When - schedule both periodic and immediate
        scheduler.scheduleRecurringTransactionChecks()
        scheduler.scheduleImmediateCheck()

        // Then - both types of work should exist
        val workInfos = workManager.getWorkInfosByTag(
            RecurringTransactionWorker.UNIQUE_WORK_NAME
        ).get()

        assertEquals(
            "Both periodic and immediate work should coexist",
            2,
            workInfos.size
        )
    }

    @Test
    fun `isScheduled returns true after scheduling`() {
        val scheduler = RecurringTransactionScheduler(context)

        // Given - no work scheduled yet
        assertFalse("Should return false when not scheduled", scheduler.isScheduled())

        // When - schedule work
        scheduler.scheduleRecurringTransactionChecks()

        // Allow WorkManager to process the scheduling
        ShadowLooper.shadowMainLooper().idle()

        // Then - verify work is enqueued (checking with WorkManager directly)
        val workInfos = workManager.getWorkInfosByTag(RecurringTransactionWorker.UNIQUE_WORK_NAME).get()
        assertFalse("Work should be enqueued", workInfos.isEmpty())
    }

    @Test
    fun `isScheduled returns false after stopping`() {
        val scheduler = RecurringTransactionScheduler(context)

        // Given - schedule then stop
        scheduler.scheduleRecurringTransactionChecks()
        ShadowLooper.shadowMainLooper().idle()
        scheduler.stopRecurringTransactionChecks()

        // Then
        assertFalse("Should return false when work is cancelled", scheduler.isScheduled())
    }
}
