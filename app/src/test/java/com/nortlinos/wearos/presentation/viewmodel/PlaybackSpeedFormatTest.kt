package com.nortlinos.wearos.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackSpeedFormatTest {

    @Test
    fun `whole numbers drop the decimal`() {
        assertEquals("1", PlaybackSpeedFormat.label(1.0f))
        assertEquals("2", PlaybackSpeedFormat.label(2.0f))
        assertEquals("3", PlaybackSpeedFormat.label(3.0f))
    }

    @Test
    fun `fractional speeds keep one decimal place`() {
        assertEquals("1.1", PlaybackSpeedFormat.label(1.1f))
        assertEquals("1.2", PlaybackSpeedFormat.label(1.2f))
        assertEquals("1.5", PlaybackSpeedFormat.label(1.5f))
    }

    @Test
    fun `step moves by one tenth without float drift`() {
        var speed = 1.0f
        repeat(7) { speed = PlaybackSpeedFormat.step(speed, 1) }
        assertEquals("1.7", PlaybackSpeedFormat.label(speed))
        repeat(12) { speed = PlaybackSpeedFormat.step(speed, -1) }
        assertEquals("0.5", PlaybackSpeedFormat.label(speed))
    }

    @Test
    fun `step clamps at the bounds`() {
        assertEquals(PlaybackSpeedFormat.MAX, PlaybackSpeedFormat.step(PlaybackSpeedFormat.MAX, 1))
        assertEquals(PlaybackSpeedFormat.MIN, PlaybackSpeedFormat.step(PlaybackSpeedFormat.MIN, -1))
    }

    @Test
    fun `sanitize snaps to a tenth and rejects out-of-range values`() {
        assertEquals(1.4f, PlaybackSpeedFormat.sanitize(1.38f))
        assertEquals(1.0f, PlaybackSpeedFormat.sanitize(5.0f))
        assertEquals(1.0f, PlaybackSpeedFormat.sanitize(-1.0f))
        assertEquals(1.0f, PlaybackSpeedFormat.sanitize(Float.NaN))
    }
}
