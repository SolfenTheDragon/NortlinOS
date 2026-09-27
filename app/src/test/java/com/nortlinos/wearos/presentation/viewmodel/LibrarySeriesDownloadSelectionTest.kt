package com.nortlinos.wearos.presentation.viewmodel

import com.nortlinos.wearos.data.local.DownloadStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class LibrarySeriesDownloadSelectionTest {
    @Test
    fun doesNotQueueBooksAlreadyDownloadedOrActive() {
        val selection = LibrarySeriesDownloadSelection.create(
            itemIds = listOf("downloaded", "queued", "active", "paused", "failed", "new", "new"),
            statuses = mapOf(
                "downloaded" to DownloadStatus.DOWNLOADED,
                "queued" to DownloadStatus.QUEUED,
                "active" to DownloadStatus.DOWNLOADING,
                "paused" to DownloadStatus.PAUSED,
                "failed" to DownloadStatus.FAILED
            )
        )

        assertEquals(listOf("paused", "failed", "new"), selection.itemIdsToQueue)
        assertEquals(3, selection.skippedCount)
    }
}
