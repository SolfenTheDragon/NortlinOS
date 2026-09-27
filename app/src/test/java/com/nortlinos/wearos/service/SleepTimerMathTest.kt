package com.nortlinos.wearos.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepTimerMathTest {
    @Test
    fun `remaining time is deadline minus current time`() {
        assertEquals(30_000, SleepTimerMath.remainingMillis(90_000, 60_000))
    }

    @Test
    fun `remaining time never becomes negative`() {
        assertEquals(0, SleepTimerMath.remainingMillis(60_000, 90_000))
    }

    @Test
    fun `timer emits expiry and becomes inactive`() = runBlocking {
        val timer = SleepTimerManager(ExpiryAlarm.Disabled, TimeSource.System)

        // `expired` has no replay, so an expiry emitted before the collector attaches would be
        // dropped and this test would hang. `onSubscription` guarantees the timer only starts
        // once this collector is registered.
        withTimeout(1_000) {
            timer.expired.onSubscription { timer.start(20) }.first()
        }

        assertFalse(timer.state.value.active)
    }

    @Test
    fun `timer retains configured duration for reset`() {
        val timer = SleepTimerManager(ExpiryAlarm.Disabled, TimeSource.System)

        timer.start(45 * 60_000L)

        assertTrue(timer.state.value.active)
        assertEquals(45 * 60_000L, timer.state.value.durationMs)
        timer.cancel()
    }

    @Test
    fun `displayed minutes round up and never reach zero`() {        assertEquals(60, SleepTimerMath.displayedMinutes(60 * 60_000L))
        assertEquals(2, SleepTimerMath.displayedMinutes(90_000))
        assertEquals(1, SleepTimerMath.displayedMinutes(60_000))
        assertEquals(1, SleepTimerMath.displayedMinutes(1))
    }

    @Test
    fun `wakes once per displayed minute instead of once per second`() {
        assertEquals(60_000, SleepTimerMath.millisUntilNextDisplayChange(60 * 60_000L))
        assertEquals(60_000, SleepTimerMath.millisUntilNextDisplayChange(60_000))
    }

    @Test
    fun `wake lands on the boundary where the displayed value changes`() {
        val remaining = 90_000L
        val wait = SleepTimerMath.millisUntilNextDisplayChange(remaining)

        assertEquals(30_000, wait)
        // Crossing that boundary must actually change what the user sees.
        assertEquals(2, SleepTimerMath.displayedMinutes(remaining))
        assertEquals(1, SleepTimerMath.displayedMinutes(remaining - wait))
    }

    @Test
    fun `final wake lands exactly on expiry`() {
        assertEquals(1_000, SleepTimerMath.millisUntilNextDisplayChange(1_000))
        assertEquals(0, SleepTimerMath.millisUntilNextDisplayChange(0))
    }

    @Test
    fun `wake interval always advances the timer and is capped at one minute`() {
        var remaining = 720 * 60_000L
        var wakes = 0
        while (remaining > 0) {
            val wait = SleepTimerMath.millisUntilNextDisplayChange(remaining)
            assertTrue("wait must advance the timer", wait in 1..60_000)
            remaining -= wait
            wakes++
        }

        assertEquals(0, remaining)
        // The 12-hour maximum timer now costs 720 wakeups instead of 43,200.
        assertEquals(720, wakes)
    }
}
