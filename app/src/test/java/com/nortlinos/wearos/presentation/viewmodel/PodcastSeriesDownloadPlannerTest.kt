package com.nortlinos.wearos.presentation.viewmodel

import com.nortlinos.wearos.data.local.DownloadStatus
import com.nortlinos.wearos.data.local.DownloadedItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PodcastSeriesDownloadPlannerTest {
    @Test
    fun `estimates remaining space while skipping active and completed episodes`() {
        val plan = PodcastSeriesDownloadPlanner.create(
            itemIds = listOf("done", "active", "partial", "new"),
            sizes = mapOf("done" to 800L, "active" to 700L, "partial" to 500L, "new" to 300L),
            downloads = mapOf(
                "done" to download("done", DownloadStatus.DOWNLOADED, 800),
                "active" to download("active", DownloadStatus.DOWNLOADING, 120),
                "partial" to download("partial", DownloadStatus.PAUSED, 200)
            )
        )

        assertEquals(listOf("partial", "new"), plan.itemIdsToQueue)
        assertEquals(2, plan.skippedCount)
        assertEquals(600L, plan.requiredBytes)
        assertEquals(0, plan.unknownSizeCount)
    }

    @Test
    fun `marks missing episode sizes for an explicit space warning`() {
        val plan = PodcastSeriesDownloadPlanner.create(
            itemIds = listOf("known", "unknown"),
            sizes = mapOf("known" to 400L),
            downloads = emptyMap()
        )

        assertEquals(400L, plan.requiredBytes)
        assertEquals(1, plan.unknownSizeCount)
    }

    private fun download(
        itemId: String,
        status: DownloadStatus,
        downloadedBytes: Long
    ) = DownloadedItemEntity(
        itemId = itemId,
        originServerUrl = "https://abs.example",
        status = status,
        localDirectory = "/unused",
        fileSizeBytes = 0,
        downloadedBytes = downloadedBytes
    )
}
