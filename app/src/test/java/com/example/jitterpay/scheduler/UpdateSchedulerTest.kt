package com.example.jitterpay.scheduler

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.jitterpay.worker.ApkDownloadWorker
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for UpdateScheduler.
 *
 * Tests scheduling, canceling, and status checking of APK download work.
 */
@RunWith(RobolectricTestRunner::class)
class UpdateSchedulerTest {

    private lateinit var context: Context
    private lateinit var scheduler: UpdateScheduler

    @Before
    fun setup() {
        context = org.robolectric.RuntimeEnvironment.getApplication()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        scheduler = UpdateScheduler(context)
    }

    @After
    fun tearDown() {
        // No cleanup needed - WorkManagerTestInitHelper initializes for the test context
    }

    @Test
    fun `scheduleDownload enqueues work request`() {
        // When
        scheduler.scheduleDownload(
            version = "2",
            versionName = "v2.0.0",
            downloadUrl = "https://store.example.com/v2.0.0/jitterpay-arm64-v8a-2.0.0.apk",
            apkSize = 15000000L,
            releaseDate = "2026-01-30"
        )

        // Then
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()

        assertTrue("Work should be scheduled", workInfos.isNotEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
    }

    @Test
    fun `scheduleDownload adds correct tags`() {
        // When
        scheduler.scheduleDownload(
            version = "2",
            versionName = "v2.0.0",
            downloadUrl = "https://store.example.com/apk",
            apkSize = 15000000L,
            releaseDate = "2026-01-30"
        )

        // Then
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()

        assertTrue("Work should have download tag", workInfos.first().tags.contains(ApkDownloadWorker.WORK_NAME))
    }

    @Test
    fun `scheduleDownload replaces existing work`() {
        // Given - schedule work first time
        scheduler.scheduleDownload(
            version = "1",
            versionName = "v1.0.0",
            downloadUrl = "https://store.example.com/v1.0.0/apk",
            apkSize = 10000000L,
            releaseDate = "2025-01-01"
        )

        val workManager = WorkManager.getInstance(context)
        val initialWorkInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()
        val initialCount = initialWorkInfos.size

        // When - schedule again
        scheduler.scheduleDownload(
            version = "2",
            versionName = "v2.0.0",
            downloadUrl = "https://store.example.com/v2.0.0/apk",
            apkSize = 15000000L,
            releaseDate = "2026-01-30"
        )

        // Then - should replace, not duplicate
        val updatedWorkInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()

        assertEquals(
            "Should replace existing work, not add duplicate",
            initialCount.toLong(),
            updatedWorkInfos.size.toLong()
        )
    }

    @Test
    fun `cancelDownload removes scheduled work`() {
        // Given
        scheduler.scheduleDownload(
            version = "2",
            versionName = "v2.0.0",
            downloadUrl = "https://store.example.com/apk",
            apkSize = 15000000L,
            releaseDate = "2026-01-30"
        )

        val workManager = WorkManager.getInstance(context)
        var workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()
        assertTrue("Work should be scheduled initially", workInfos.isNotEmpty())

        // When
        scheduler.cancelDownload()

        // Then
        workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()
        val anyRunning = workInfos.any {
            it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
        }
        assertTrue("All scheduled work should be cancelled", !anyRunning)
    }

    @Test
    fun `getDownloadState returns IDLE when no work scheduled`() = runBlocking {
        // When
        val state = scheduler.getDownloadState()

        // Then
        assertEquals(DownloadState.IDLE, state)
    }

    @Test
    fun `getDownloadState returns ENQUEUED when work is enqueued`() = runBlocking {
        // Given
        scheduler.scheduleDownload(
            version = "2",
            versionName = "v2.0.0",
            downloadUrl = "https://store.example.com/apk",
            apkSize = 15000000L,
            releaseDate = "2026-01-30"
        )

        // When
        val state = scheduler.getDownloadState()

        // Then
        assertEquals(DownloadState.ENQUEUED, state)
    }

    @Test
    fun `isDownloading returns true when work is enqueued`() = runBlocking {
        // Given
        scheduler.scheduleDownload(
            version = "2",
            versionName = "v2.0.0",
            downloadUrl = "https://store.example.com/apk",
            apkSize = 15000000L,
            releaseDate = "2026-01-30"
        )

        // When
        val isDownloading = scheduler.isDownloading()

        // Then
        assertTrue("Should return true when work is enqueued", isDownloading)
    }

    @Test
    fun `isDownloading returns false when no work scheduled`() = runBlocking {
        // When
        val isDownloading = scheduler.isDownloading()

        // Then
        assertFalse("Should return false when no work scheduled", isDownloading)
    }

    @Test
    fun `scheduleDownload with valid parameters creates work`() {
        // When
        scheduler.scheduleDownload(
            version = "2",
            versionName = "v2.0.0",
            downloadUrl = "https://store.example.com/v2.0.0/jitterpay-arm64-v8a-2.0.0.apk",
            apkSize = 15000000L,
            releaseDate = "2026-01-30"
        )

        // Then - verify work was created with unique name
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()

        assertEquals("Should have exactly one work request", 1, workInfos.size)
        assertEquals("Work should be in ENQUEUED state", WorkInfo.State.ENQUEUED, workInfos.first().state)
    }

    @Test
    fun `cancelDownload works when no work is scheduled`() {
        // When - cancel without scheduling first
        scheduler.cancelDownload()

        // Then - no exception should be thrown
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(ApkDownloadWorker.WORK_NAME).get()
        assertTrue("No work should be scheduled", workInfos.isEmpty())
    }
}
