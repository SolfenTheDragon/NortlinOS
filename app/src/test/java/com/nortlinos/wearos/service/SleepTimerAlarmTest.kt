package com.nortlinos.wearos.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The countdown coroutine cannot be trusted to fire on time: audio offload lets the CPU suspend,
 * and `delay` runs on a clock that stops while it is. These tests pin the wakeup alarm that backs
 * it, driving both the alarm and the clock from the test.
 */
class SleepTimerAlarmTest {
    private class FakeAlarm : ExpiryAlarm {
        var scheduledFor: Long? = null
        private var pending: (() -> Unit)? = null

        override fun schedule(deadlineEpochMs: Long, onFire: () -> Unit) {
            scheduledFor = deadlineEpochMs
            pending = onFire
        }

        override fun cancel() {
            scheduledFor = null
        }

        fun fire() = pending?.invoke()
    }

    private class FakeClock(var now: Long = 1_700_000_000_000) : TimeSource {
        override fun nowMs(): Long = now
    }

    @Test
    fun `starting a timer schedules a wakeup at the deadline`() {
        val alarm = FakeAlarm()
        val clock = FakeClock()
        val timer = SleepTimerManager(alarm, clock)

        timer.start(30 * 60_000L)

        assertEquals(clock.now + 30 * 60_000L, alarm.scheduledFor)
        timer.cancel()
    }

    @Test
    fun `cancelling a timer cancels the wakeup`() {
        val alarm = FakeAlarm()
        val timer = SleepTimerManager(alarm, FakeClock())

        timer.start(30 * 60_000L)
        timer.cancel()

        assertNull(alarm.scheduledFor)
        assertFalse(timer.state.value.active)
    }

    @Test
    fun `alarm expires the timer even though the countdown has not woken`() = runBlocking {
        val alarm = FakeAlarm()
        val clock = FakeClock()
        val timer = SleepTimerManager(alarm, clock)

        // A one-minute timer's countdown would not wake for another 60 seconds, which is exactly
        // the window a suspended CPU stretches. The alarm must still end playback on time.
        withTimeout(1_000) {
            timer.expired.onSubscription {
                timer.start(60_000L)
                clock.now += 60_000L
                alarm.fire()
            }.first()
        }

        assertFalse(timer.state.value.active)
        assertNull(alarm.scheduledFor)
    }

    @Test
    fun `an alarm that fires before the deadline does not expire the timer`() {
        val alarm = FakeAlarm()
        val clock = FakeClock()
        val timer = SleepTimerManager(alarm, clock)

        timer.start(30 * 60_000L)
        clock.now += 60_000L
        alarm.fire()

        assertTrue(timer.state.value.active)
        timer.cancel()
    }

    @Test
    fun `a stale alarm from a cancelled timer is ignored`() = runBlocking {
        val alarm = FakeAlarm()
        val clock = FakeClock()
        val timer = SleepTimerManager(alarm, clock)

        timer.start(60_000L)
        timer.cancel()
        clock.now += 120_000L
        alarm.fire()

        assertFalse(timer.state.value.active)
    }

    @Test
    fun `restarting the timer reschedules the wakeup and ignores the old alarm`() {
        val alarm = FakeAlarm()
        val clock = FakeClock()
        val timer = SleepTimerManager(alarm, clock)

        timer.start(15 * 60_000L)
        val stale = alarm.scheduledFor
        timer.start(45 * 60_000L)

        assertEquals(clock.now + 45 * 60_000L, alarm.scheduledFor)
        assertTrue(stale != alarm.scheduledFor)

        // The first run's callback was replaced, but firing the surviving one 15 minutes in must
        // not cut the newly started 45-minute timer short.
        clock.now += 15 * 60_000L
        alarm.fire()
        assertTrue(timer.state.value.active)
        timer.cancel()
    }
}
