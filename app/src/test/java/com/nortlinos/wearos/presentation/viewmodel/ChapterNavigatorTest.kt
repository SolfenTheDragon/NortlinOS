package com.nortlinos.wearos.presentation.viewmodel

import com.nortlinos.wearos.data.local.ChapterEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChapterNavigatorTest {
    private fun chapter(index: Int, startMs: Long, endMs: Long) = ChapterEntity(
        itemId = "li_1",
        originServerUrl = "https://abs.example.com",
        chapterId = index,
        title = "Chapter ${index + 1}",
        startMs = startMs,
        endMs = endMs
    )

    // Three 10-minute chapters spanning half an hour.
    private val chapters = listOf(
        chapter(0, 0, 600_000),
        chapter(1, 600_000, 1_200_000),
        chapter(2, 1_200_000, 1_800_000)
    )

    @Test
    fun `next chapter jumps to the following start`() {
        assertEquals(600_000L, ChapterNavigator.nextStart(chapters, 300_000))
        assertEquals(1_200_000L, ChapterNavigator.nextStart(chapters, 600_000))
    }

    @Test
    fun `next chapter is unavailable in the final chapter`() {
        assertNull(ChapterNavigator.nextStart(chapters, 1_500_000))
        // Past the end of the book, too.
        assertNull(ChapterNavigator.nextStart(chapters, 1_800_000))
    }

    @Test
    fun `previous chapter restarts the current one once past the threshold`() {
        // The threshold is what makes the button usable for re-hearing the current chapter.
        assertEquals(600_000L, ChapterNavigator.previousStart(chapters, 600_000 + 3_000))
        assertEquals(600_000L, ChapterNavigator.previousStart(chapters, 900_000))
    }

    @Test
    fun `previous chapter steps back when near the start of a chapter`() {
        assertEquals(0L, ChapterNavigator.previousStart(chapters, 600_000 + 2_999))
        assertEquals(0L, ChapterNavigator.previousStart(chapters, 600_000))
    }

    @Test
    fun `previous chapter from within the first chapter returns to zero`() {
        assertEquals(0L, ChapterNavigator.previousStart(chapters, 300_000))
    }

    @Test
    fun `previous chapter is unavailable only at the very start of the book`() {
        // Already at zero, inside the first chapter: nowhere earlier to go.
        assertNull(ChapterNavigator.previousStart(chapters, 0))
        // Just inside the first chapter it still has somewhere to go — back to the start.
        assertEquals(0L, ChapterNavigator.previousStart(chapters, 2_999))
    }

    @Test
    fun `a book without chapters offers no navigation`() {
        assertNull(ChapterNavigator.nextStart(emptyList(), 1_000))
        assertNull(ChapterNavigator.previousStart(emptyList(), 1_000))
        assertEquals(-1, ChapterNavigator.indexAt(emptyList(), 1_000))
    }

    @Test
    fun `index reports the chapter containing the position`() {
        assertEquals(0, ChapterNavigator.indexAt(chapters, 0))
        assertEquals(0, ChapterNavigator.indexAt(chapters, 599_999))
        assertEquals(1, ChapterNavigator.indexAt(chapters, 600_000))
        assertEquals(2, ChapterNavigator.indexAt(chapters, 1_799_999))
    }

    @Test
    fun `a position before the first chapter starts is handled`() {
        // Defensive: chapter lists are server-supplied and need not start at zero.
        val offset = listOf(chapter(0, 30_000, 600_000))

        assertEquals(-1, ChapterNavigator.indexAt(offset, 10_000))
        assertEquals(30_000L, ChapterNavigator.nextStart(offset, 10_000))
        assertEquals(0L, ChapterNavigator.previousStart(offset, 10_000))
    }

    @Test
    fun `gaps between chapters do not break navigation`() {
        val gapped = listOf(
            chapter(0, 0, 300_000),
            chapter(1, 600_000, 900_000)
        )

        // Sitting in the gap still counts as being in the preceding chapter.
        assertEquals(0, ChapterNavigator.indexAt(gapped, 450_000))
        assertEquals(600_000L, ChapterNavigator.nextStart(gapped, 450_000))
        assertEquals(0L, ChapterNavigator.previousStart(gapped, 450_000))
    }

    @Test
    fun `ring marks are chapter starts as book fractions, first excluded`() {
        assertEquals(listOf(1f / 3, 2f / 3), ChapterNavigator.marks(chapters, 1_800_000))
    }

    @Test
    fun `ring marks drop out-of-range starts and unknown durations`() {
        val odd = chapters + chapter(3, 2_000_000, 2_100_000)
        assertEquals(listOf(1f / 3, 2f / 3), ChapterNavigator.marks(odd, 1_800_000))
        assertEquals(emptyList<Float>(), ChapterNavigator.marks(chapters, 0))
    }
}
