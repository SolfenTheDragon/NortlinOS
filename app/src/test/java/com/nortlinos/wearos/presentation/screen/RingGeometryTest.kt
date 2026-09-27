package com.nortlinos.wearos.presentation.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class RingGeometryTest {
    @Test
    fun `bounds wrap chapter marks with the start and end of the book`() {
        assertEquals(listOf(0f, 0.25f, 0.5f, 1f), RingGeometry.bounds(listOf(0.25f, 0.5f)))
        assertEquals(listOf(0f, 1f), RingGeometry.bounds(emptyList()))
    }

    @Test
    fun `bounds ignore marks outside the book`() {
        assertEquals(listOf(0f, 0.5f, 1f), RingGeometry.bounds(listOf(0f, 0.5f, 1f, 1.2f)))
    }

    @Test
    fun `a single segment is a closed ring`() {
        assertEquals(0f, RingGeometry.gapDegrees(1), 0f)
    }

    @Test
    fun `gaps shrink for books with many chapters`() {
        assertEquals(2f, RingGeometry.gapDegrees(12), 0f)
        // 300 chapters: 1.2 degrees each, so fixed 2-degree gaps would erase every segment.
        assertEquals(0.36f, RingGeometry.gapDegrees(300), 0.001f)
    }
}
