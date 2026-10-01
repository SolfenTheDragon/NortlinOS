package com.nortlinos.wearos.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteProgressKeyTest {

    @Test
    fun `books are keyed by their own id with no episode`() {
        assertEquals("li_book" to null, RemoteProgressKey.of("li_book", podcastId = null))
    }

    @Test
    fun `podcast episodes are keyed by podcast then episode`() {
        assertEquals("li_podcast" to "ep_1", RemoteProgressKey.of("ep_1", podcastId = "li_podcast"))
    }

    @Test
    fun `episode keys match how me mediaProgress indexes episode entries`() {
        // ProgressSyncEngine indexes /api/me mediaProgress by (libraryItemId, episodeId).
        val remote = mapOf(
            ("li_podcast" to "ep_1") to "episode",
            ("li_book" to null) to "book"
        )
        assertEquals("episode", remote[RemoteProgressKey.of("ep_1", "li_podcast")])
        assertEquals("book", remote[RemoteProgressKey.of("li_book", null)])
        assertEquals(null, remote[RemoteProgressKey.of("ep_2", "li_podcast")])
    }
}
