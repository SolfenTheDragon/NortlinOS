package com.nortlinos.wearos.tile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NowPlayingSnapshotTest {
    private val base = NowPlayingSnapshot(
        itemId = "book",
        originServerUrl = "https://abs.example",
        title = "Dune",
        author = "Frank Herbert",
        playing = true,
        positionMs = 60_000,
        durationMs = 3 * 3_600_000L + 11 * 60_000L
    )

    @Test
    fun statusShowsStateAndTimeLeftRoundedUp() {
        assertEquals("Playing \u00b7 3h 10m left", base.status())
        assertEquals("Paused \u00b7 1m left", base.copy(playing = false, positionMs = base.durationMs - 1).status())
        assertEquals("Paused", base.copy(playing = false, durationMs = 0).status())
    }

    @Test
    fun refreshesOnlyForVisibleChanges() {
        assertTrue(base.needsTileRefresh(null))
        assertTrue(base.copy(playing = false).needsTileRefresh(base))
        assertTrue(base.copy(itemId = "other").needsTileRefresh(base))
        // Position ticks while playing are not worth a tile render.
        assertFalse(base.copy(positionMs = 120_000).needsTileRefresh(base))
        val paused = base.copy(playing = false)
        assertTrue(paused.copy(positionMs = 30_000).needsTileRefresh(paused))
        assertTrue(base.copy(coverItemId = "book").needsTileRefresh(base))
    }

    @Test
    fun coverOnlyCountsForTheSameBook() {
        assertFalse(base.hasCover)
        assertTrue(base.copy(coverItemId = "book").hasCover)
        assertFalse(base.copy(coverItemId = "older book").hasCover)
    }

    @Test
    fun pauseClickIdChangesOnEveryResume() {
        val playing = base.copy(playingSince = 1_000)
        // Stable while one run of playback continues, so the tile's button keeps working...
        assertEquals(playing.pauseClickId, playing.copy(positionMs = 90_000).pauseClickId)
        // ...but a later resume at the very same position never matches an old tap.
        assertNotEquals(playing.pauseClickId, playing.copy(playingSince = 2_000).pauseClickId)
        assertNotEquals(playing.pauseClickId, playing.copy(itemId = "other").pauseClickId)
    }
}
