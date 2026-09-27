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
    fun `presets match the requested set exactly`() {
        assertEquals(
            listOf(1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 2.0f, 3.0f),
            PlaybackSpeedFormat.PRESETS
        )
    }

    @Test
    fun `every preset formats without surprises`() {
        val expected = listOf("1", "1.1", "1.2", "1.3", "1.4", "1.5", "2", "3")
        assertEquals(expected, PlaybackSpeedFormat.PRESETS.map(PlaybackSpeedFormat::label))
    }

    @Test
    fun `sanitize keeps every valid preset unchanged`() {
        PlaybackSpeedFormat.PRESETS.forEach { speed ->
            assertEquals(speed, PlaybackSpeedFormat.sanitize(speed))
        }
    }

    @Test
    fun `sanitize falls back to 1x for anything outside the preset list`() {
        assertEquals(1.0f, PlaybackSpeedFormat.sanitize(0.75f))
        assertEquals(1.0f, PlaybackSpeedFormat.sanitize(1.35f))
        assertEquals(1.0f, PlaybackSpeedFormat.sanitize(5.0f))
        assertEquals(1.0f, PlaybackSpeedFormat.sanitize(-1.0f))
    }
}
