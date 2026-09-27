package com.nortlinos.wearos.presentation.screen

import com.nortlinos.wearos.data.local.DownloadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DownloadOutcomesTest {
    @Test
    fun `an active download finishing or failing is confirmed`() {
        assertEquals(DownloadOutcome.COMPLETED, DownloadOutcomes.of(DownloadStatus.DOWNLOADING, DownloadStatus.DOWNLOADED))
        assertEquals(DownloadOutcome.COMPLETED, DownloadOutcomes.of(DownloadStatus.QUEUED, DownloadStatus.DOWNLOADED))
        assertEquals(DownloadOutcome.FAILED, DownloadOutcomes.of(DownloadStatus.DOWNLOADING, DownloadStatus.FAILED))
    }

    @Test
    fun `deleting a finished download is confirmed`() {
        assertEquals(DownloadOutcome.DELETED, DownloadOutcomes.of(DownloadStatus.DOWNLOADED, null))
    }

    @Test
    fun `opening a page on a settled download shows nothing`() {
        assertNull(DownloadOutcomes.of(null, DownloadStatus.DOWNLOADED))
        assertNull(DownloadOutcomes.of(null, DownloadStatus.FAILED))
    }

    @Test
    fun `cancelling and pausing need no confirmation`() {
        assertNull(DownloadOutcomes.of(DownloadStatus.DOWNLOADING, null))
        assertNull(DownloadOutcomes.of(DownloadStatus.DOWNLOADING, DownloadStatus.PAUSED))
    }
}
