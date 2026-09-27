package com.nortlinos.wearos.presentation.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The one-second position ticker is a battery cost: every tick is a binder round trip to the
 * playback service plus a recomposition. These cases pin the conditions under which it is allowed
 * to run, so the gating cannot be removed without a failing test.
 */
class PlaybackTickerTest {
    @Test
    fun `ticks while playing and visible`() {
        assertTrue(
            PlaybackTicker.shouldTick(
                playing = true,
                ambient = false,
                playerScreenActive = true
            )
        )
    }

    @Test
    fun `does not tick while paused`() {
        assertFalse(
            PlaybackTicker.shouldTick(
                playing = false,
                ambient = false,
                playerScreenActive = true
            )
        )
    }

    @Test
    fun `does not tick in ambient always-on mode`() {
        assertFalse(
            PlaybackTicker.shouldTick(
                playing = true,
                ambient = true,
                playerScreenActive = true
            )
        )
    }

    @Test
    fun `does not tick during background playback`() {
        assertFalse(
            PlaybackTicker.shouldTick(
                playing = true,
                ambient = false,
                playerScreenActive = false
            )
        )
    }
}
