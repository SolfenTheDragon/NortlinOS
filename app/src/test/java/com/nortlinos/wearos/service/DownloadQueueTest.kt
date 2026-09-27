package com.nortlinos.wearos.service

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadQueueTest {

    @Test
    fun `runs the first request without queuing it`() = runTest {
        val queue = DownloadQueue()
        var waited = false

        val ran = queue.withSlot(onWait = { waited = true }) { true }

        assertEquals(true, ran)
        assertFalse(waited)
        assertEquals(0, queue.activeCount)
    }

    @Test
    fun `holds later requests until the running one finishes`() = runTest {
        val queue = DownloadQueue()
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val order = mutableListOf<String>()
        var secondQueued = false

        val first = launch {
            queue.withSlot {
                order += "first-start"
                firstStarted.complete(Unit)
                releaseFirst.await()
                order += "first-end"
            }
        }
        firstStarted.await()

        val second = launch {
            queue.withSlot(onWait = { secondQueued = true }) { order += "second-start" }
        }
        testScheduler.runCurrent()

        assertTrue("the second book should have been told it is queued", secondQueued)
        assertEquals(listOf("first-start"), order)
        assertEquals(1, queue.activeCount)

        releaseFirst.complete(Unit)
        first.join()
        second.join()

        assertEquals(listOf("first-start", "first-end", "second-start"), order)
        assertEquals(0, queue.activeCount)
    }

    @Test
    fun `gives up its run when no slot comes free in time`() = runTest {
        val queue = DownloadQueue()
        val releaseFirst = CompletableDeferred<Unit>()
        val firstStarted = CompletableDeferred<Unit>()
        var queuedCallbacks = 0

        val first = launch {
            queue.withSlot {
                firstStarted.complete(Unit)
                releaseFirst.await()
            }
        }
        firstStarted.await()

        val secondRan = queue.withSlot(waitMillis = 1_000, onWait = { queuedCallbacks++ }) { true }

        assertEquals(null, secondRan)
        assertEquals(1, queuedCallbacks)

        releaseFirst.complete(Unit)
        first.join()
        assertEquals(0, queue.activeCount)
    }

    @Test
    fun `releases the slot when a transfer fails`() = runTest {
        val queue = DownloadQueue()

        runCatching { queue.withSlot<Unit> { throw IllegalStateException("network dropped") } }

        assertEquals(0, queue.activeCount)
        assertEquals(true, queue.withSlot { true })
    }

    @Test
    fun `admits every queued request in the order it arrived`() = runTest {
        val queue = DownloadQueue()
        val blockFirst = CompletableDeferred<Unit>()
        val started = mutableListOf<Int>()

        val jobs = (0 until 5).map { index ->
            launch {
                queue.withSlot {
                    started += index
                    if (index == 0) blockFirst.await()
                }
            }
        }
        testScheduler.runCurrent()

        assertEquals(listOf(0), started)

        blockFirst.complete(Unit)
        jobs.forEach { it.join() }

        assertEquals(listOf(0, 1, 2, 3, 4), started)
    }
}
