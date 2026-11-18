package com.nortlinos.wearos.data.repository

import com.nortlinos.wearos.data.local.ListeningSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListeningSessionPlannerTest {
    private val window = ListeningSessionPlanner.CONTINUE_WINDOW_MS
    private var ids = 0

    private fun plan(
        latest: ListeningSessionEntity?,
        positionMs: Long,
        listenedMs: Long,
        now: Long,
        streamed: Boolean = false
    ) = ListeningSessionPlanner.next(
        latest = latest,
        itemId = "book",
        originServerUrl = "https://abs.example",
        streamed = streamed,
        positionMs = positionMs,
        durationMs = 3_600_000,
        listenedMs = listenedMs,
        now = now,
        newId = { "s${++ids}" }
    )

    @Test
    fun firstListeningStartsASessionBackdatedByTheTimeListened() {
        val session = plan(null, positionMs = 70_000, listenedMs = 10_000, now = 1_000_000)!!
        assertEquals("s1", session.id)
        assertEquals(60_000L, session.startPositionMs)
        assertEquals(70_000L, session.positionMs)
        assertEquals(10_000L, session.listenedMs)
        assertEquals(990_000L, session.startedAt)
        assertEquals(1_000_000L, session.updatedAt)
        assertTrue(session.dirty)
    }

    @Test
    fun seekWithoutListeningDoesNotStartASession() {
        assertNull(plan(null, positionMs = 500_000, listenedMs = 0, now = 1_000_000))
    }

    @Test
    fun ticksWithinTheWindowAccumulateIntoOneSession() {
        val first = plan(null, positionMs = 10_000, listenedMs = 10_000, now = 1_000_000)!!
        val synced = first.copy(dirty = false)
        val second = plan(synced, positionMs = 20_000, listenedMs = 10_000, now = 1_010_000)!!
        assertEquals(first.id, second.id)
        assertEquals(20_000L, second.listenedMs)
        assertEquals(20_000L, second.positionMs)
        assertEquals(first.startedAt, second.startedAt)
        // Already-uploaded sessions are re-sent with the new totals; the server updates by id.
        assertTrue(second.dirty)
    }

    @Test
    fun seekWhilePausedMovesTheContinuingSessionPosition() {
        val first = plan(null, positionMs = 10_000, listenedMs = 10_000, now = 1_000_000)!!
        val moved = plan(first, positionMs = 2_000, listenedMs = 0, now = 1_005_000)!!
        assertEquals(first.id, moved.id)
        assertEquals(2_000L, moved.positionMs)
        assertEquals(10_000L, moved.listenedMs)
    }

    @Test
    fun longBreakStartsANewSession() {
        val first = plan(null, positionMs = 10_000, listenedMs = 10_000, now = 1_000_000)!!
        val later = plan(first, positionMs = 20_000, listenedMs = 10_000, now = 1_000_000 + window)!!
        assertNotEquals(first.id, later.id)
        assertEquals(10_000L, later.listenedMs)
    }

    @Test
    fun switchingBetweenStreamAndDownloadStartsANewSession() {
        val streamed = plan(null, 10_000, 10_000, now = 1_000_000, streamed = true)!!
        val local = plan(streamed, 20_000, 10_000, now = 1_010_000, streamed = false)!!
        assertNotEquals(streamed.id, local.id)
    }

    @Test
    fun clockMovedBackwardsStartsANewSession() {
        val first = plan(null, positionMs = 10_000, listenedMs = 10_000, now = 1_000_000)!!
        val skewed = plan(first, positionMs = 20_000, listenedMs = 10_000, now = 900_000)!!
        assertNotEquals(first.id, skewed.id)
    }

    @Test
    fun negativeInputsAreClamped() {
        val session = plan(null, positionMs = -5, listenedMs = 4_000, now = 10_000)!!
        assertEquals(0L, session.positionMs)
        assertEquals(0L, session.startPositionMs)
    }
}
