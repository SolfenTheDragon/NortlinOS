package com.nortlinos.wearos.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class BookmarkTitleFormatTest {

    @Test
    fun `positions under an hour use minutes colon seconds`() {
        assertEquals("Bookmark at 0:00", BookmarkTitleFormat.forPosition(0L))
        assertEquals("Bookmark at 0:16", BookmarkTitleFormat.forPosition(16_000L))
        assertEquals("Bookmark at 5:09", BookmarkTitleFormat.forPosition(309_000L))
        assertEquals("Bookmark at 59:59", BookmarkTitleFormat.forPosition(3_599_000L))
    }

    @Test
    fun `positions an hour or more include an hours component`() {
        assertEquals("Bookmark at 1:00:00", BookmarkTitleFormat.forPosition(3_600_000L))
        assertEquals("Bookmark at 1:02:03", BookmarkTitleFormat.forPosition(3_723_000L))
        assertEquals("Bookmark at 10:00:00", BookmarkTitleFormat.forPosition(36_000_000L))
    }

    @Test
    fun `negative positions clamp to zero instead of throwing`() {
        assertEquals("Bookmark at 0:00", BookmarkTitleFormat.forPosition(-5_000L))
    }
}
